package com.qiscus.qiscusmultichannel.util

import android.content.Context
import com.qiscus.sdk.chat.core.custom.data.model.QiscusComment

/**
 * Created on : 2020-03-02
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
interface MultichannelNotificationListener {

    fun handleMultichannelListener(context: Context?, qiscusComment: QiscusComment?)
}