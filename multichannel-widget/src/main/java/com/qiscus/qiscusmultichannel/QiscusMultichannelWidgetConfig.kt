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
    private var hideUIEvent: Boolean = false // hide system event

    fun setEnableLog(enableLog: Boolean) = apply {
        this.enableLog = enableLog
    }

    fun setNotificationListener(multichannelNotificationListener: MultichannelNotificationListener?) =
        apply {
            this.multichannelNotificationListener = multichannelNotificationListener
        }

    fun setEnableNotification(enableNotification: Boolean) = apply {
        this.enableNotification = enableNotification
    }

    fun setNotificationIcon(@DrawableRes iconId: Int) = apply {
        this.notificationIcon = iconId
    }

    internal fun setSessional(isSessional: Boolean) {
        this.isSessional = isSessional
    }

    internal fun setRoomTitle(roomTitle: String?) {
        this.roomTitle = roomTitle
    }

    internal fun setRoomSubtitle(subtitleType: RoomSubtitle, roomSubtitle: String?) {
        this.subtitleType = subtitleType
        this.roomSubtitle = if (subtitleType == RoomSubtitle.EDITABLE) roomSubtitle else null
    }

    internal fun setRoomSubtitle(subtitleType: RoomSubtitle) {
        if (subtitleType == RoomSubtitle.EDITABLE) this.subtitleType = RoomSubtitle.ENABLE
        else this.subtitleType = subtitleType
    }

    internal fun setAvatar(avatarConfig: Avatar) {
        this.avatarConfig = avatarConfig
    }

    internal fun setShowSystemMessage(isHidden: Boolean) {
        this.hideUIEvent = isHidden
    }

    internal fun isEnableLog() = enableLog

    internal fun isEnableNotification() = enableNotification

    internal fun getNotificationListener() = multichannelNotificationListener

    internal fun getNotificationIcon(): Int = this.notificationIcon

    internal fun isSessional() = isSessional

    internal fun getRoomTitle(): String? = roomTitle

    internal fun getRoomSubtitle(): String? = roomSubtitle

    internal fun getRoomSubtitleType(): RoomSubtitle = subtitleType

    internal fun isShowSystemMessage(): Boolean = hideUIEvent

    internal fun isAvatarActived(): Boolean = avatarConfig == Avatar.ENABLE

    enum class RoomSubtitle {
        ENABLE, DISABLE, EDITABLE
    }

    enum class Avatar {
        ENABLE, DISABLE
    }

}