package com.qiscus.qiscusmultichannel

import android.app.Application
import android.content.Context
import android.content.Intent
import com.google.firebase.messaging.RemoteMessage
import com.qiscus.jupuk.Jupuk
import com.qiscus.nirmana.Nirmana
import com.qiscus.qiscusmultichannel.data.model.user.User
import com.qiscus.qiscusmultichannel.data.model.user.UserProperties
import com.qiscus.qiscusmultichannel.ui.chat.ChatRoomActivity
import com.qiscus.qiscusmultichannel.util.Const
import com.qiscus.qiscusmultichannel.util.PNUtil
import com.qiscus.qiscusmultichannel.util.QiscusChatLocal
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.model.QAccount
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.data.model.QiscusNonce
import org.json.JSONObject
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created on : 05/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class QiscusMultichannelWidget private constructor(
    application: Application,
    qiscusCore: QiscusCore,
    appId: String,
    val component: QiscusMultichannelWidgetComponent,
    var config: QiscusMultichannelWidgetConfig,
    var color: QiscusMultichannelWidgetColor,
    localPrefKey: String
) {

    companion object {

        @Volatile
        private var INSTANCE: QiscusMultichannelWidget? = null

        @JvmStatic
        val instance: QiscusMultichannelWidget
            get() {
                if (INSTANCE == null) {
                    synchronized(QiscusMultichannelWidget::class.java) {
                        if (INSTANCE == null) {
                            throw RuntimeException("Please setup QiscusMultichannelWidget Chat first!")
                        }
                    }
                }

                return INSTANCE!!
            }

        @JvmStatic
        fun setup(
            application: Application,
            qiscusCore: QiscusCore,
            applicationId: String,
            localPrefKey: String
        ): QiscusMultichannelWidget = setup(
            application,
            qiscusCore,
            applicationId,
            QiscusMultichannelWidgetConfig(),
            localPrefKey
        )

        @JvmStatic
        fun setup(
            application: Application,
            qiscusCore: QiscusCore,
            applicationId: String,
            config: QiscusMultichannelWidgetConfig,
            localPrefKey: String
        ): QiscusMultichannelWidget = setup(
            application,
            qiscusCore,
            applicationId,
            config,
            QiscusMultichannelWidgetColor(),
            localPrefKey
        )

        @JvmStatic
        fun setup(
            application: Application,
            qiscusCore: QiscusCore,
            applicationId: String,
            config: QiscusMultichannelWidgetConfig,
            color: QiscusMultichannelWidgetColor,
            localPrefKey: String
        ): QiscusMultichannelWidget {
            INSTANCE = QiscusMultichannelWidget(
                application,
                qiscusCore,
                applicationId,
                QiscusMultichannelWidgetComponent(config.isEnableLog()),
                config,
                color,
                localPrefKey
            )
            return INSTANCE!!
        }

    }

    init {
        Const.setQiscusCore(qiscusCore)
        Const.qiscusCore()?.setup(application, appId, localPrefKey)
        setCoreConfig()
        Nirmana.init(application)
        Jupuk.init(application)
    }

    private fun setCoreConfig() {
        Const.qiscusCore()?.chatConfig
            ?.setEnableFcmPushNotification(true)
            ?.setNotificationListener { context, qiscusComment ->

                if (!config.isEnableNotification()) {
                    return@setNotificationListener
                }

                if (config.getNotificationListener() != null) {
                    config.getNotificationListener()
                        ?.handleMultichannelListener(context, qiscusComment)
                    return@setNotificationListener
                }

                if (context != null && qiscusComment != null) {
                    PNUtil.showPn(context, qiscusComment)
                }
            }
            ?.enableDebugMode(config.isEnableLog())
    }


    fun updateConfig(config: QiscusMultichannelWidgetConfig) {
        this.config = config
        setCoreConfig()
    }

    fun updateColor(color: QiscusMultichannelWidgetColor) {
        this.color = color
    }

    fun loginMultiChannel(
        name: String?,
        userId: String?,
        avatar: String?,
        extras: String?,
        userProperties: List<UserProperties>?,
        onSuccess: (QAccount) -> Unit,
        onError: (Throwable) -> Unit
    ) {

        instance.component.chatroomRepository.loginMultichannel(
            name,
            userId,
            avatar,
            extras,
            userProperties,
            config,
            {
                it.data.roomId?.toLong()?.let { id ->
                    QiscusChatLocal.setRoomId(id)
                }
                it.data.identityToken?.let {
                    Const.qiscusCore()?.setUserWithIdentityToken(
                        it,
                        object : QiscusCore.SetUserListener {
                            override fun onSuccess(qiscusAccount: QAccount) {
                                onSuccess(qiscusAccount)
                            }

                            override fun onError(throwable: Throwable) {
                                onError(throwable)
                            }
                        })
                }
            }) {
            onError(it)
        }
    }

    /**
     *
     */
    fun getNonce(onSuccess: (QiscusNonce) -> Unit, onError: (Throwable) -> Unit) {
        Const.qiscusCore()?.api
            ?.jwtNonce
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(onSuccess, onError)
    }

//    /**
//     *
//     */
//    fun setUser(token: String, onSuccess: (QAccount) -> Unit, onError: (Throwable) -> Unit) {
//        Const.qiscusCore()?.setUserWithIdentityToken(token)
//            ?.subscribeOn(Schedulers.io())
//            ?.observeOn(AndroidSchedulers.mainThread())
//            ?.subscribe(onSuccess, onError)
//    }
//
//    /**
//     * abaikan
//     */
//    fun setUser(
//        userId: String,
//        userKey: String,
//        username: String,
//        onSuccess: (QAccount) -> Unit,
//        onError: (Throwable) -> Unit
//    ) {
//        Const.qiscusCore()?.setUser(userId, userKey)
//            ?.withUsername(username)
//            ?.save()
//            ?.subscribeOn(Schedulers.io())
//            ?.observeOn(AndroidSchedulers.mainThread())
//            ?.subscribe({
//                onSuccess(it)
//            }, onError)
//
//
//    }

    fun updateUser(
        username: String, avatarUrl: String, extras: JSONObject?,
        onSuccess: (QAccount?) -> Unit,
        onError: (Throwable?) -> Unit
    ) {
        Const.qiscusCore()?.updateUser(
            username, avatarUrl, extras,
            object : QiscusCore.SetUserListener {
                override fun onSuccess(qiscusAccount: QAccount?) {
                    onSuccess(qiscusAccount)
                }

                override fun onError(throwable: Throwable?) {
                    onError(throwable)
                }
            })
    }

    /**
     *
     */

    fun hasSetupUser(): Boolean = Const.qiscusCore()?.hasSetupUser()!!

    fun openChatRoomById(
        context: Context,
        roomId: Long,
        clearTaskActivity: Boolean,
        onError: (Throwable) -> Unit
    ) {
        if (!hasSetupUser()) {
            onError(Throwable("Please set user first"))
        }

        openChatRoomById(roomId, {
            val intent = ChatRoomActivity.generateIntent(context, it)
            if (clearTaskActivity) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }, {
            onError(it)
        })
    }

    fun openChatRoomById(
        roomId: Long,
        onSuccess: (QChatRoom) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (!hasSetupUser()) {
            onError(Throwable("Please set user first"))
        }
        Const.qiscusCore()?.api?.getChatRoomInfo(roomId)
            ?.doOnNext {
                Const.qiscusCore()?.dataStore?.addOrUpdate(it)
            }
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                onSuccess(it)
            }, {
                onError(it)
            })
    }

    fun getQiscusAccount(): QAccount {
        return Const.qiscusCore()?.qiscusAccount!!
    }

    fun openChatRoom(context: Context) {
        openChatRoom(context, true)
    }

    fun openChatRoom(context: Context, clearTaskActivity: Boolean) {
        openChatRoomById(
            context,
            QiscusChatLocal.getRoomId(),
            clearTaskActivity
        ) { throwable ->
            throwable.printStackTrace()
        }
    }

    fun initiateChat(): QiscusChatRoomBuilder = QiscusChatRoomBuilder()

    fun firebaseMessagingUtil(remoteMessage: RemoteMessage) {
        Const.qiscusCore()?.firebaseMessagingUtil?.handleMessageReceived(remoteMessage)
    }

    fun getAppId(): String {
        return Const.qiscusCore()?.appId!!
    }

    fun isMultichannelMessage(
        remoteMessage: RemoteMessage,
        qiscusCores: MutableList<QiscusCore>
    ): Boolean {
        Const.setAllQiscusCore(qiscusCores)
        try {
            val msg = JSONObject(remoteMessage.data["payload"]).get("room_options").toString()
            if (JSONObject(msg).get("app_code") == getAppId()) {
                Const.qiscusCore()?.firebaseMessagingUtil?.handleMessageReceived(remoteMessage)
                return true
            }

        } catch (e: Exception) {
            return false
        }

        return false

    }

    fun registerDeviceToken(qiscusCore: QiscusCore, token: String) {
        qiscusCore.registerDeviceToken(token)
    }

    fun clearUser(qiscusCore: QiscusCore) {
        qiscusCore.clearUser()
        QiscusChatLocal.clearPreferences()
    }

    private var user: User? = null

    fun setUser(
        userId: String,
        name: String,
        avatar: String,
        userProperties: Map<String, String>? = null
    ) {
        this.user = User(userId, name, avatar, userProperties)
    }

    fun getUser(): User = if (user != null) user!! else {
        throw RuntimeException("please set user firt")
    }

    fun isLoggedIn(): Boolean {
        try {
            Const.qiscusCore()?.qiscusAccount?.let {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

}
