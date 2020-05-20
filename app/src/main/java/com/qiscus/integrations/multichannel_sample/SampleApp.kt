package com.qiscus.integrations.multichannel_sample

import androidx.multidex.MultiDexApplication
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.MultichannelWidgetConfig

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
                .setNotificationListener(null)
                .setRoomTitle("Bot name")

        MultichannelWidget.setup(this, "karm-gzu41e4e4dv9fu3f", configMultichannel)
    }
}