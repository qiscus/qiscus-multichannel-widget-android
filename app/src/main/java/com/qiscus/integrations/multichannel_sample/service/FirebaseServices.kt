package com.qiscus.integrations.multichannel_sample.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.qiscus.qiscusmultichannel.MultichannelWidget

/**
 * Created on : 26/03/20
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class FirebaseServices : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        MultichannelWidget.instance.registerDeviceToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        if (MultichannelWidget.instance.isMultichannelMessage(p0)) {
            Log.e("debug", "notif")
            return
        }
    }
}