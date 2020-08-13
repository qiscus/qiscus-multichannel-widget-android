package com.qiscus.qiscusmultichannel.data.repository.impl

import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.MultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.data.model.DataInitialChat
import com.qiscus.qiscusmultichannel.data.model.UserProperties
import com.qiscus.qiscusmultichannel.data.repository.ChatroomRepository
import com.qiscus.qiscusmultichannel.data.repository.response.ResponseInitiateChat
import com.qiscus.qiscusmultichannel.util.Const
import com.qiscus.qiscusmultichannel.util.QiscusChatLocal
import com.qiscus.sdk.chat.core.data.model.QAccount
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.data.model.QUser
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
        message: QMessage,
        onSuccess: (QMessage) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val qAccount: QAccount = Const.qiscusCore()?.getQiscusAccount()!!
        val qUser = QUser()
        qUser.avatarUrl = qAccount.avatarUrl
        qUser.id = qAccount.id
        qUser.extras = qAccount.extras
        qUser.name = qAccount.name
        message.setSender(qUser)

        Const.qiscusCore()?.api?.sendMessage(message)
            ?.doOnSubscribe { Const.qiscusCore()?.getDataStore()?.addOrUpdate(message) }
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                if (it.chatRoomId == roomId) {
                    onSuccess(it)
                }
            }, { throwable ->
                throwable.printStackTrace()
                if (message.chatRoomId == roomId) {
                    onError(throwable)
                }
            })
    }

    fun publishCustomEvent(
        roomId: Long,
        data: JSONObject
    ) {
        Const.qiscusCore()?.pusherApi?.publishCustomEvent(roomId, data)
    }

    fun subscribeCustomEvent(
        roomId: Long
    ) {
        Const.qiscusCore()?.pusherApi?.subsribeCustomEvent(roomId)
    }

    fun initiateChat(
        name: String,
        userId: String,
        avatar: String?,
        extras: String,
        userProp: List<UserProperties>,
        responseInitiateChat: (ResponseInitiateChat) -> Unit,
        onError: (Throwable) -> Unit
    ) {

        Const.qiscusCore()?.api?.jwtNonce
            ?.doOnNext {
                QiscusChatLocal.saveExtras(extras)
                QiscusChatLocal.saveUserProps(userProp)
                QiscusChatLocal.saveUserId(userId)
                QiscusChatLocal.saveAvatar(avatar)
            }
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                MultichannelWidget.instance.component.qiscusChatRepository.initiateChat(
                    DataInitialChat(
                        Const.qiscusCore()?.getAppId()!!,
                        userId,
                        name,
                        avatar,
                        it.nonce,
                        null,
                        extras,
                        userProp
                    ), {
                        it.data.isSessional?.let {
                            MultichannelWidgetConfig.setSessional(true)
                        }
                        responseInitiateChat(it)
                    }, {
                        onError(it)
                    })
            }, {
                onError(it)
            })
    }
}