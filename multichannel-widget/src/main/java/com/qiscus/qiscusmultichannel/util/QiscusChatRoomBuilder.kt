package com.qiscus.qiscusmultichannel.util

import android.content.Context
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetConfig.Avatar
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetConfig.RoomSubtitle
import com.qiscus.qiscusmultichannel.ui.chat.ChatRoomActivity
import com.qiscus.qiscusmultichannel.ui.loading.LoadingActivity
import com.qiscus.sdk.chat.core.data.model.QChatRoom

/**
 * Created on : 05/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class QiscusChatRoomBuilder internal constructor(private val multichannelWidget: MultichanelChatWidget) {

    private var showLoading: Boolean = false

    /**
     * update config
     */
    fun showLoadingWhenInitiate(showLoading: Boolean) = apply {
        this.showLoading = showLoading
    }

    fun setRoomTitle(roomTitle: String?) = apply {
        multichannelWidget.getConfig().setRoomTitle(roomTitle)
    }

    fun setRoomSubtitle(subtitleType: RoomSubtitle, roomSubtitle: String?) = apply {
        multichannelWidget.getConfig().setRoomSubtitle(subtitleType, roomSubtitle)
    }

    fun setRoomSubtitle(subtitleType: RoomSubtitle) = apply {
        multichannelWidget.getConfig().setRoomSubtitle(subtitleType)
    }

    fun setAvatar(avatarConfig: Avatar) = apply {
        multichannelWidget.getConfig().setAvatar(avatarConfig)
    }

    fun setShowSystemMessage(isHidden: Boolean) = apply {
        multichannelWidget.getConfig().setShowSystemMessage(isHidden)
    }

    fun setSessional(isSessional: Boolean) = apply {
        multichannelWidget.getConfig().setSessional(isSessional)
    }

    /**
     * open chat
     */
    fun startChat(context: Context) {
        startChat(context, null)
    }

    fun startChat(context: Context, initiateCallback: InitiateCallback?) {
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
                context.startActivity(ChatRoomActivity.generateIntent(context, it))
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