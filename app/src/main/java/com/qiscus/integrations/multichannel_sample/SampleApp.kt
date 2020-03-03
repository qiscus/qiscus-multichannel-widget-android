package com.qiscus.integrations.multichannel_sample

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.MultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.util.MultichannelNotificationListener
import com.qiscus.sdk.chat.core.custom.data.model.QiscusComment

/**
 * Created on : 2020-02-28
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class SampleApp: MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        val configMultichannel: MultichannelWidgetConfig =
            MultichannelWidgetConfig.setEnableLog(BuildConfig.DEBUG)
                .setNotificationListener(object : MultichannelNotificationListener {
                    override fun handleMultichannelListener(
                        context: Context?,
                        qiscusComment: QiscusComment?
                    ) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                })

        MultichannelWidget.init(this, "your app id", configMultichannel)
    }
}