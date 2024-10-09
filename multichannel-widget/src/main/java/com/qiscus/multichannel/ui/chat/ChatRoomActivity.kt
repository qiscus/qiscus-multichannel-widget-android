package com.qiscus.multichannel.ui.chat

import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.request.RequestOptions
import com.qiscus.multichannel.QiscusMultichannelWidget
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.data.local.QiscusSessionLocal
import com.qiscus.multichannel.databinding.ActivityChatRoomMcBinding
import com.qiscus.multichannel.util.MultichanelChatWidget
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.multichannel.util.QiscusPermissionsUtil
import com.qiscus.multichannel.util.ResourceManager
import com.qiscus.nirmana.Nirmana
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.event.QMessageReceivedEvent
import com.qiscus.sdk.chat.core.event.QiscusMqttStatusEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class ChatRoomActivity : AppCompatActivity(), ChatRoomFragment.CommentSelectedListener,
    ChatRoomFragment.OnUserTypingListener {

    private lateinit var binding: ActivityChatRoomMcBinding
    lateinit var displayMetrics: DisplayMetrics
    lateinit var qiscusChatRoom: QChatRoom
    private val users: MutableSet<String> = HashSet()
    private var subtitle: String = ""
    private var memberList: String = ""
    private val qiscusMultichannelWidget: MultichanelChatWidget = QiscusMultichannelWidget.instance

    private var runnable = Runnable {
        runOnUiThread {
            binding.tvSubtitle.text = qiscusMultichannelWidget.getConfig().getRoomSubtitle() ?: memberList
        }
    }
    private var handler = Handler(Looper.getMainLooper())

    companion object {
        const val CHATROOM_KEY = "chatroom_key"
        const val MESSAGE_KEY = "message_key"
        const val AUTO_MESSAGE_KEY = "auto_message_key"
        const val IS_TEST_MODE = "is_test_mode"

        fun generateIntent(
            context: Context,
            qiscusChatRoom: QChatRoom,
            qiscusMessage: QMessage?,
            isAutoSendMessage: Boolean,
            isTest: Boolean,
            clearTaskActivity: Boolean
        ) {
            val intent = Intent(context, ChatRoomActivity::class.java)
            if (clearTaskActivity) intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(CHATROOM_KEY, qiscusChatRoom)
            intent.putExtra(MESSAGE_KEY, qiscusMessage)
            intent.putExtra(AUTO_MESSAGE_KEY, isAutoSendMessage)
            intent.putExtra(IS_TEST_MODE, isTest)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomMcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        QiscusSessionLocal.removeInitiate()
        ResourceManager.setUp(this, qiscusMultichannelWidget.getColor())
        initColor()

        val room = intent.getParcelableExtra<QChatRoom>(CHATROOM_KEY)
        val qMessage = intent.getParcelableExtra<QMessage>(MESSAGE_KEY)
        val isAutoSendMessage = intent.getBooleanExtra(AUTO_MESSAGE_KEY, false)
        val isTest = intent.getBooleanExtra(IS_TEST_MODE, false)

        if (isTest || room == null) {
            finish()
            return
        } else {
            this.qiscusChatRoom = room
        }

        binding.btnBack.setOnClickListener { finish() }

        displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.getRealMetrics(displayMetrics)
        } else {
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        ResourceManager.DIMEN_ROUNDED_IMAGE = ResourceManager.getDimen(displayMetrics, 8).toInt()

        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                ChatRoomFragment.newInstance(qiscusChatRoom, qMessage, isAutoSendMessage),
                ChatRoomFragment::class.java.name
            )
            .commit()

        //setAlarmManager()

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        binding.toolbarSelectedComment.btnActionCopy.setOnClickListener { getChatFragment().copyComment() }
        binding.toolbarSelectedComment.btnActionDelete.setOnClickListener { getChatFragment().deleteComment() }
        binding.toolbarSelectedComment.btnActionReply.setOnClickListener { getChatFragment().replyComment() }
        binding.toolbarSelectedComment.btnActionReplyCancel.setOnClickListener { getChatFragment().clearSelectedComment() }
        setBarInfo()

        binding.tvTitleFile.text =
            qiscusMultichannelWidget.getConfig().getRoomTitle() ?: qiscusChatRoom.name
        binding.tvSubtitle.visibility = if (qiscusMultichannelWidget.getConfig().getRoomSubtitleType() ==
            QiscusMultichannelWidgetConfig.RoomSubtitle.DISABLE
        ) View.GONE else View.VISIBLE

        Nirmana.getInstance().get()
            .load(getAvatar())
            .apply(
                RequestOptions()
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .dontAnimate()
            )
            .into(binding.ivAvatar)

        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            QiscusPermissionsUtil.requestNotificationPermission(this)
        }
    }

    private fun initColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = qiscusMultichannelWidget.getColor().getStatusBarColor()
        }
        binding.toolbar.setBackgroundColor(qiscusMultichannelWidget.getColor().getNavigationColor())

        binding.tvTitleFile.setTextColor(qiscusMultichannelWidget.getColor().getNavigationTitleColor())
        binding.tvSubtitle.setTextColor(qiscusMultichannelWidget.getColor().getNavigationTitleColor())
        binding.btnBack.setColorFilter(
            ContextCompat.getColor(
                this,
                R.color.qiscus_back_icon_mc
            ),
            PorterDuff.Mode.SRC_IN
        )

        binding.btnBack.setColorFilter(qiscusMultichannelWidget.getColor().getNavigationTitleColor())
        binding.toolbarSelectedComment.containerOption.setBackgroundColor(qiscusMultichannelWidget.getColor().getNavigationColor())
        binding.toolbarSelectedComment.btnActionCopy.setColorFilter(
            qiscusMultichannelWidget.getColor().getNavigationTitleColor()
        )
        binding.toolbarSelectedComment.btnActionDelete.setColorFilter(
            qiscusMultichannelWidget.getColor().getNavigationTitleColor()
        )
        binding.toolbarSelectedComment.btnActionReply.setColorFilter(
            qiscusMultichannelWidget.getColor().getNavigationTitleColor()
        )
        binding.toolbarSelectedComment.btnActionReplyCancel.setColorFilter(
            qiscusMultichannelWidget.getColor().getNavigationTitleColor()
        )
    }

    override fun onResume() {
        super.onResume()
        for (member in qiscusChatRoom.participants) {
            if (member.id != qiscusMultichannelWidget.getQiscusAccount().id) {
                users.add(member.id)
                MultichannelConst.qiscusCore()?.pusherApi?.subscribeUserOnlinePresence(member.id)
            }
        }
    }

    private fun getChatFragment(): ChatRoomFragment {
        return supportFragmentManager.findFragmentByTag(ChatRoomFragment::class.java.name) as ChatRoomFragment
    }

    override fun onCommentSelected(selectedComment: QMessage) {
        val me = MultichannelConst.qiscusCore()?.qiscusAccount?.id
        if (binding.toolbarSelectedComment.root.visibility == View.VISIBLE) {
            binding.toolbarSelectedComment.root.visibility = View.GONE
            getChatFragment().clearSelectedComment()
        } else {
            binding.toolbarSelectedComment.btnActionDelete.visibility =
                if (selectedComment.isMyComment(me)) View.VISIBLE else View.GONE
            binding.toolbarSelectedComment.root.visibility = View.VISIBLE
        }
    }

    override fun onClearSelectedComment(status: Boolean) {
        binding.toolbarSelectedComment.root.visibility = View.INVISIBLE
    }

    override fun onUserTyping(email: String?, isTyping: Boolean) {
        if (qiscusMultichannelWidget.getConfig().getRoomSubtitleType() ==
            QiscusMultichannelWidgetConfig.RoomSubtitle.DISABLE
        ) return

        subtitle = if (isTyping) "typing..." else getSubtitle()
        runOnUiThread {
            binding.tvSubtitle.text = subtitle
        }

        if (isTyping) {
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, 3000)
        }
    }

    private fun setBarInfo() {
        val listMember: ArrayList<String> = arrayListOf()
        MultichannelConst.qiscusCore()?.api?.getChatRoomInfo(qiscusChatRoom.id)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { chatRoom ->
                chatRoom.participants.forEach {
                    listMember.add(it.name)
                }
                this.memberList = listMember.joinToString()
                binding.tvSubtitle.text = getSubtitle()
            }
    }

    /*@Subscribe
    fun onUserStatusChanged(event: QiscusUserStatusEvent) {
        val last = QiscusDateUtil.getRelativeTimeDiff(event.lastActive)
        if (users.contains(event.user)) {
            //tvSubtitle?.text = if (event.isOnline) "Online" else "Last seen $last"
        }
    }*/

    @Subscribe
    fun onMessageReceived(event: QMessageReceivedEvent) {
        when (event.qiscusComment.type) {
            QMessage.Type.SYSTEM_EVENT -> setBarInfo()
            else -> {/*ignored*/}
        }
    }

    fun getSubtitle(): String {
        return qiscusMultichannelWidget.getConfig().getRoomSubtitle() ?: memberList
    }

    fun getAvatar(): String {
        for (member in qiscusChatRoom.participants) {
            if (member.extras.has("type")) {
                val type = member.extras.getString("type")
                if (type.isNotEmpty() && type == "agent") {
                    return member.avatarUrl
                }
            }
        }

        return getString(R.string.default_avatar_url)
    }

    override fun onDestroy() {
        super.onDestroy()
        for (user in users) {
            MultichannelConst.qiscusCore()?.pusherApi?.unsubscribeUserOnlinePresence(user)
        }
        EventBus.getDefault().unregister(this)
//        clearFindViewByIdCache()
    }

    @Subscribe
    fun onConnection(mqttStatusEvent: QiscusMqttStatusEvent) {
        when (mqttStatusEvent) {
            QiscusMqttStatusEvent.CONNECTED -> {
                Log.i("test_mqtt:", "connected")
            }
            QiscusMqttStatusEvent.DISCONNECTED -> {
                Log.i("test_mqtt:", "disconnected")
            }
            else -> {/*ignored*/}
        }
    }

    private fun setAlarmManager() {
        val alarmMgr = this.getSystemService(ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            if (!alarmMgr.canScheduleExactAlarms()) {
                val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setTitle("Permission necessary")
                    .setMessage("Schedule Exact Alarm permission is necessary for realtime")
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent = Intent(
                                ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                Uri.parse("package:" + this.packageName)
                            )
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            this.applicationContext.startActivity(intent)
                        }
                    }
                val alert: AlertDialog = alertBuilder.create()
                alert.show()
            }
        }
    }
}
