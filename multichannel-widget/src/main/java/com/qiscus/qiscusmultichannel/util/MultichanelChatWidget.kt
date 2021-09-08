package com.qiscus.qiscusmultichannel.util

import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetColor
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetComponent
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.data.model.user.User
import com.qiscus.qiscusmultichannel.data.model.user.UserProperties
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.model.QAccount
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.data.model.QiscusNonce
import org.json.JSONObject


/**
 * Created on : 06/09/21
 * Author     : mmnuradityo
 * GitHub     : https://github.com/mmnuradityo
 */
interface MultichanelChatWidget {

    fun getComponent(): QiscusMultichannelWidgetComponent

    fun getConfig(): QiscusMultichannelWidgetConfig

    fun getColor(): QiscusMultichannelWidgetColor

    fun updateConfig(config: QiscusMultichannelWidgetConfig)

    fun updateColor(color: QiscusMultichannelWidgetColor)

    fun getNonce(onSuccess: (QiscusNonce) -> Unit, onError: (Throwable) -> Unit)

    fun getAppId(): String

    fun registerDeviceToken(qiscusCore: QiscusCore, token: String)

    fun firebaseMessagingUtil(remoteMessage: RemoteMessage)

    fun updateUser(
        username: String, avatarUrl: String, extras: JSONObject?,
        onSuccess: (QAccount?) -> Unit,
        onError: (Throwable?) -> Unit
    )

    fun hasSetupUser(): Boolean

    fun getQiscusAccount(): QAccount

    fun clearUser()

    fun setUser(userId: String, name: String, avatar: String)

    fun setUser(
        userId: String,
        name: String,
        avatar: String,
        userProperties: Map<String, String>? = null
    )

    fun loginMultiChannel(
        name: String?,
        userId: String?,
        avatar: String?,
        extras: String?,
        userProperties: List<UserProperties>?,
        onSuccess: (QAccount) -> Unit,
        onError: (Throwable) -> Unit
    )

    fun isLoggedIn(): Boolean

    fun userCheck(onSuccess: (User, MutableList<UserProperties>) -> Unit)

    fun openChatRoom(context: Context)

    fun openChatRoom(context: Context, clearTaskActivity: Boolean)

    fun openChatRoomById(roomId: Long, onSuccess: (QChatRoom) -> Unit, onError: (Throwable) -> Unit)

    fun openChatRoomById(
        context: Context,
        roomId: Long,
        clearTaskActivity: Boolean,
        onError: (Throwable) -> Unit
    )

    fun isMultichannelMessage(
        remoteMessage: RemoteMessage,
        qiscusCores: MutableList<QiscusCore>
    ): Boolean

}