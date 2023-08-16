package com.qiscus.multichannel.ui.loading

import com.qiscus.multichannel.QiscusMultichannelWidget
import com.qiscus.multichannel.data.local.QiscusChatLocal
import com.qiscus.multichannel.data.model.user.UserProperties
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import org.json.JSONObject

/**
 * Created on : 04/03/20
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class LoadingPresenter(private val multichannelWidget: QiscusMultichannelWidget) {

    private var view: LoadingView? = null

    fun attachView(view: LoadingView) {
        this.view = view
    }

    fun detach() {
        this.view = null
    }

    fun initiateChat(
        username: String?,
        userId: String?,
        avatar: String?,
        sessionId: String?,
        extras: JSONObject?,
        userProp: List<UserProperties>?
    ) {
        if (!QiscusChatLocal.getHasMigration()) {
            QiscusChatLocal.setHasMigration(true)
            QiscusChatLocal.setRoomId(0)
        }

        multichannelWidget.getSecureSession()
            .initiateChat(
                username,
                userId,
                avatar,
                sessionId,
                extras,
                userProp,
                {
                    openRoomById()
                },
                {
                    view?.onError(it.localizedMessage)
                })
    }

    fun openRoomById() {
        multichannelWidget.getSecureSession()
            .goToChatroom(QiscusChatLocal.getRoomId(), {
                view?.onSuccess(it)
            }, {
                view?.onError(it.localizedMessage)
            })
    }

    interface LoadingView {
        fun onError(message: String)

        fun onSuccess(room: QChatRoom)
    }
}