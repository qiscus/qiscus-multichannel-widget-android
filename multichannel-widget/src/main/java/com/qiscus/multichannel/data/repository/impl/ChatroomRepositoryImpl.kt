package com.qiscus.multichannel.data.repository.impl

import com.qiscus.multichannel.data.local.QiscusChatLocal
import com.qiscus.multichannel.data.model.user.UserProperties
import com.qiscus.multichannel.data.repository.ChatroomRepository
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.sdk.chat.core.QiscusCore
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

    private fun getQiscusCore() : QiscusCore = MultichannelConst.qiscusCore()!!

   // disable account from sendMessage
   /* val qAccount: QAccount = MultichannelConst.qiscusCore()?.qiscusAccount!!
       val qUser = QUser()
       qUser.avatarUrl = qAccount.avatarUrl
       qUser.id = qAccount.id
       qUser.extras = qAccount.extras
       qUser.name = qAccount.name
       message.sender = qUser*/
    override fun sendMessage(
        roomId: Long,
        message: QMessage,
        onSuccess: (QMessage) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (message.type == QMessage.Type.TEXT && message.text.trim().isEmpty()) {
            onError(Throwable("message can't empty"))
        }

        getQiscusCore().api.sendMessage(message)
                .doOnSubscribe { getQiscusCore().dataStore.addOrUpdate(message) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.chatRoomId == roomId) {
                        onSuccess(message)
                    }
                }, { throwable ->
                    throwable.printStackTrace()
                    if (message.chatRoomId == roomId) {
                        onError(throwable)
                    }
                })
    }

    override fun publishCustomEvent(
        roomId: Long,
        data: JSONObject
    ) {
        getQiscusCore().pusherApi.publishCustomEvent(roomId, data)
    }

    override fun subscribeCustomEvent(
        roomId: Long
    ) {
        getQiscusCore().pusherApi.subsribeCustomEvent(roomId)
    }

    override fun getJwtNonce(
        name: String?,
        userId: String?,
        avatar: String?,
        sessionId: String?,
        extras: String?,
        userProp: List<UserProperties>?,
        onSuccess: (QiscusNonce) -> Unit,
        onError: (Throwable) -> Unit
    ) {

        getQiscusCore().api.jwtNonce
            .doOnNext {
                QiscusChatLocal.saveExtras(extras)
                QiscusChatLocal.saveUserProps(userProp)
                QiscusChatLocal.saveUserId(userId)
                QiscusChatLocal.saveUsername(name)
                QiscusChatLocal.saveAvatar(avatar)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onSuccess(it)
            }, {
                onError(it)
            })
    }
}