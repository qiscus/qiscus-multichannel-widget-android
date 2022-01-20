package com.qiscus.multichannel.ui.chat

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.request.RequestOptions
import com.qiscus.multichannel.QiscusMultichannelWidget
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.util.MultichanelChatWidget
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.multichannel.util.ResourceManager
import com.qiscus.nirmana.Nirmana
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.event.QMessageReceivedEvent
import com.qiscus.sdk.chat.core.event.QiscusMqttStatusEvent
import com.qiscus.sdk.chat.core.event.QiscusUserStatusEvent
import com.qiscus.sdk.chat.core.util.QiscusDateUtil
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_chat_room_mc.*
import kotlinx.android.synthetic.main.toolbar_menu_selected_comment_mc.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class ChatRoomActivity : AppCompatActivity(), ChatRoomFragment.CommentSelectedListener,
    ChatRoomFragment.OnUserTypingListener {

    lateinit var displayMetrics: DisplayMetrics
    lateinit var qiscusChatRoom: QChatRoom
    private val users: MutableSet<String> = HashSet()
    private var subtitle: String = ""
    private var memberList: String = ""
    private val qiscusMultichannelWidget: MultichanelChatWidget = QiscusMultichannelWidget.instance

    private var runnable = Runnable {
        runOnUiThread {
            tvSubtitle?.text = qiscusMultichannelWidget.getConfig().getRoomSubtitle() ?: memberList
        }
    }
    private var handler = Handler(Looper.getMainLooper())

    companion object {
        val CHATROOM_KEY = "chatroom_key"

        fun generateIntent(
            context: Context,
            qiscusChatRoom: QChatRoom,
            clearTaskActivity: Boolean
        ) {
            val intent = Intent(context, ChatRoomActivity::class.java)
            if (clearTaskActivity) intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(CHATROOM_KEY, qiscusChatRoom)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room_mc)
        ResourceManager.setUp(this, qiscusMultichannelWidget.getColor())
        initColor()

        val room = intent.getParcelableExtra<QChatRoom>(CHATROOM_KEY)

        if (room == null) {
            finish()
            return
        } else {
            this.qiscusChatRoom = room
        }

        btnBack.setOnClickListener { finish() }

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
                ChatRoomFragment.newInstance(qiscusChatRoom),
                ChatRoomFragment::class.java.name
            )
            .commit()

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        btn_action_copy.setOnClickListener { getChatFragment().copyComment() }
        btn_action_delete.setOnClickListener { getChatFragment().deleteComment() }
        btn_action_reply.setOnClickListener { getChatFragment().replyComment() }
        btn_action_reply_cancel.setOnClickListener { getChatFragment().clearSelectedComment() }
        setBarInfo()

        tv_title_file.text =
            qiscusMultichannelWidget.getConfig().getRoomTitle() ?: qiscusChatRoom.name
        tvSubtitle.visibility = if (qiscusMultichannelWidget.getConfig().getRoomSubtitleType() ==
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
            .into(ivAvatar)
    }

    private fun initColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = qiscusMultichannelWidget.getColor().getStatusBarColor()
        }
        toolbar.setBackgroundColor(qiscusMultichannelWidget.getColor().getNavigationColor())

        tv_title_file.setTextColor(qiscusMultichannelWidget.getColor().getNavigationTitleColor())
        tvSubtitle.setTextColor(qiscusMultichannelWidget.getColor().getNavigationTitleColor())
        btnBack.setColorFilter(
            ContextCompat.getColor(
                this,
                R.color.qiscus_back_icon_mc
            ),
            PorterDuff.Mode.SRC_IN
        )

        btnBack.setColorFilter(qiscusMultichannelWidget.getColor().getNavigationTitleColor())
        containerOption.setBackgroundColor(qiscusMultichannelWidget.getColor().getNavigationColor())
        btn_action_copy.setColorFilter(
            qiscusMultichannelWidget.getColor().getNavigationTitleColor()
        )
        btn_action_delete.setColorFilter(
            qiscusMultichannelWidget.getColor().getNavigationTitleColor()
        )
        btn_action_reply.setColorFilter(
            qiscusMultichannelWidget.getColor().getNavigationTitleColor()
        )
        btn_action_reply_cancel.setColorFilter(
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
        if (toolbar_selected_comment.visibility == View.VISIBLE) {
            toolbar_selected_comment.visibility = View.GONE
            getChatFragment().clearSelectedComment()
        } else {
            btn_action_delete.visibility =
                if (selectedComment.isMyComment(me)) View.VISIBLE else View.GONE
            toolbar_selected_comment.visibility = View.VISIBLE
        }
    }

    override fun onClearSelectedComment(status: Boolean) {
        toolbar_selected_comment.visibility = View.INVISIBLE
    }

    override fun onUserTyping(email: String?, isTyping: Boolean) {
        if (qiscusMultichannelWidget.getConfig().getRoomSubtitleType() ==
            QiscusMultichannelWidgetConfig.RoomSubtitle.DISABLE
        ) return

        subtitle = if (isTyping) "typing..." else getSubtitle()
        runOnUiThread {
            tvSubtitle?.text = subtitle
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
                tvSubtitle.text = getSubtitle()
            }
    }

    @Subscribe
    fun onUserStatusChanged(event: QiscusUserStatusEvent) {
        val last = QiscusDateUtil.getRelativeTimeDiff(event.lastActive)
        if (users.contains(event.user)) {
            //tvSubtitle?.text = if (event.isOnline) "Online" else "Last seen $last"
        }
    }

    @Subscribe
    fun onMessageReceived(event: QMessageReceivedEvent) {
        when (event.qiscusComment.type) {
            QMessage.Type.SYSTEM_EVENT -> setBarInfo()
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
        clearFindViewByIdCache()
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
        }
    }
}
