package com.qiscus.multichannel.basetest

import android.app.Application
import android.content.Context
import com.qiscus.multichannel.QiscusMultichannelWidget
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.util.MultichannelNotificationListener
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.model.QMessage

class MulchanWidget {

    private val appId: String = "multichannel_app_id"// change with your AppId
    private val localKey: String = "qiscus_multichannel_user" // change with your localKey

    private val config = QiscusMultichannelWidgetConfig()
        .setEnableLog(true)  // change it to false if your app is ready for release
        .setEnableNotification(true)
        .setNotificationListener(object : MultichannelNotificationListener {

            override fun handleMultichannelListener(context: Context?, qiscusComment: QMessage?) {
                // show your notification here
                /*if (context != null && qiscusComment != null) {
                    PNUtil.showPn(context, qiscusComment)
                }*/
            }

        })
        .setNotificationIcon(R.drawable.ic_notification)


    val qiscusMultiChatEngine = QiscusCore()

    fun generateMulchanWidget(application: Application) = QiscusMultichannelWidget.setup(
        application, qiscusMultiChatEngine, appId, config, localKey
    ).apply {
        val userProperties = mapOf(
            "city" to "jogja",
            "job" to "developer"
        )
        setUser("id", "name", "ava", userProperties)
    }

    fun login() {
        qiscusMultiChatEngine.setUserWithIdentityToken("fakeToken")
    }
}