package com.qiscus.multichannel

import android.app.Application
import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import com.qiscus.jupuk.Jupuk
import com.qiscus.multichannel.data.local.QiscusChatLocal
import com.qiscus.multichannel.data.local.QiscusSessionLocal
import com.qiscus.multichannel.data.model.user.User
import com.qiscus.multichannel.data.model.user.UserProperties
import com.qiscus.multichannel.ui.chat.ChatRoomActivity
import com.qiscus.multichannel.util.MultichanelChatWidget
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.multichannel.util.PNUtil
import com.qiscus.multichannel.util.QiscusChatRoomBuilder
import com.qiscus.multichannel.util.SecureSession
import com.qiscus.multichannel.util.SecureSessionImpl
import com.qiscus.nirmana.Nirmana
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.model.QAccount
import com.qiscus.sdk.chat.core.data.model.QChatRoom
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
class QiscusMultichannelWidget private constructor(
    application: Application,
    qiscusCore: QiscusCore,
    appId: String,
    private val component: QWidgetComponent,
    private var config: QiscusMultichannelWidgetConfig,
    private var color: QiscusMultichannelWidgetColor,
    localPrefKey: String
) : MultichanelChatWidget {

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
                QiscusMultichannelWidgetComponent().create(config.isEnableLog()),
                config,
                color,
                localPrefKey,
            )
            return INSTANCE!!
        }

    }

    private val sessionSecure: SecureSession
    private val chatRoomBuilder: QiscusChatRoomBuilder
    private var user: User? = null

    init {
        MultichannelConst.setQiscusCore(qiscusCore)
        qiscusCore.setup(application, appId, localPrefKey)
        setCoreConfig(qiscusCore)
        Nirmana.init(application)
        Jupuk.init(application)
        config.prepare(application)
        sessionSecure = SecureSessionImpl(component, config)
        chatRoomBuilder = QiscusChatRoomBuilder(this, sessionSecure)
    }

    private fun setCoreConfig(qiscusCore : QiscusCore) {
        qiscusCore.chatConfig
            .setEnableFcmPushNotification(true)
            .setNotificationListener { context, qiscusComment ->

                if (!config.isEnableNotification()) {
                    return@setNotificationListener
                }

                if (config.getNotificationListener() != null) {
                    config.getNotificationListener()!!
                        .handleMultichannelListener(context, qiscusComment)
                    return@setNotificationListener
                }

                if (context != null && qiscusComment != null) {
                    PNUtil.showPn(context, qiscusComment)
                }
            }
            .enableDebugMode(config.isEnableLog())
    }

    override fun getComponent(): QWidgetComponent = component

    override fun getConfig(): QiscusMultichannelWidgetConfig = config

    override fun getColor(): QiscusMultichannelWidgetColor = color

    internal fun getSecureSession(): SecureSession = sessionSecure

    /**
     *
     */
    override fun getNonce(onSuccess: (QiscusNonce) -> Unit, onError: (Throwable) -> Unit) {
        MultichannelConst.qiscusCore()!!.api
            .jwtNonce
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onSuccess, onError)
    }

    override fun getAppId(): String {
        return MultichannelConst.qiscusCore()!!.appId
    }

    override fun registerDeviceToken(qiscusCore: QiscusCore, token: String) {
        qiscusCore.registerDeviceToken(token)
    }

    override fun firebaseMessagingUtil(remoteMessage: RemoteMessage) {
        MultichannelConst.qiscusCore()!!.firebaseMessagingUtil.handleMessageReceived(remoteMessage)
    }

    override fun updateUser(
        username: String, avatarUrl: String, extras: JSONObject?,
        onSuccess: (QAccount?) -> Unit,
        onError: (Throwable?) -> Unit
    ) {
        MultichannelConst.qiscusCore()!!.updateUser(
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

    override fun hasSetupUser(): Boolean = MultichannelConst.qiscusCore()!!.hasSetupUser()

    override fun getQiscusAccount(): QAccount {
        return MultichannelConst.qiscusCore()!!.qiscusAccount
    }

    override fun clearUser() {
        MultichannelConst.qiscusCore()!!.clearUser()
        QiscusChatLocal.clearPreferences()
    }

    override fun setUser(
        userId: String,
        name: String,
        avatar: String,
        userProperties: Map<String, String>?,
        extras: JSONObject
    ) {
        this.user = User(userId, name, avatar, QiscusSessionLocal.getSessionId(userId), userProperties, extras)
    }

    override fun isLoggedIn(): Boolean {
        try {
            MultichannelConst.qiscusCore()!!.qiscusAccount?.let {
                return true
            }
        } catch (e: Exception) {
            // ignored
        }
        return false
    }

    override fun userCheck(onSuccess: (User, MutableList<UserProperties>) -> Unit) {
        if (user != null) {
            val userProp: MutableList<UserProperties> = ArrayList()
            user!!.userProperties?.let { properties ->
                for ((k, v) in properties) {
                    val obj = UserProperties(k, v)
                    userProp.add(obj)
                }
            }

            onSuccess(user!!, userProp)
        } else {
            throw RuntimeException("please set user first")
        }
    }

    override fun openChatRoomById(
        context: Context,
        roomId: Long,
        clearTaskActivity: Boolean,
        onError: (Throwable) -> Unit
    ) {
        openChatRoomById(context, roomId, null, false, clearTaskActivity, onError)
    }

    override fun openChatRoomById(
        context: Context,
        roomId: Long,
        qMessage: QMessage?,
        isAutoSendMessage: Boolean,
        clearTaskActivity: Boolean,
        onError: (Throwable) -> Unit
    ) {
        openChatRoomById(roomId, {
            openChatRoom(
                context, it, qMessage, isAutoSendMessage, clearTaskActivity, onError
            )
        }, {
            onError(it)
        })
    }

    override fun openChatRoomById(
        roomId: Long,
        onSuccess: (QChatRoom) -> Unit,
        onError: (Throwable) -> Unit
    ) {

        if (!hasSetupUser()) {
            onError(Throwable("Please set user first!"))
            return
        }

        if (QiscusSessionLocal.isInitiate()) {
            getSecureSession().goToChatroom(QiscusChatLocal.getRoomId(), onSuccess, onError)
        } else {
            val userId = QiscusChatLocal.getUserId()
            var userName = QiscusChatLocal.getUsername()

            if (userName.isNullOrEmpty()
                && MultichannelConst.qiscusCore()!!.dataStore.getChatRoom(roomId) != null
            ) {
                userName = MultichannelConst.qiscusCore()!!.dataStore.getChatRoom(roomId).name
            }

            getSecureSession().initiateChat(
                userName,
                userId,
                QiscusChatLocal.getAvatar(),
                QiscusSessionLocal.getSessionId(userId),
                QiscusChatLocal.getExtras(),
                QiscusChatLocal.getUserProps(),
                {
                    getSecureSession().goToChatroom(QiscusChatLocal.getRoomId(), onSuccess, onError)
                }, onError
            )
        }
    }

    override fun openChatRoom(context: Context) {
        openChatRoom(context, true)
    }

    override fun openChatRoom(
        context: Context,
        qChatRoom: QChatRoom,
        qMessage: QMessage?,
        isAutoSendMessage: Boolean,
        clearTaskActivity: Boolean,
        onError: (Throwable) -> Unit
    ) {
        if (!hasSetupUser()) {
            onError(Throwable("Please set user first!"))
            return
        }

        if (QiscusSessionLocal.isInitiate()) {
            ChatRoomActivity.generateIntent(
                context,
                qChatRoom,
                qMessage,
                isAutoSendMessage,
                false,
                clearTaskActivity
            )
        } else {
            onError.invoke(Throwable("Please, initiate chat first!"))
        }
    }

    override fun openChatRoom(context: Context, clearTaskActivity: Boolean) {
        openChatRoomById(
            context,
            QiscusChatLocal.getRoomId(),
            clearTaskActivity
        ) { throwable ->
            throwable.printStackTrace()
        }
    }

    override fun isMultichannelMessage(
        remoteMessage: RemoteMessage,
        qiscusCores: MutableList<QiscusCore>
    ): Boolean {
        MultichannelConst.setAllQiscusCore(qiscusCores)
        try {
            remoteMessage.data["payload"]?.let {
                val msg = JSONObject(it).get("room_options").toString()
                if (JSONObject(msg).get("app_code") == getAppId()) {
                    MultichannelConst.qiscusCore()!!.firebaseMessagingUtil.handleMessageReceived(
                        remoteMessage
                    )
                    return true
                }
            }
        } catch (e: Exception) {
            return false
        }

        return false
    }

    fun initiateChat(): QiscusChatRoomBuilder = chatRoomBuilder

}
