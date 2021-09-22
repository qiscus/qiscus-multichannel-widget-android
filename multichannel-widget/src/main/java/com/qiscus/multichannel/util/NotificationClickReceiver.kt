package com.qiscus.multichannel.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.qiscus.multichannel.QiscusMultichannelWidget
import com.qiscus.multichannel.data.local.QiscusChatLocal
import com.qiscus.sdk.chat.core.data.model.QMessage

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Aug, Tue 14 2018 12.40
 **/
class NotificationClickReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val qiscusComment = intent.getParcelableExtra<QMessage>("data")
        qiscusComment?.let {
            QiscusChatLocal.setRoomId(it.chatRoomId)
            QiscusMultichannelWidget.instance.openChatRoom(context, true)
        }
    }
}