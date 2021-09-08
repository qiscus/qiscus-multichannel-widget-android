package com.qiscus.qiscusmultichannel

import android.app.Application
import android.content.Context
import android.content.Intent
import com.google.firebase.messaging.RemoteMessage
import com.qiscus.jupuk.Jupuk
import com.qiscus.nirmana.Nirmana
import com.qiscus.qiscusmultichannel.data.model.DataInitialChat
import com.qiscus.qiscusmultichannel.data.model.user.User
import com.qiscus.qiscusmultichannel.data.model.user.UserProperties
import com.qiscus.qiscusmultichannel.ui.chat.ChatRoomActivity
import com.qiscus.qiscusmultichannel.util.*
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
    private val component: QiscusMultichannelWidgetComponent,
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
                QiscusMultichannelWidgetComponent(config.isEnableLog()),
                config,
                color,
                localPrefKey
            )
            return INSTANCE!!
        }

    }

    private val chatRoomBuilder: QiscusChatRoomBuilder
    private var user: User? = null

    init {
        MultichannelConst.setQiscusCore(qiscusCore)
        MultichannelConst.qiscusCore()?.setup(application, appId, localPrefKey)
        setCoreConfig()
        Nirmana.init(application)
        Jupuk.init(application)
        chatRoomBuilder = QiscusChatRoomBuilder(this)
    }

    private fun setCoreConfig() {
        MultichannelConst.qiscusCore()?.chatConfig
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

    override fun getComponent(): QiscusMultichannelWidgetComponent = component

    override fun getConfig(): QiscusMultichannelWidgetConfig = config

    override fun getColor(): QiscusMultichannelWidgetColor = color

    override fun updateConfig(config: QiscusMultichannelWidgetConfig) {
        this.config = config
        setCoreConfig()
    }

    override fun updateColor(color: QiscusMultichannelWidgetColor) {
        this.color = color
    }

    override fun loginMultiChannel(
        name: String?,
        userId: String?,
        avatar: String?,
        extras: String?,
        userProperties: List<UserProperties>?,
        onSuccess: (QAccount) -> Unit,
        onError: (Throwable) -> Unit
    ) {

        component.chatroomRepository.loginMultichannel(
            userId,
            avatar,
            extras,
            userProperties,
            {
                component.qiscusChatRepository.initiateChat(
                    DataInitialChat(
                        MultichannelConst.qiscusCore()?.appId!!,
                        userId,
                        name,
                        avatar,
                        it.nonce,
                        null,
                        extras,
                        userProperties
                    ), { response ->
                        response.data.isSessional?.let { sessional ->
                            config.setSessional(sessional)
                        }

                        response.data.roomId?.toLong()?.let { id ->
                            QiscusChatLocal.setRoomId(id)
                        }

                        response.data.identityToken?.let { token ->
                            MultichannelConst.qiscusCore()?.setUserWithIdentityToken(token,
                                object : QiscusCore.SetUserListener {
                                    override fun onSuccess(qiscusAccount: QAccount) {
                                        onSuccess(qiscusAccount)
                                    }

                                    override fun onError(throwable: Throwable) {
                                        onError(throwable)
                                    }
                                })
                        }
                    }, { throwable ->
                        onError(throwable)
                    })
            }) {
            onError(it)
        }
    }

    /**
     *
     */
    override fun getNonce(onSuccess: (QiscusNonce) -> Unit, onError: (Throwable) -> Unit) {
        MultichannelConst.qiscusCore()?.api
            ?.jwtNonce
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(onSuccess, onError)
    }

    override fun getAppId(): String {
        return MultichannelConst.qiscusCore()?.appId!!
    }

    override fun registerDeviceToken(qiscusCore: QiscusCore, token: String) {
        qiscusCore.registerDeviceToken(token)
    }

    override fun firebaseMessagingUtil(remoteMessage: RemoteMessage) {
        MultichannelConst.qiscusCore()?.firebaseMessagingUtil?.handleMessageReceived(remoteMessage)
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

    override fun updateUser(
        username: String, avatarUrl: String, extras: JSONObject?,
        onSuccess: (QAccount?) -> Unit,
        onError: (Throwable?) -> Unit
    ) {
        MultichannelConst.qiscusCore()?.updateUser(
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

    override fun hasSetupUser(): Boolean = MultichannelConst.qiscusCore()?.hasSetupUser()!!

    override fun getQiscusAccount(): QAccount {
        return MultichannelConst.qiscusCore()?.qiscusAccount!!
    }

    override fun clearUser() {
        MultichannelConst.qiscusCore()?.let {
            it.clearUser()
            QiscusChatLocal.clearPreferences()
        }
    }

    override fun setUser(userId: String, name: String, avatar: String) {
        setUser(userId, name, avatar, null)
    }

    override fun setUser(
        userId: String,
        name: String,
        avatar: String,
        userProperties: Map<String, String>?
    ) {
        this.user = User(userId, name, avatar, userProperties)
    }

    override fun isLoggedIn(): Boolean {
        try {
            MultichannelConst.qiscusCore()?.qiscusAccount?.let {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
            throw RuntimeException("please set user firt")
        }
    }

    override fun openChatRoomById(
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

    override fun openChatRoomById(
        roomId: Long,
        onSuccess: (QChatRoom) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (!hasSetupUser()) {
            onError(Throwable("Please set user first"))
        }
        MultichannelConst.qiscusCore()?.api?.getChatRoomInfo(roomId)
            ?.doOnNext {
                MultichannelConst.qiscusCore()?.dataStore?.addOrUpdate(it)
            }
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                onSuccess(it)
            }, {
                onError(it)
            })
    }


    override fun openChatRoom(context: Context) {
        openChatRoom(context, true)
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
            val msg = JSONObject(remoteMessage.data["payload"]).get("room_options").toString()
            if (JSONObject(msg).get("app_code") == getAppId()) {
                MultichannelConst.qiscusCore()?.firebaseMessagingUtil?.handleMessageReceived(
                    remoteMessage
                )
                return true
            }

        } catch (e: Exception) {
            return false
        }

        return false
    }

    fun initiateChat(): QiscusChatRoomBuilder = chatRoomBuilder

}
