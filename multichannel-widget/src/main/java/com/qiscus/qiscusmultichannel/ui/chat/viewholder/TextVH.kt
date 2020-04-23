package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.annotation.SuppressLint
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.core.util.PatternsCompat
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.util.DateUtil
import com.qiscus.qiscusmultichannel.ui.webView.WebViewHelper
import com.qiscus.sdk.chat.core.custom.QiscusCore
import com.qiscus.sdk.chat.core.custom.data.model.QiscusComment
import java.util.regex.Matcher


/**
 * Created on : 22/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class TextVH(itemView: View) : BaseViewHolder(itemView) {
    private val message: TextView = itemView.findViewById(R.id.message)
    private val sender: TextView? = itemView.findViewById(R.id.sender)
    private val dateOfMessage: TextView? = itemView.findViewById(R.id.dateOfMessage)

    override fun bind(comment: QiscusComment) {
        super.bind(comment)
        message.text = comment.message
        val chatRoom = QiscusCore.getDataStore().getChatRoom(comment.roomId)

        if (chatRoom != null) {
            sender?.visibility = if (chatRoom.isGroup) View.GONE else View.VISIBLE
        }
        dateOfMessage?.text = DateUtil.toFullDate(comment.time)
        setUpLinks()
    }

    override fun setNeedToShowDate(showDate: Boolean) {
        dateOfMessage?.visibility = if (showDate) View.VISIBLE else View.GONE
    }

    @SuppressLint("DefaultLocale")
    private fun setUpLinks() {
        val text = message.text.toString().toLowerCase()
        val matcher: Matcher = PatternsCompat.AUTOLINK_WEB_URL.matcher(text)
        while (matcher.find()) {
            val start: Int = matcher.start()
            if (start > 0 && text[start - 1] == '@') {
                continue
            }
            val end: Int = matcher.end()
            clickify(start, end, object : ClickSpan.OnClickListener {
                override fun onClick() {
                    var url = text.substring(start, end)
                    if (!url.startsWith("http")) {
                        url = "http://$url"
                    }
                    WebViewHelper.launchUrl(itemView.context, Uri.parse(url))
                }
            })
        }
    }

    private fun clickify(start: Int, end: Int, listener: ClickSpan.OnClickListener) {
        val text: CharSequence = message.text.toString()
        val span = ClickSpan(listener)
        if (start == -1) {
            return
        }
        if (text is Spannable) {
            (text as Spannable).setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val s: SpannableString = SpannableString.valueOf(text)
            s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            message.text = s
        }
    }

    private class ClickSpan(private val listener: OnClickListener?) :
        ClickableSpan() {

        interface OnClickListener {
            fun onClick()
        }

        override fun onClick(widget: View) {
            listener?.onClick()
        }

    }
}