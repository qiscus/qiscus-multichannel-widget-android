package com.qiscus.qiscusmultichannel.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.qiscus.qiscusmultichannel.R
import com.qiscus.sdk.chat.core.custom.QiscusCore
import com.qiscus.sdk.chat.core.custom.data.local.QiscusCacheManager
import com.qiscus.sdk.chat.core.custom.data.model.QiscusComment
import com.qiscus.sdk.chat.core.custom.util.BuildVersionUtil
import com.qiscus.sdk.chat.core.custom.util.QiscusAndroidUtil
import com.qiscus.sdk.chat.core.custom.util.QiscusNumberUtil
import org.json.JSONObject

/**
 * Created on : 2019-11-08
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class PNUtil {

    companion object {
        fun showPn(context: Context, qiscusComment: QiscusComment) {
            if (QiscusCore.getDataStore().isContains(qiscusComment)) {
                return
            }
            QiscusCore.getDataStore().addOrUpdate(qiscusComment)

            val lastActivity = QiscusCacheManager.INSTANCE.lastChatActivity
            if (lastActivity.first!! && lastActivity.second == qiscusComment.roomId) {
                return
            }

            if (QiscusCore.getQiscusAccount().email == qiscusComment.senderEmail) {
                return
            }

            val notificationChannelId =
                QiscusCore.getApps().packageName + ".qiscus.sdk.notification.channel"
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
            pendingIntent = PendingIntent.getBroadcast(
                context, QiscusNumberUtil.convertToInt(qiscusComment.roomId),
                openIntent, PendingIntent.FLAG_CANCEL_CURRENT
            )

            val notificationBuilder = NotificationCompat.Builder(context, notificationChannelId)
            notificationBuilder.setContentTitle(qiscusComment.roomName)
                .setContentIntent(pendingIntent)
                .setContentText(getContent(context, qiscusComment))
                .setTicker(getContent(context, qiscusComment))
                //@TODO Change background image
                .setSmallIcon(R.drawable.mybblogo)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setGroup("CHAT_NOTIF_" + qiscusComment.roomId)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

            QiscusAndroidUtil.runOnUIThread {
                NotificationManagerCompat.from(context)
                    .notify(
                        QiscusNumberUtil.convertToInt(qiscusComment.roomId),
                        notificationBuilder.build()
                    )
            }
        }

        private fun getContent(context: Context, qiscusComment: QiscusComment): String {

            val account = QiscusCore.getQiscusAccount()
            var sender = ""

            if (qiscusComment.isGroupMessage &&
                qiscusComment.type != QiscusComment.Type.CUSTOM &&
                qiscusComment.type != QiscusComment.Type.LOCATION
            ) {
                sender = "${qiscusComment.sender} : "
            }

            if (EventUtil.isChatEvent(qiscusComment)) {
                val json = JSONObject(qiscusComment.extraPayload)
                val payload = json.getJSONObject("content").getJSONObject("chat_event")
                return sender + ParsingChatEventUtil.instance.parsingMessage(payload, account)
            } else {
                if (qiscusComment.type == QiscusComment.Type.CUSTOM) {
                    val obj = JSONObject(qiscusComment.extraPayload)
                    val type = obj.getString("type")
                    if (type.contains("image")) {
                        return sender + context.getString(
                            R.string.qiscus_send_image_mc,
                            qiscusComment.sender
                        )
                    }

                    if (type.contains("file")) {
                        return sender + context.getString(
                            R.string.qiscus_send_file_mc,
                            qiscusComment.sender
                        )
                    }
                } else if (qiscusComment.type == QiscusComment.Type.LOCATION) {
                    return sender + context.getString(
                        R.string.qiscus_send_location_mc,
                        qiscusComment.sender
                    )
                } else if (qiscusComment.type == QiscusComment.Type.FILE) {
                    return sender + context.getString(
                        R.string.qiscus_send_file_mc,
                        qiscusComment.sender
                    )
                } else if (qiscusComment.type == QiscusComment.Type.IMAGE) {
                    return sender + context.getString(
                        R.string.qiscus_send_image_mc,
                        qiscusComment.sender
                    )
                }
                return sender + qiscusComment.message
            }

        }
    }
}