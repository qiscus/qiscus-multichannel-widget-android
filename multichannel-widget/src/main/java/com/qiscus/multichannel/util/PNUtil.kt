package com.qiscus.multichannel.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.qiscus.multichannel.QiscusMultichannelWidget
import com.qiscus.multichannel.R
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.util.BuildVersionUtil
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil
import com.qiscus.sdk.chat.core.util.QiscusNumberUtil
import org.json.JSONObject


/**
 * Created on : 2019-11-08
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class PNUtil {

    companion object {
        fun showPn(context: Context, qiscusComment: QMessage) {
            if (MultichannelConst.qiscusCore()?.dataStore?.isContains(qiscusComment)!!) {
                return
            }
            MultichannelConst.qiscusCore()?.dataStore?.addOrUpdate(qiscusComment)

            val lastActivity = MultichannelConst.qiscusCore()?.cacheManager?.lastChatActivity!!
            if (lastActivity.first!! && lastActivity.second == qiscusComment.chatRoomId) {
                return
            }

            if (MultichannelConst.qiscusCore()?.qiscusAccount?.id == qiscusComment.sender.id) {
                return
            }

            if (!QiscusMultichannelWidget.instance.getConfig()
                    .isShowSystemMessage() && qiscusComment.type == QMessage.Type.SYSTEM_EVENT
            ) {
                return
            }

            val notificationChannelId =
                MultichannelConst.qiscusCore()?.apps?.packageName + ".qiscus.sdk.notification.channel"
            if (BuildVersionUtil.isOreoOrHigher()) {
                val notificationChannel = NotificationChannel(
                    notificationChannelId,
                    "Chat",
                    NotificationManager.IMPORTANCE_HIGH
                )
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)
            }

            val pendingIntent: PendingIntent
            val openIntent = Intent(context, NotificationClickReceiver::class.java)
            openIntent.putExtra("data", qiscusComment)

            pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getBroadcast(
                    context, QiscusNumberUtil.convertToInt(qiscusComment.chatRoomId),
                    openIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                PendingIntent.getBroadcast(
                    context, QiscusNumberUtil.convertToInt(qiscusComment.chatRoomId),
                    openIntent, PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            val room =
                MultichannelConst.qiscusCore()?.dataStore?.getChatRoom(qiscusComment.chatRoomId)
            val notificationBuilder = NotificationCompat.Builder(context, notificationChannelId)
            notificationBuilder.setContentTitle(room?.name)
                .setContentIntent(pendingIntent)
                .setContentText(getContent(context, qiscusComment))
                .setTicker(getContent(context, qiscusComment))
                //@TODO Change background image
                .setSmallIcon(QiscusMultichannelWidget.instance.getConfig().getNotificationIcon())
                .setColor(ContextCompat.getColor(context, R.color.qiscus_notification_mc))
                .setGroup("CHAT_NOTIF_" + qiscusComment.chatRoomId)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

            QiscusAndroidUtil.runOnUIThread {
                NotificationManagerCompat.from(context)
                    .notify(
                        QiscusNumberUtil.convertToInt(qiscusComment.chatRoomId),
                        notificationBuilder.build()
                    )
            }
        }

        private fun getContent(context: Context, qiscusComment: QMessage): String {

            val account = MultichannelConst.qiscusCore()?.qiscusAccount!!
            var sender = ""

            var chatRoom =
                MultichannelConst.qiscusCore()?.dataStore?.getChatRoom(qiscusComment.chatRoomId)
            if (chatRoom?.type == "group" &&
                qiscusComment.type != QMessage.Type.CUSTOM &&
                qiscusComment.type != QMessage.Type.LOCATION
            ) {
                sender = "${qiscusComment.sender.name} : "
            }

            if (EventUtil.isChatEvent(qiscusComment)) {
                val json = JSONObject(qiscusComment.payload)
                val payload = json.getJSONObject("content").getJSONObject("chat_event")
                return sender + ParsingChatEventUtil.instance.parsingMessage(payload, account)
            } else {
                if (qiscusComment.type == QMessage.Type.CUSTOM) {
                    val obj = JSONObject(qiscusComment.payload)
                    val type = obj.getString("type")
                    if (type.contains("image")) {
                        return sender + context.getString(
                            R.string.qiscus_send_image_mc,
                            qiscusComment.sender.name
                        )
                    }

                    if (type.contains("file")) {
                        return sender + context.getString(
                            R.string.qiscus_send_file_mc,
                            qiscusComment.sender.name
                        )
                    }
                } else if (qiscusComment.type == QMessage.Type.LOCATION) {
                    return sender + context.getString(
                        R.string.qiscus_send_location_mc,
                        qiscusComment.sender
                    )
                } else if (qiscusComment.type == QMessage.Type.FILE) {
                    return sender + context.getString(
                        R.string.qiscus_send_file_mc,
                        qiscusComment.sender.name
                    )
                } else if (qiscusComment.type == QMessage.Type.IMAGE) {
                    return sender + context.getString(
                        R.string.qiscus_send_image_mc,
                        qiscusComment.sender.name
                    )
                }
                return sender + qiscusComment.text
            }

        }
    }
}