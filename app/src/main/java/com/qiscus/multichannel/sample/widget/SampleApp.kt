package com.qiscus.multichannel.sample.widget

import androidx.multidex.MultiDexApplication
import com.qiscus.multichannel.sample.BuildConfig
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.MultichannelWidgetConfig

/**
 * Created on : 2020-02-28
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class SampleApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        //just 1 in 1 lifecircle
        ConstCore.setCore()

        val configMultichannel: MultichannelWidgetConfig =
            MultichannelWidgetConfig.setEnableLog(BuildConfig.DEBUG)
                .setNotificationListener(null)
                .setRoomTitle("Custom Title")
                .setRoomSubtitle("Custom subtitle")
                .setHardcodedAvatar("https://d1edrlpyc25xu0.cloudfront.net/cee-8xj32ozyfbnka0arz/image/upload/XBOSht7_hR/bebi.jpeg")

        MultichannelWidget.setup(
            this,
            ConstCore.qiscusCore1(),
            "goum-bzlmlpixlodgb6wn",
            configMultichannel,
            "user1"
        )
        //MultichannelWidget.setup(this, ConstCore.qiscusCore1(), "erliv-1vgncdxub60y7y8", configMultichannel, "user1")
    }
}