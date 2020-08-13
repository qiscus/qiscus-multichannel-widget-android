package com.qiscus.qiscusmultichannel.ui.loading

import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.data.model.UserProperties
import com.qiscus.qiscusmultichannel.util.QiscusChatLocal
import com.qiscus.sdk.chat.core.data.model.QChatRoom

/**
 * Created on : 04/03/20
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class LoadingPresenter {

    private var view: LoadingView? = null

    fun attachView(view: LoadingView) {
        this.view = view
    }

    fun detach() {
        this.view = null
    }

    fun initiateChat(username: String, userId: String, avatar: String?, extras: String, userProp: List<UserProperties>) {
        if (QiscusChatLocal.getRoomId() == 0L) {
            MultichannelWidget.instance.loginMultiChannel(username, userId, avatar, extras, userProp, {
                openRoomById()
            }, {
               view?.onError(it.localizedMessage)
            })
        } else {
            openRoomById()
        }
    }

    fun openRoomById() {
        MultichannelWidget.instance.openChatRoomById(QiscusChatLocal.getRoomId(), {
            view?.onSuccess(it)
        },{
            view?.onError(it.localizedMessage)
        })
    }

    interface LoadingView {
        fun onError(message: String)

        fun onSuccess(room: QChatRoom)
    }
}