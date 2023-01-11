package com.qiscus.multichannel.data.repository

import com.qiscus.multichannel.data.model.user.UserProperties
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.data.model.QiscusNonce
import org.json.JSONObject

/**
 * Created on : 05/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
interface ChatroomRepository {
    fun sendMessage(roomId: Long, message: QMessage, onSuccess: (QMessage) -> Unit, onError: (Throwable) -> Unit)

    fun publishCustomEvent(roomId: Long, data: JSONObject)

    fun subscribeCustomEvent(roomId: Long)

    fun loginMultichannel(userId: String?, avatar: String?, extras: String?, userProp: List<UserProperties>?, onSuccess: (QiscusNonce) -> Unit, onError: (Throwable) -> Unit)
}