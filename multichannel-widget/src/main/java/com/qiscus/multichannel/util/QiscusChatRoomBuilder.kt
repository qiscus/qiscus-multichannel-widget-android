package com.qiscus.multichannel.util

import android.content.Context
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig.Avatar
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig.RoomSubtitle
import com.qiscus.multichannel.data.local.QiscusChatLocal
import com.qiscus.multichannel.ui.chat.ChatRoomActivity
import com.qiscus.multichannel.ui.loading.LoadingActivity
import com.qiscus.sdk.chat.core.data.model.QChatRoom

/**
 * Created on : 05/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class QiscusChatRoomBuilder internal constructor(private val multichannelWidget: MultichanelChatWidget) {

    private var roomTitle: String? = null
    private var roomSubtitle: String? = null
    private var subtitleType: RoomSubtitle = RoomSubtitle.ENABLE
    private var avatarConfig: Avatar = Avatar.DISABLE
    private var isShowSystemMessage: Boolean = false
    private var isSessional: Boolean = false
    private var showLoading: Boolean = false
    private var channelId: Int = 0

    /**
     * update config
     */
    fun showLoadingWhenInitiate(showLoading: Boolean) = apply {
        this.showLoading = showLoading
    }

    fun setChannelId(channelId: Int) = apply {
        this.channelId = channelId
    }

    fun setRoomTitle(roomTitle: String?) = apply {
        this.roomTitle = roomSubtitle
    }

    fun setRoomSubtitle(subtitleType: RoomSubtitle, roomSubtitle: String?) = apply {
        this.subtitleType = subtitleType
        this.roomSubtitle = roomSubtitle
    }

    fun setRoomSubtitle(subtitleType: RoomSubtitle) = apply {
        this.subtitleType = subtitleType
    }

    fun setAvatar(avatarConfig: Avatar) = apply {
        this.avatarConfig = avatarConfig
    }

    fun setShowSystemMessage(isShowing: Boolean) = apply {
        this.isShowSystemMessage = isShowing
    }

    fun setSessional(isSessional: Boolean) = apply {
        this.isSessional = isSessional
    }

    /**
     * open chat
     */
    private fun saveConfig() {
        multichannelWidget.getConfig().apply {
            setRoomTitle(roomTitle)
            setRoomSubtitle(subtitleType, roomSubtitle)
            setAvatar(avatarConfig)
            setChannelId(channelId)
            setSessional(isSessional)
            setShowSystemMessage(isShowSystemMessage)
        }
    }

    fun startChat(context: Context) {
        startChat(context, null)
    }

    fun startChat(context: Context, initiateCallback: InitiateCallback?) {
        saveConfig()
        multichannelWidget.clearUser()
        multichannelWidget.userCheck { user, userProp ->
            if (this.showLoading) {
                LoadingActivity.generateIntent(
                    context,
                    user.name,
                    user.userId,
                    user.avatar,
                    null,
                    userProp
                )
            } else {
                initiateCallback?.onProgress()
                multichannelWidget.loginMultiChannel(
                    user.name,
                    user.userId,
                    user.avatar,
                    "{}",
                    userProp,
                    {
                        loadChatRoom(context, initiateCallback)
                    },
                    { throwable ->
                        initiateCallback?.onError(throwable)
                    })
            }
        }
    }

    private fun loadChatRoom(context: Context, initiateCallback: InitiateCallback?) {
        multichannelWidget.openChatRoomById(QiscusChatLocal.getRoomId(), {
            if (initiateCallback != null) {
                initiateCallback.onSuccess(it)
            } else {
                ChatRoomActivity.generateIntent(context, it, false)
            }
        }, {
            initiateCallback?.onError(it)
        })
    }

    interface InitiateCallback {
        fun onProgress()
        fun onSuccess(qChatRoom: QChatRoom)
        fun onError(throwable: Throwable)
    }

}