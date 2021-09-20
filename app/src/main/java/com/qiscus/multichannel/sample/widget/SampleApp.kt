package com.qiscus.multichannel.sample.widget

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.qiscus.multichannel.sample.BuildConfig
import com.qiscus.multichannel.sample.R
import com.qiscus.multichannel.sample.widget.QiscusMultiChatEngine.Companion.MULTICHANNEL_CORE
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidget
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetColor
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.util.MultichannelNotificationListener
import com.qiscus.qiscusmultichannel.util.PNUtil
import com.qiscus.sdk.chat.core.data.model.QMessage

/**
 * Created on : 2020-02-28
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class SampleApp : MultiDexApplication() {

    companion object {
        @Volatile
        private var INSTANCE: SampleApp? = null

        @JvmStatic
        val instance: SampleApp
            get() {
                if (INSTANCE == null) {
                    synchronized(QiscusMultichannelWidget::class.java) {
                        if (INSTANCE == null) {
                            throw RuntimeException("Something wrong!!!")
                        }
                    }
                }

                return INSTANCE!!
            }
    }

    private val appId = "akoop-i0xwcb7spjwzhro" // change with your AppId
    private val localKey = "qiscus_multichannel_user" // change with your localKey

    private val config = QiscusMultichannelWidgetConfig()
        .setEnableLog(BuildConfig.DEBUG)
        .setEnableNotification(true)
        .setNotificationListener(object : MultichannelNotificationListener {

            override fun handleMultichannelListener(context: Context?, qiscusComment: QMessage?) {
                // show your notification here
                if (context != null && qiscusComment != null) {
                    PNUtil.showPn(context, qiscusComment)
                }
            }

        })
        .setNotificationIcon(R.drawable.ic_notification)

    private val color = QiscusMultichannelWidgetColor()
        .setStatusBarColor(R.color.qiscusStatusBar)
        .setNavigationColor(R.color.qiscusNavigation)
        .setNavigationTitleColor(R.color.qiscusNavigationTittle)
        .setBaseColor(R.color.qiscusBackground)
        .setEmptyBacgroundColor(R.color.qiscusEmptyBackground)
        .setEmptyTextColor(R.color.qiscusEmptyText)
        .setTimeBackgroundColor(R.color.qiscusTimeBackground)
        .setTimeLabelTextColor(R.color.qiscusTimeLable)
        .setLeftBubbleColor(R.color.qiscusLeftBubble)
        .setRightBubbleColor(R.color.qiscusRightBubble)
        .setLeftBubbleTextColor(R.color.qiscusLeftTextBubble)
        .setRightBubbleTextColor(R.color.qiscusRightTextBubble)
        .setFieldChatBorderColor(R.color.qiscusFieldChat)
        .setSystemEventTextColor(R.color.qiscusSystemEvent)
        .setSendContainerColor(R.color.qiscusSendContainer)
        .setSendContainerBackgroundColor(R.color.qiscusSendContainerBackground)

    lateinit var qiscusMultichannelWidget: QiscusMultichannelWidget
    lateinit var qiscusMultiChatEngine: QiscusMultiChatEngine

    override fun onCreate() {
        super.onCreate()
        //just 1 in 1 lifecircle
        qiscusMultiChatEngine = QiscusMultiChatEngine()

        qiscusMultichannelWidget = QiscusMultichannelWidget.setup(
            this,
            qiscusMultiChatEngine.get(MULTICHANNEL_CORE),
            appId,
            config,
//            color,
            localKey
        )

        INSTANCE = this
    }

}