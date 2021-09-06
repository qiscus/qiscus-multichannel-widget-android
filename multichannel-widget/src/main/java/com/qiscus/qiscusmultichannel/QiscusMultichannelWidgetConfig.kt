package com.qiscus.qiscusmultichannel

import androidx.annotation.DrawableRes
import com.qiscus.qiscusmultichannel.util.MultichannelNotificationListener

/**
 * Created on : 05/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class QiscusMultichannelWidgetConfig {

    private var notificationIcon: Int = R.drawable.ic_notification
    private var avatarConfig: Avatar = Avatar.ENABLE
    private var subtitleType: RoomSubtitle = RoomSubtitle.EDITABLE
    private var enableLog: Boolean = false
    private var isSessional: Boolean = false
    private var multichannelNotificationListener: MultichannelNotificationListener? = null
    private var enableNotification: Boolean = true
    private var roomTitle: String? = null
    private var roomSubtitle: String? = null
    private var hardcodedAvatar: String? = null
    private var hideUIEvent: Boolean = false // hide system event
    private var videoPreviewSend: Boolean = true // show video preview on send confirmation activity

    fun setEnableLog(enableLog: Boolean) = apply { this.enableLog = enableLog }
    fun isEnableLog() = enableLog
    fun isSessional() = isSessional
    fun setSessional(isSessional: Boolean) = apply { this.isSessional = isSessional }
    fun setNotificationListener(multichannelNotificationListener: MultichannelNotificationListener?) =
        apply { this.multichannelNotificationListener = multichannelNotificationListener }

    fun getNotificationListener() = multichannelNotificationListener
    fun setEnableNotification(enableNotification: Boolean) =
        apply { this.enableNotification = enableNotification }

    fun setNotificationIcon(@DrawableRes iconId: Int) =
        apply { this.notificationIcon = iconId }

    fun isEnableNotification() = enableNotification
    fun setRoomTitle(roomTitle: String?) = apply { this.roomTitle = roomTitle }
    fun getRoomTitle(): String? = roomTitle
    fun setRoomSubtitle(subtitleType: RoomSubtitle, roomSubtitle: String?) = apply {
        this.subtitleType = subtitleType
        this.roomSubtitle = if (subtitleType == RoomSubtitle.EDITABLE) roomSubtitle else null
    }

    fun setRoomSubtitle(subtitleType: RoomSubtitle) = apply {
        if (subtitleType == RoomSubtitle.EDITABLE) this.subtitleType = RoomSubtitle.ENABLE
        else this.subtitleType = subtitleType
    }

    fun setAvatar(avatarConfig: Avatar) = apply {
        this.avatarConfig = avatarConfig
    }

    fun getRoomSubtitle(): String? = roomSubtitle
    fun getRoomSubtitleType(): RoomSubtitle = subtitleType
    fun setHideUIEvent(isHidden: Boolean) = apply { this.hideUIEvent = isHidden }
    fun getHideUIEvent(): Boolean = hideUIEvent
    fun setVideoPreviewOnSend(isShow: Boolean) = apply { this.videoPreviewSend = isShow }
    fun getVideoPreviewOnSend(): Boolean = videoPreviewSend
    fun isAvatarActived(): Boolean = avatarConfig == Avatar.ENABLE

    @Deprecated("move using configuration from dashboard")
    fun setHardcodedAvatar(hardcodedAvatar: String) =
        apply { this.hardcodedAvatar = hardcodedAvatar }

    @Deprecated("move using configuration from dashboard")
    fun getHardcodedAvatar(): String? = hardcodedAvatar
    fun getNotificationIcon(): Int = this.notificationIcon

    enum class RoomSubtitle {
        ENABLE, DISABLE, EDITABLE
    }

    enum class Avatar {
        ENABLE, DISABLE
    }

}