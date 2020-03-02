package com.qiscus.qiscusmultichannel

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import com.qiscus.jupuk.Jupuk
import com.qiscus.nirmana.Nirmana
import com.qiscus.qiscusmultichannel.ui.chat.ChatRoomActivity
import com.qiscus.qiscusmultichannel.util.PNUtil
import com.qiscus.qiscusmultichannel.util.QiscusChatLocal
import com.qiscus.sdk.chat.core.custom.QiscusCore
import com.qiscus.sdk.chat.core.custom.data.model.NotificationListener
import com.qiscus.sdk.chat.core.custom.data.model.QiscusAccount
import com.qiscus.sdk.chat.core.custom.data.model.QiscusComment
import com.qiscus.sdk.chat.core.custom.data.model.QiscusNonce
import com.qiscus.sdk.chat.core.custom.data.remote.QiscusApi
import com.qiscus.sdk.chat.core.custom.util.QiscusFirebaseMessagingUtil
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
                            //throw RuntimeException("Please init Qiscus Chat first!")
                        }
                    }
                }

                return INSTANCE!!
            }

        @JvmStatic
        fun init(application: Application, applicationId: String) {
            init(application, applicationId, MultichannelWidgetConfig)
        }

        @JvmStatic
        fun init(
            application: Application,
            applicationId: String,
            config: MultichannelWidgetConfig
        ) {
            INSTANCE = MultichannelWidget(MultichannelWidgetComponent())
            QiscusCore.setup(application, applicationId)
            QiscusCore.getChatConfig()
                .setEnableFcmPushNotification(true)
                .setNotificationListener { context, qiscusComment ->

                    if (config.multichannelNotificationListener != null) {
                        config.getNotificationListener()
                            ?.handleMultichannelListener(context, qiscusComment)
                        return@setNotificationListener
                    }

                    if (context != null && qiscusComment != null) {
                        PNUtil.showPn(context, qiscusComment)
                    }
                }
                .enableDebugMode(config.isEnableLog())
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

        fun loginMultiChannel(
            name: String,
            userId: String,
            onSuccess: (QiscusAccount) -> Unit,
            onError: (Throwable) -> Unit
        ) {
            instance.component.chatroomRepository.getNonce(name, userId) {
                it.data.roomId?.toLong()?.let {
                    QiscusChatLocal.setRoomId(it)
                }
                it.data.identityToken?.let {
                    QiscusCore.setUserWithIdentityToken(it,
                        object : QiscusCore.SetUserListener {
                            override fun onSuccess(qiscusAccount: QiscusAccount) {
                                onSuccess(qiscusAccount)
                            }

                            override fun onError(throwable: Throwable) {
                                onError(throwable)
                            }
                        })
                }
            }
        }
    }

    /**
     *
     */
    fun getNonce(onSuccess: (QiscusNonce) -> Unit, onError: (Throwable) -> Unit) {
        QiscusApi.getInstance()
            .getJWTNonce()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onSuccess, onError)
    }

    /**
     *
     */
    fun setUser(token: String, onSuccess: (QiscusAccount) -> Unit, onError: (Throwable) -> Unit) {
        QiscusCore.setUserWithIdentityToken(token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onSuccess, onError)
    }

    /**
     * abaikan
     */
    fun setUser(
        userId: String,
        userKey: String,
        username: String,
        onSuccess: (QiscusAccount) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        QiscusCore.setUser(userId, userKey)
            .withUsername(username)
            .save()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onSuccess(it)
            }, onError)


    }

    fun updateUser(
        username: String, avatarUrl: String, extras: JSONObject?,
        onSuccess: (QiscusAccount?) -> Unit,
        onError: (Throwable?) -> Unit
    ) {
        QiscusCore.updateUser(username, avatarUrl, extras,
            object : QiscusCore.SetUserListener {
                override fun onSuccess(qiscusAccount: QiscusAccount?) {
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

    private fun hasSetupUser(): Boolean = QiscusCore.hasSetupUser()


    fun openChatRoomById(
        context: Context,
        roomId: Long,
        onError: (Throwable) -> Unit
    ) {
        if (!hasSetupUser()) {
            onError(Throwable("Please set user first"))
        }
        QiscusApi.getInstance().getChatRoomInfo(roomId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                ChatRoomActivity.generateIntent(context, it)
            }, {
                onError(it)
            })
    }

    fun getQiscusAccount(): QiscusAccount {
        return QiscusCore.getQiscusAccount()
    }

    fun openChatRoomMultichannel() {
        openChatRoomById(
            application.applicationContext,
            QiscusChatLocal.getRoomId()
        ) {
            it
        }
    }

    fun initiateChat(context: Context, name: String, userId: String, onError: (Throwable) -> Unit) {

        if (QiscusChatLocal.getRoomId() == 0L) {
            loginMultiChannel(name, userId, {
                openChatRoomById(context, QiscusChatLocal.getRoomId()) {
                    onError(it)
                }
            }, {
                onError(it)
            })
        } else {
            openChatRoomById(context, QiscusChatLocal.getRoomId()) {
                onError(it)
            }
        }
    }

    fun firebaseMessagingUtil(remoteMessage: RemoteMessage) {
        QiscusFirebaseMessagingUtil.handleMessageReceived(remoteMessage)
    }

    fun getAppId(): String {
        return QiscusCore.getAppId()
    }

    fun isMultichannelMessage(remoteMessage: RemoteMessage): Boolean {
        try {
            val msg = JSONObject(remoteMessage.data.get("payload")).get("room_options").toString()
            if (JSONObject(msg).get("app_code") == appId) {
                QiscusFirebaseMessagingUtil.handleMessageReceived(remoteMessage)
                return true
            }

        } catch (e: Exception) {
            return false
        }

        return false

    }

    fun registerDeviceToken(token: String) {
        QiscusCore.registerDeviceToken(token)
    }
}
