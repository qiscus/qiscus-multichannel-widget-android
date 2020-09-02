package com.qiscus.qiscusmultichannel

import android.app.Application
import android.content.Context
import android.content.Intent
import com.google.firebase.messaging.RemoteMessage
import com.qiscus.jupuk.Jupuk
import com.qiscus.nirmana.Nirmana
import com.qiscus.qiscusmultichannel.data.model.UserProperties
import com.qiscus.qiscusmultichannel.ui.chat.ChatRoomActivity
import com.qiscus.qiscusmultichannel.ui.loading.LoadingActivity
import com.qiscus.qiscusmultichannel.util.Const
import com.qiscus.qiscusmultichannel.util.PNUtil
import com.qiscus.qiscusmultichannel.util.QiscusChatLocal
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.model.QAccount
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.data.model.QiscusNonce
import com.qiscus.sdk.chat.core.util.QiscusFirebaseMessagingUtil
import org.json.JSONObject
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created on : 05/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class MultichannelWidget constructor(val component: MultichannelWidgetComponent) {

    companion object {

        @Volatile
        private var INSTANCE: MultichannelWidget? = null

        var config: MultichannelWidgetConfig = MultichannelWidgetConfig

        lateinit var application: Application
        private var appId: String = ""

        @JvmStatic
        val instance: MultichannelWidget
            get() {
                if (INSTANCE == null) {
                    synchronized(MultichannelWidget::class.java) {
                        if (INSTANCE == null) {
                            throw RuntimeException("Please init Qiscus Chat first!")
                        }
                    }
                }

                return INSTANCE!!
            }

        @JvmStatic
        fun setup(application: Application, qiscusCore: QiscusCore, applicationId: String, localPrefKey: String) {
            setup(application, qiscusCore, applicationId, MultichannelWidgetConfig, localPrefKey)
        }

        @JvmStatic
        fun setup(
            application: Application,
            qiscusCore: QiscusCore,
            applicationId: String,
            config: MultichannelWidgetConfig,
            localPrefKey : String
        ) {
            Const.setQiscusCore(qiscusCore)
            INSTANCE = MultichannelWidget(MultichannelWidgetComponent())
            Const.qiscusCore()?.setup(application, applicationId, localPrefKey)
            Const.qiscusCore()?.getChatConfig()
                ?.setEnableFcmPushNotification(true)
                ?.setNotificationListener { context, qiscusComment ->

                    if (!config.isEnableNotification()) {
                        return@setNotificationListener
                    }

                    if (config.multichannelNotificationListener != null) {
                        config.getNotificationListener()
                            ?.handleMultichannelListener(context, qiscusComment)
                        return@setNotificationListener
                    }

                    if (context != null && qiscusComment != null) {
                        PNUtil.showPn(context, qiscusComment)
                    }
                }
                ?.enableDebugMode(config.isEnableLog())
            this.config = config
            this.application = application
            Nirmana.init(application)
            Jupuk.init(application)
            this.appId = applicationId
        }

        @JvmStatic
        fun updateConfig(config: MultichannelWidgetConfig) {
            this.config = config
        }
    }

    fun loginMultiChannel(
        name: String,
        userId: String,
        avatar: String?,
        extras: String,
        userProperties: List<UserProperties>,
        onSuccess: (QAccount) -> Unit,
        onError: (Throwable) -> Unit
    ) {

        instance.component.chatroomRepository.initiateChat(name, userId, avatar, extras, userProperties, {
            it.data.roomId?.toLong()?.let { id ->
                QiscusChatLocal.setRoomId(id)
            }
            it.data.identityToken?.let {
                Const.qiscusCore()?.setUserWithIdentityToken(it,
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

    /**
     *
     */
    fun setUser(token: String, onSuccess: (QAccount) -> Unit, onError: (Throwable) -> Unit) {
        Const.qiscusCore()?.setUserWithIdentityToken(token)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(onSuccess, onError)
    }

    /**
     * abaikan
     */
    fun setUser(
        userId: String,
        userKey: String,
        username: String,
        onSuccess: (QAccount) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        Const.qiscusCore()?.setUser(userId, userKey)
            ?.withUsername(username)
            ?.save()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                onSuccess(it)
            }, onError)


    }

    fun updateUser(
        username: String, avatarUrl: String, extras: JSONObject?,
        onSuccess: (QAccount?) -> Unit,
        onError: (Throwable?) -> Unit
    ) {
        Const.qiscusCore()?.updateUser(username, avatarUrl, extras,
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

    fun hasSetupUser(): Boolean =  Const.qiscusCore()?.hasSetupUser()!!


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
        return  Const.qiscusCore()?.getQiscusAccount()!!
    }

    fun openChatRoomMultichannel(clearTaskActivity: Boolean) {
        openChatRoomById(
            application.applicationContext,
            QiscusChatLocal.getRoomId(),
            clearTaskActivity
        ) {
            it
        }
    }

    fun initiateChat(context: Context, name: String, userId: String, avatar: String, extras: JSONObject?, userProperties: Map<String, String>?) {
        var userProp: MutableList<UserProperties> = ArrayList()
        userProperties?.let {
             for ((k,v) in it) {
                 val obj = UserProperties(k, v)
                 userProp.add(obj)
             }
         }

        LoadingActivity.generateIntent(context, name, userId, avatar, extras, userProp)
    }

    fun firebaseMessagingUtil(remoteMessage: RemoteMessage) {
        Const.qiscusCore()?.firebaseMessagingUtil?.handleMessageReceived(remoteMessage)
    }

    fun getAppId(): String {
        return Const.qiscusCore()?.getAppId()!!
    }

    fun isMultichannelMessage(remoteMessage: RemoteMessage, qiscusCores: MutableList<QiscusCore>): Boolean {
        Const.setAllQiscusCore(qiscusCores)
        try {
            val msg = JSONObject(remoteMessage.data.get("payload")).get("room_options").toString()
            if (JSONObject(msg).get("app_code") == appId) {
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
}
