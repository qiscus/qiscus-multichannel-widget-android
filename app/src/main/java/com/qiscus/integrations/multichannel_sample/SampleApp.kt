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

        //just 1 in 1 lifecircle
        ConstCore.setCore()

        val configMultichannel: MultichannelWidgetConfig =
            MultichannelWidgetConfig.setEnableLog(BuildConfig.DEBUG)
                .setNotificationListener(null)
                .setRoomTitle("Bot name")
                .setRoomSubtitle("Custom subtitle")
                .setHardcodedAvatar("https://d1edrlpyc25xu0.cloudfront.net/cee-8xj32ozyfbnka0arz/image/upload/XBOSht7_hR/bebi.jpeg")

        MultichannelWidget.setup(this, ConstCore.qiscusCore1(), "cee-8xj32ozyfbnka0arz", configMultichannel, "user1")
        //MultichannelWidget.setup(this, ConstCore.qiscusCore1(), "karm-gzu41e4e4dv9fu3f", configMultichannel, "user1")
    }
}