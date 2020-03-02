package com.qiscus.qiscusmultichannel.data.repository.impl

import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.MultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.data.model.DataInitialChat
import com.qiscus.qiscusmultichannel.data.repository.ChatroomRepository
import com.qiscus.qiscusmultichannel.data.repository.response.ResponseInitiateChat
import com.qiscus.sdk.chat.core.custom.QiscusCore
import com.qiscus.sdk.chat.core.custom.data.model.QiscusComment
import com.qiscus.sdk.chat.core.custom.data.remote.QiscusApi
import com.qiscus.sdk.chat.core.custom.data.remote.QiscusPusherApi
import org.json.JSONObject
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created on : 05/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class ChatroomRepositoryImpl : ChatroomRepository {

    fun sendComment(
        roomId: Long,
        message: QiscusComment,
        onSuccess: (QiscusComment) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        QiscusApi.getInstance().sendMessage(message)
            .doOnSubscribe { QiscusCore.getDataStore().addOrUpdate(message) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.roomId == roomId) {
                    onSuccess(it)
                }
            }, { throwable ->
                throwable.printStackTrace()
                if (message.roomId == roomId) {
                    onError(throwable)
                }
            })
    }

    fun publishCustomEvent(
        roomId: Long,
        data: JSONObject
    ) {
        QiscusPusherApi.getInstance().publishCustomEvent(roomId, data)
    }

    fun subscribeCustomEvent(
        roomId: Long
    ) {
        QiscusPusherApi.getInstance().subsribeCustomEvent(roomId)
    }

    fun getNonce(
        name: String,
        userId: String,
        responseInitiateChat: (ResponseInitiateChat) -> Unit
    ) {
        QiscusApi.getInstance().jwtNonce
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                MultichannelWidget.instance.component.qiscusChatRepository.getNonce(
                    DataInitialChat(
                        QiscusCore.getAppId(),
                        userId,
                        name,
                        "",
                        it.nonce,
                        "",
                        null
                    ), {
                        it.data.isSessional?.let {
                            MultichannelWidgetConfig.setSessional(true)
                        }
                        responseInitiateChat(it)
                    }, {
                        it
                    })
            }, {
                it
            })
    }
}