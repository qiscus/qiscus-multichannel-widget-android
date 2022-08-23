package com.qiscus.multichannel.ui.loading

import com.qiscus.multichannel.data.local.QiscusChatLocal
import com.qiscus.multichannel.data.model.user.UserProperties
import com.qiscus.multichannel.util.MultichanelChatWidget
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.data.model.QMessage

/**
 * Created on : 04/03/20
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class LoadingPresenter(private val multichannelWidget: MultichanelChatWidget) {

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
        extras: String?,
        userProp: List<UserProperties>?
    ) {
        if (!QiscusChatLocal.getHasMigration()) {
            QiscusChatLocal.setHasMigration(true)
            QiscusChatLocal.setRoomId(0)
        }

        multichannelWidget.loginMultiChannel(
            username,
            userId,
            avatar,
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
        multichannelWidget.openChatRoomById(QiscusChatLocal.getRoomId(), {
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