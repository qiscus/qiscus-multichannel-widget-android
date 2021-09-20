package com.qiscus.qiscusmultichannel.data.repository.impl

import com.qiscus.qiscusmultichannel.data.local.QiscusChatLocal
import com.qiscus.qiscusmultichannel.data.model.user.UserProperties
import com.qiscus.qiscusmultichannel.data.repository.ChatroomRepository
import com.qiscus.qiscusmultichannel.util.MultichannelConst
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.data.model.QiscusNonce
import org.json.JSONObject
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created on : 05/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class ChatroomRepositoryImpl : ChatroomRepository {

    fun sendMessage(
        roomId: Long,
        message: QMessage,
        onSuccess: (QMessage) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        /*val qAccount: QAccount = MultichannelConst.qiscusCore()?.qiscusAccount!!
        val qUser = QUser()
        qUser.avatarUrl = qAccount.avatarUrl
        qUser.id = qAccount.id
        qUser.extras = qAccount.extras
        qUser.name = qAccount.name
        message.sender = qUser*/

        if (message.type == QMessage.Type.TEXT && message.text.trim().isEmpty()) {
            onError(Throwable("message can't empty"))
        }

        MultichannelConst.qiscusCore()?.api?.sendMessage(message)
            ?.doOnSubscribe { MultichannelConst.qiscusCore()?.dataStore?.addOrUpdate(message) }
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
        MultichannelConst.qiscusCore()?.pusherApi?.publishCustomEvent(roomId, data)
    }

    fun subscribeCustomEvent(
        roomId: Long
    ) {
        MultichannelConst.qiscusCore()?.pusherApi?.subsribeCustomEvent(roomId)
    }

    fun loginMultichannel(
        userId: String?,
        avatar: String?,
        extras: String?,
        userProp: List<UserProperties>?,
        onSuccess: (QiscusNonce) -> Unit,
        onError: (Throwable) -> Unit
    ) {

        MultichannelConst.qiscusCore()?.api?.jwtNonce
            ?.doOnNext {
                QiscusChatLocal.saveExtras(extras)
                QiscusChatLocal.saveUserProps(userProp)
                QiscusChatLocal.saveUserId(userId)
                QiscusChatLocal.saveAvatar(avatar)
            }
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                onSuccess(it)
            }, {
                onError(it)
            })
    }
}