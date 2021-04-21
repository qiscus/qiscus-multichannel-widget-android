package com.qiscus.qiscusmultichannel.ui.webView

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.qiscus.qiscusmultichannel.R
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_web_view_mc.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created on : 14/02/20
 * Author     : arioki
 * Name       : Yoga Setiawan
 * GitHub     : https://github.com/arioki
 */

class WebViewActivity : AppCompatActivity() {

    private lateinit var webViewClient: WebViewClient
    private var mWebViewClient: WebViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            tv_title.text = view.title
            tv_url.text = view.url
            //view.loadUrl("javascript:window.android.onUrlChange(window.location.href);")
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            tv_title.text = view.title
            tv_url.text = view.url
            if (url.startsWith("intent://") && url.contains("scheme=http")) {
                var bkpUrl: String? = null
                val regexBkp: Pattern = Pattern.compile("intent://(.*?)#")
                val regexMatcherBkp: Matcher = regexBkp.matcher(url)
                if (regexMatcherBkp.find()) {
                    bkpUrl = regexMatcherBkp.group(1)
                    val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://$bkpUrl"))
                    startActivity(myIntent)
                    finish()
                    return true
                } else {
                    return false
                }
            }
            return false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view_mc)

        webViewClient = WebViewClient()
        val url = intent.getStringExtra("url")
        webview.loadUrl(url)

        with(webview.settings) {
            setAppCacheEnabled(true)
            javaScriptEnabled = true
            domStorageEnabled = true
            allowContentAccess = true
            allowFileAccess = true
        }

        webview.webViewClient = mWebViewClient
        btn_back.setOnClickListener { finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        clearFindViewByIdCache()
    }

}
