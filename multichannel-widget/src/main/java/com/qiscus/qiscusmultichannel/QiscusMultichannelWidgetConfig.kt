package com.qiscus.qiscusmultichannel

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.DrawableRes
import com.qiscus.qiscusmultichannel.util.MultichannelNotificationListener

/**
 * Created on : 05/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class QiscusMultichannelWidgetConfig {

    private var notificationIcon: Int = R.drawable.ic_notification
    private var enableLog: Boolean = false
    private var multichannelNotificationListener: MultichannelNotificationListener? = null
    private var enableNotification: Boolean = true

    private lateinit var sharedPreferences: SharedPreferences

    fun prepare(context: Context) {
        this.sharedPreferences =
            context.getSharedPreferences("qiscus_multichannel_config", Context.MODE_PRIVATE)
    }

    fun getPref(): SharedPreferences {
        return sharedPreferences
    }

    private fun edit(): SharedPreferences.Editor {
        return getPref().edit()
    }

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
        edit().putBoolean("isSessional", isSessional).apply()
    }

    internal fun setRoomTitle(roomTitle: String?) {
        edit().putString("roomTitle", roomTitle).apply()
    }

    internal fun setRoomSubtitle(subtitleType: RoomSubtitle, roomSubtitle: String?) {
        edit().putString("subtitleType", subtitleType.toString()).apply()
        edit().putString(
            "roomSubtitle",
            if (subtitleType == RoomSubtitle.EDITABLE) roomSubtitle else null
        ).apply()
    }

    internal fun setRoomSubtitle(subtitleType: RoomSubtitle) {
        if (subtitleType == RoomSubtitle.EDITABLE) {
            edit().putString("subtitleType", RoomSubtitle.EDITABLE.toString()).apply()
        } else {
            edit().putString("subtitleType", subtitleType.toString()).apply()
        }
    }

    internal fun setAvatar(avatarConfig: Avatar) {
        edit().putString("avatarConfig", avatarConfig.toString()).apply()
    }

    internal fun setShowSystemMessage(isHidden: Boolean) {
        edit().putBoolean("hideUIEvent", isHidden).apply()
    }

    internal fun isEnableLog() = enableLog

    internal fun isEnableNotification() = enableNotification

    internal fun getNotificationListener() = multichannelNotificationListener

    internal fun getNotificationIcon(): Int = this.notificationIcon

    internal fun isSessional() = getPref().getBoolean("isSessional", false)

    internal fun getRoomTitle(): String? = getPref().getString("roomTitle", null)

    internal fun getRoomSubtitle(): String? = getPref().getString("roomSubtitle", null)

    internal fun getRoomSubtitleType(): RoomSubtitle = RoomSubtitle.valueOf(
        getPref().getString(
            "subtitleType",
            RoomSubtitle.EDITABLE.toString()
        )!!
    )

    internal fun isShowSystemMessage(): Boolean = getPref().getBoolean("hideUIEvent", false)

    internal fun isAvatarActived(): Boolean = Avatar.valueOf(
        getPref().getString(
            "avatarConfig",
            Avatar.ENABLE.toString()
        )!!
    ) == Avatar.ENABLE

    enum class RoomSubtitle {
        ENABLE, DISABLE, EDITABLE
    }

    enum class Avatar {
        ENABLE, DISABLE
    }

}