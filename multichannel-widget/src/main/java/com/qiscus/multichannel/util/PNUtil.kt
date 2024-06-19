package com.qiscus.multichannel.util

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import com.qiscus.multichannel.QiscusMultichannelWidget
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.NotificationTrampolineActivity
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.util.BuildVersionUtil
import com.qiscus.sdk.chat.core.util.QiscusNumberUtil


/**
 * Created on : 2019-11-08
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class PNUtil {

    companion object {

        @JvmStatic
        var pnBuilder: PNBuilder = PNBuilder()

        @JvmStatic
        fun showPn(context: Context, qiscusComment: QMessage) {
            if (MultichannelConst.qiscusCore()?.dataStore?.isContains(qiscusComment)!!) {
                return
            }
            MultichannelConst.qiscusCore()?.dataStore?.addOrUpdate(qiscusComment)

            val lastActivity = MultichannelConst.qiscusCore()?.cacheManager?.lastChatActivity!!
            val isChatRoomActive = (lastActivity.first!! && lastActivity.second == qiscusComment.chatRoomId)

            val isMyMessage = qiscusComment.isMyComment(MultichannelConst.qiscusCore()?.qiscusAccount?.id)

            val isNotShowSystemMessage = !QiscusMultichannelWidget.instance.getConfig().isShowSystemMessage()
                    && qiscusComment.type == QMessage.Type.SYSTEM_EVENT

            if (isChatRoomActive || isMyMessage || isNotShowSystemMessage) return

            val notificationChannelId = getNotificationId()
            var activeNotification: Notification? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                for (notification in notificationManager.activeNotifications) {
                    if (notification.id == QiscusNumberUtil.convertToInt(qiscusComment.chatRoomId)) {
                        activeNotification = notification.notification
                    }
                }
                if (BuildVersionUtil.isOreoOrHigher()) {
                    val notificationChannel = NotificationChannel(
                        notificationChannelId, "Chat", NotificationManager.IMPORTANCE_HIGH
                    )
                    notificationChannel.enableLights(true)
                    notificationChannel.lightColor = pnBuilder.notificationLightColor
                    notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    notificationChannel.setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .build()
                    )

                    notificationManager.createNotificationChannel(notificationChannel)
                }
            }

            notificationBuilder(
                context, qiscusComment, notificationChannelId, activeNotification
            )
        }

        private fun getNotificationId() =
            MultichannelConst.qiscusCore()?.apps?.packageName + ".qiscus.sdk.notification.channel"

        private fun setupAction(context: Context, qiscusComment: QMessage): PendingIntent {
            return PendingIntent.getActivity(
                context,
                QiscusNumberUtil.convertToInt(qiscusComment.chatRoomId),
                NotificationTrampolineActivity.generateIntent(context, qiscusComment),
                getContentFlags()
            )
        }

        private fun getContentFlags(): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else PendingIntent.FLAG_UPDATE_CURRENT
        }

        private fun messageStyle(
            qiscusComment: QMessage, roomName: String, activeNotification: Notification?, context: Context
        ): NotificationCompat.MessagingStyle? {
            val style: NotificationCompat.MessagingStyle? = if (activeNotification != null) {
                NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(
                    activeNotification
                )
            } else {
                val userName = createUserNamePerson(qiscusComment, roomName)
                val person = Person.Builder()
                    .setName(userName)
                    .setKey(qiscusComment.chatRoomId.toString())
                    .build()

                NotificationCompat.MessagingStyle(person)
                    .setGroupConversation(true)
                    .setConversationTitle(userName)
            }

            val user = Person.Builder()
                .setName(qiscusComment.sender.name)
                .setIcon(getAvatar(qiscusComment, context))
                .setKey(qiscusComment.sender.id)
                .build()

            if (!qiscusComment.isMyComment(MultichannelConst.qiscusCore()?.qiscusAccount?.id)) {
                var message: String = qiscusComment.text
                if (messageFromFileSend(message)) message = "upload file"
                style!!.addMessage(message, qiscusComment.timestamp.time, user)
            }
            return style
        }

        private fun createUserNamePerson(qiscusComment: QMessage, roomName: String): String {
            var userName: String = roomName
            if (userName.isEmpty()) {
                userName =
                    if (qiscusComment.getType() === QMessage.Type.SYSTEM_EVENT) "System" else qiscusComment.sender.name
            }
            return userName
        }

        private fun messageFromFileSend(message: String): Boolean {
            var fileNameEndIndex = message.trim { it <= ' ' }.lastIndexOf("[/file]")
            if (fileNameEndIndex == -1) {
                fileNameEndIndex = message.trim { it <= ' ' }.lastIndexOf("[/sticker]")
            }
            return fileNameEndIndex > -1
        }

        private fun getAvatar(qiscusComment: QMessage, context: Context): IconCompat? {
            var avatar: Bitmap? =
                if (qiscusComment.type !== QMessage.Type.SYSTEM_EVENT) ImageUtils.getBitmapNotifFromURL(
                    qiscusComment.sender.avatarUrl
                ) else {
                    ImageUtils.getBitmapFromVectorDrawable(
                        ContextCompat.getDrawable(context, pnBuilder.icDefaultAvatar)
                    )
                }

            if (avatar == null) {
                avatar = ImageUtils.getBitmapFromVectorDrawable(
                    ContextCompat.getDrawable(context, R.drawable.ic_avatar)
                )
            }
            return if (avatar != null) IconCompat.createWithBitmap(avatar) else null
        }

        private fun notificationBuilder(
            context: Context, qiscusComment: QMessage, notificationId: String, activeNotification: Notification?
        ) {
            val qChatRoom: QChatRoom? =
                MultichannelConst.qiscusCore()?.dataStore?.getChatRoom(qiscusComment.chatRoomId)
            if (qChatRoom == null) return

            @SuppressLint("LaunchActivityFromNotification")
            val notifBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(context, notificationId)
                    .setContentTitle(qChatRoom.name)
                    .setContentIntent(setupAction(context, qiscusComment))
                    .setContentText(qiscusComment.text)
                    .setSmallIcon(pnBuilder.icNotification)
                    .setStyle(messageStyle(qiscusComment, qChatRoom.name, activeNotification, context))
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setGroup("CHAT_NOTIF_" + qiscusComment.chatRoomId)
                    .setAutoCancel(true)
                    .setLights(pnBuilder.notificationLightColor, 200, 200)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

            NotificationManagerCompat.from(context)
                .notify(
                    notificationId,
                    QiscusNumberUtil.convertToInt(qiscusComment.chatRoomId),
                    notifBuilder.build()
                )
        }
    }

    class PNBuilder(
        @DrawableRes val icNotification: Int = R.drawable.ic_notification,
        @DrawableRes val icDefaultAvatar: Int = R.drawable.ic_qiscus_white,
        @ColorInt val notificationLightColor: Int = Color.GREEN,
        val parentActivityClass: Class<*>? = null
    )

}