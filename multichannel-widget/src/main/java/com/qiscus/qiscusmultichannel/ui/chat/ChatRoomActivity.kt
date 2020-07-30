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
import com.qiscus.qiscusmultichannel.R
import com.qiscus.sdk.chat.core.custom.data.model.QiscusChatRoom
import com.qiscus.sdk.chat.core.custom.data.model.QiscusComment
import com.qiscus.sdk.chat.core.custom.data.remote.QiscusApi
import com.qiscus.sdk.chat.core.custom.data.remote.QiscusPusherApi
import com.qiscus.sdk.chat.core.custom.event.QiscusCommentReceivedEvent
import com.qiscus.sdk.chat.core.custom.event.QiscusUserStatusEvent
import com.qiscus.sdk.chat.core.custom.util.QiscusDateUtil
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_chat_room_mc.*
import kotlinx.android.synthetic.main.toolbar_menu_selected_comment_mc.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class ChatRoomActivity : AppCompatActivity(), ChatRoomFragment.CommentSelectedListener,
    ChatRoomFragment.OnUserTypingListener {

    lateinit var qiscusChatRoom: QiscusChatRoom
    private val users: MutableSet<String> = HashSet()
    private var memberList: String = ""
    private  var runnable =  Runnable{
        tvSubtitle?.text = MultichannelWidget.config.getRoomSubtitle() ?: memberList
    }
    private var handler = Handler(Looper.getMainLooper())
    companion object {
        val CHATROOM_KEY = "chatroom_key"

        fun generateIntent(
            context: Context,
            qiscusChatRoom: QiscusChatRoom
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

        qiscusChatRoom = intent.getParcelableExtra(CHATROOM_KEY)

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
        btn_action_reply_cancle.setOnClickListener { getChatFragment().clearSelectedComment() }
        setBarInfo()

        tvTitle.text = MultichannelWidget.config.getRoomTitle() ?: qiscusChatRoom.name

        Nirmana.getInstance().get()
            .load(qiscusChatRoom.avatarUrl)
            .into(ivAvatar)
    }

    override fun onResume() {
        super.onResume()
        bindRoomData()
    }

    private fun getChatFragment(): ChatRoomFragment {
        return supportFragmentManager.findFragmentByTag(ChatRoomFragment::class.java.name) as ChatRoomFragment
    }

    override fun onCommentSelected(selectedComment: QiscusComment) {
        if (toolbar_selected_comment.visibility == View.VISIBLE) {
            toolbar_selected_comment.visibility = View.GONE
            getChatFragment().clearSelectedComment()
        } else {
            btn_action_delete.visibility =
                if (selectedComment.isMyComment) View.VISIBLE else View.GONE
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
        for (member in qiscusChatRoom.member) {
            if (member.email != MultichannelWidget.instance.getQiscusAccount().email) {
                users.add(member.email)
                QiscusPusherApi.getInstance().subscribeUserOnlinePresence(member.email)
            }
        }
    }

    private fun setBarInfo() {
        val listMember: ArrayList<String> = arrayListOf()
        QiscusApi.getInstance().getChatRoomInfo(qiscusChatRoom.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { chatRoom ->
                chatRoom.member.forEach {
                    listMember.add(it.username)
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
    fun onMessageReceived(event: QiscusCommentReceivedEvent) {
        when (event.qiscusComment.type) {
            QiscusComment.Type.SYSTEM_EVENT -> setBarInfo()
        }

    }

    fun getSubtitle(): String {
        return MultichannelWidget.config.getRoomSubtitle() ?: memberList
    }

    override fun onDestroy() {
        super.onDestroy()
        for (user in users) {
            QiscusPusherApi.getInstance().unsubscribeUserOnlinePresence(user)
        }
        EventBus.getDefault().unregister(this)
        clearFindViewByIdCache()
    }
}
