package com.qiscus.qiscusmultichannel.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.qiscus.nirmana.Nirmana
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.MultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.util.Const
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.event.QMessageReceivedEvent
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

    lateinit var qiscusChatRoom: QChatRoom
    private val users: MutableSet<String> = HashSet()
    private var memberList: String = ""
    private var runnable = Runnable {
        runOnUiThread {
            tvSubtitle?.text = MultichannelWidget.config.getRoomSubtitle() ?: memberList
        }
    }
    private var handler = Handler(Looper.getMainLooper())

    companion object {
        val CHATROOM_KEY = "chatroom_key"

        fun generateIntent(
            context: Context,
            qiscusChatRoom: QChatRoom
        ): Intent {

            val intent = Intent(context, ChatRoomActivity::class.java)
            //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(CHATROOM_KEY, qiscusChatRoom)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room_mc)
        /* setSupportActionBar(toolbar_selected_comment)
          supportActionBar?.title = qiscusChatRoom.name
          toolbar.setNavigationIcon(R.drawable.ic_back)
          toolbar.setNavigationOnClickListener { finish() }
         */

        qiscusChatRoom = intent.getParcelableExtra(CHATROOM_KEY)!!

        if (!this::qiscusChatRoom.isInitialized) {
            finish()
            return
        }

        btn_back.setOnClickListener { finish() }

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

        tvTitle.text = MultichannelWidget.config.getRoomTitle() ?: qiscusChatRoom.name

        val avatar = MultichannelWidget.config.getHardcodedAvatar() ?: qiscusChatRoom.avatarUrl
        Nirmana.getInstance().get()
            .load(getAvatar())
            .into(ivAvatar)
    }

    override fun onResume() {
        super.onResume()
        bindRoomData()
    }

    private fun getChatFragment(): ChatRoomFragment {
        return supportFragmentManager.findFragmentByTag(ChatRoomFragment::class.java.name) as ChatRoomFragment
    }

    override fun onCommentSelected(selectedComment: QMessage) {
        val me = Const.qiscusCore()?.getQiscusAccount()?.getId()
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
        tvSubtitle?.text = if (isTyping) "typing..." else getSubtitle()

        if (isTyping) {
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, 5000)
        }
    }

    private fun bindRoomData() {
        for (member in qiscusChatRoom.participants) {
            if (member.id != MultichannelWidget.instance.getQiscusAccount().id) {
                users.add(member.id)
                Const.qiscusCore()?.pusherApi?.subscribeUserOnlinePresence(member.id)
            }
        }
    }

    private fun setBarInfo() {
        val listMember: ArrayList<String> = arrayListOf()
        Const.qiscusCore()?.api?.getChatRoomInfo(qiscusChatRoom.id)
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
        return MultichannelWidget.config.getRoomSubtitle() ?: memberList
    }

    fun getAvatar(): String {
        for (member in qiscusChatRoom.participants) {
            val type = member.extras.getString("type")
            if (type.isNotEmpty() && type == "agent") {
                return member.avatarUrl
            }
        }

        return MultichannelWidgetConfig.getHardcodedAvatar() ?: qiscusChatRoom.avatarUrl
    }

    override fun onDestroy() {
        super.onDestroy()
        for (user in users) {
            Const.qiscusCore()?.pusherApi?.unsubscribeUserOnlinePresence(user)
        }
        EventBus.getDefault().unregister(this)
        clearFindViewByIdCache()
    }
}
