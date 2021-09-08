package com.qiscus.multichannel.sample.widget.service

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.qiscus.multichannel.sample.widget.QiscusMultiChatEngine.Companion.MULTICHANNEL_CORE
import com.qiscus.multichannel.sample.widget.SampleApp
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidget

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

    fun getCurrentDeviceToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("Qiscus", "getCurrentDeviceToken Failed : " + task.exception)
                    return@OnCompleteListener
                }
                if (task.result != null) {
                    val currentToken = task.result!!.token
                    QiscusMultichannelWidget.instance.registerDeviceToken(
                        qiscusMultiChatEngine.get(MULTICHANNEL_CORE), currentToken
                    )
                }
            })
    }
}