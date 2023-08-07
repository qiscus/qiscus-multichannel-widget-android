package com.qiscus.multichannel.ui.webview

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

/**
 * Created on : 14/02/20
 * Author     : arioki
 * Name       : Yoga Setiawan
 * GitHub     : https://github.com/arioki
 */

object WebViewHelper {
    fun launchUrl(context: Context, uri: Uri) {
        val packageName = CustomTabsHelper.getPackageNameToUse(context)
        val builder = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        if (packageName == null) {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", uri.toString())
            context.startActivity(intent)
        } else {
            builder.intent.setPackage(packageName)
            builder.launchUrl(context, uri)
        }
    }
}