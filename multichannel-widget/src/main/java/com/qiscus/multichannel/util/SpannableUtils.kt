package com.qiscus.multichannel.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.util.PatternsCompat
import com.qiscus.multichannel.ui.webview.WebViewHelper
import java.util.regex.Matcher

class SpannableUtils(private val context: Context, private val spanListener: ClickSpan.OnSpanListener) {

    @SuppressLint("DefaultLocale", "RestrictedApi")
    fun setUpLinks(text: String) {
        val matcher: Matcher = PatternsCompat.AUTOLINK_WEB_URL.matcher(text)
        var start: Int
        var end: Int

        while (matcher.find()) {
            start = matcher.start()

            if (start > 0 && text[start - 1] == '@') continue
            end = matcher.end()

            clickify(text, start, end)
        }
    }

    private fun clickify(text: String, start: Int, end: Int) {
        val span = ClickSpan(handleClick(text, start, end))

        if (start == -1) return

        val s: SpannableString = SpannableString.valueOf(text)
        s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanListener.onSpanResult(s)
    }

    private fun handleClick(text: CharSequence, start: Int, end: Int) = object : ClickSpan.OnClickListener {
        override fun onClick() {
            var url = text.substring(start, end)
            if (!url.startsWith("http")) {
                url = "http://$url"
            }
            WebViewHelper.launchUrl(context, Uri.parse(url))
        }
    }

    class ClickSpan(private val listener: OnClickListener) :
        ClickableSpan() {

        interface OnClickListener {
            fun onClick()
        }
        interface OnSpanListener {
            fun onSpanResult(spanText: SpannableString)
        }

        override fun onClick(widget: View) {
            listener.onClick()
        }

    }
}