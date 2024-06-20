package com.qiscus.multichannel.sample.widget.service

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.qiscus.multichannel.QiscusMultichannelWidget
import com.qiscus.multichannel.sample.widget.QiscusMultiChatEngine.Companion.MULTICHANNEL_CORE
import com.qiscus.multichannel.sample.widget.SampleApp
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil


/**
 * Created on : 26/03/20
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class FirebaseServices : FirebaseMessagingService() {

    private val qiscusMultiChatEngine = SampleApp.instance.qiscusMultiChatEngine

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        QiscusMultichannelWidget.instance.registerDeviceToken(
            qiscusMultiChatEngine.get(MULTICHANNEL_CORE), newToken
        )
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (QiscusMultichannelWidget.instance.isMultichannelMessage(
                remoteMessage, qiscusMultiChatEngine.getAll()
            )
        ) {
            Log.e("debug", "notif")
            return
        }
    }

    // call this function every time open the app
    fun registerDeviceToken() {
        val qiscusCore = qiscusMultiChatEngine.get(MULTICHANNEL_CORE)
        val token: String? = qiscusCore.fcmToken
        if (token != null) {
            FirebaseMessaging.getInstance().deleteToken()
                .addOnCompleteListener {
                    // need remove token before register again
                    qiscusCore.removeDeviceToken(token)
                    getTokenFcm(qiscusCore)
                }
                .addOnFailureListener {
                    qiscusCore.registerDeviceToken(token)
                }
        } else {
            getTokenFcm(qiscusCore)
        }
    }


    private fun getTokenFcm(qiscusCore: QiscusCore) {
        // delay to get valid token from firebase
        QiscusAndroidUtil.runOnBackgroundThread({
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener OnCompleteListener@{ task: Task<String?> ->
                    if (!task.isSuccessful) {
                        Log.e("Qiscus", "getCurrentDeviceToken Failed : " + task.exception)
                        return@OnCompleteListener
                    }

                    if (task.isSuccessful && task.result != null) {
                        val currentToken = task.result
                        currentToken?.let {
                            QiscusMultichannelWidget.instance.registerDeviceToken(qiscusCore, it)
                        }
                    }
                }
        }, 2000)
    }

}