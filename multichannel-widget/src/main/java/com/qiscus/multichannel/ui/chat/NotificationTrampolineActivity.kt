package com.qiscus.multichannel.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.TaskStackBuilder
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.multichannel.util.PNUtil
import com.qiscus.multichannel.util.showToast
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.data.model.QMessage
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class NotificationTrampolineActivity : AppCompatActivity() {

    companion object {
        private val OPEN_INTENT = "open_intent"

        fun generateIntent(context: Context, comment: QMessage?): Intent {
            return Intent(
                context.applicationContext, NotificationTrampolineActivity::class.java
            )
                .putExtra(OPEN_INTENT, comment)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val qiscusComment: QMessage? = intent.getParcelableExtra(OPEN_INTENT)
        if (qiscusComment != null) {
            MultichannelConst.qiscusCore()!!.api
                .getChatRoomInfo(qiscusComment.chatRoomId)
                .map { qiscusChatRoom: QChatRoom? -> getChatRoom(qiscusChatRoom, qiscusComment) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { newIntent ->
                        startActivityMessage(
                            this, newIntent
                        )
                    }) { throwable: Throwable ->
                    showError(
                        throwable
                    )
                }
        }
        ActivityCompat.finishAffinity(this)
    }

    private fun getChatRoom(qChatRoom: QChatRoom?, qMessage: QMessage?): Intent {
        val intent = Intent(this, ChatRoomActivity::class.java)
        intent.putExtra(ChatRoomActivity.CHATROOM_KEY, qChatRoom)
        intent.putExtra(ChatRoomActivity.MESSAGE_KEY, qMessage)
        return intent
    }

    private fun showError(throwable: Throwable) {
        if (throwable.message == null) return
        showToast(throwable.message.toString())
    }
    private fun startActivityMessage(context: Context?, newIntent: Intent?) {
        val taskStackBuilder: TaskStackBuilder = TaskStackBuilder.create(
            context!!
        )

        val parentActivity = PNUtil.pnBuilder.parentActivityClass
        if (parentActivity != null) {
            taskStackBuilder
                .addParentStack(parentActivity)
                .addNextIntent(
                    Intent(context, parentActivity)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
        }

        if (newIntent != null) taskStackBuilder.addNextIntent(newIntent)
        taskStackBuilder.startActivities()
    }

}