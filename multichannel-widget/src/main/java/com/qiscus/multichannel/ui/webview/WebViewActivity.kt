package com.qiscus.multichannel.ui.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.qiscus.multichannel.databinding.ActivityWebViewMcBinding
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created on : 14/02/20
 * Author     : arioki
 * Name       : Yoga Setiawan
 * GitHub     : https://github.com/arioki
 */

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewMcBinding
    private lateinit var webViewClient: WebViewClient
    private var mWebViewClient: WebViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            binding.tvTitle.text = view.title
            binding.tvUrl.text = view.url
            //view.loadUrl("javascript:window.android.onUrlChange(window.location.href);")
        }

        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            binding.tvTitle.text = view.title
            binding.tvUrl.text = view.url
            if (url.startsWith("intent://") && url.contains("scheme=http")) {
                val regexBkp: Pattern = Pattern.compile("intent://(.*?)#")
                val regexMatcherBkp: Matcher = regexBkp.matcher(url)
                return if (regexMatcherBkp.find()) {
                    val bkpUrl = regexMatcherBkp.group(1)
                    val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://$bkpUrl"))
                    startActivity(myIntent)
                    finish()
                    true
                } else {
                    false
                }
            }
            return false
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewMcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webViewClient = WebViewClient()
        val url = intent.getStringExtra("url")
        url?.let { binding.webview.loadUrl(it) }

        with(binding.webview.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowContentAccess = true
            allowFileAccess = true
        }

        binding.webview.webViewClient = mWebViewClient
        binding.btnBack.setOnClickListener { finish() }
    }

}
