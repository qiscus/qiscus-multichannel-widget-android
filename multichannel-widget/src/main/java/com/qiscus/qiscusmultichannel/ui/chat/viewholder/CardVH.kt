package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.annotation.SuppressLint
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.util.PatternsCompat
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.qiscus.nirmana.Nirmana
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.ui.webView.WebViewHelper
import com.qiscus.sdk.chat.core.data.model.QMessage
import kotlinx.android.synthetic.main.item_card_mc.view.*
import org.json.JSONObject
import java.util.regex.Matcher

/**
 * Created on : 11/02/20
 * Author     : arioki
 * Name       : Yoga Setiawan
 * GitHub     : https://github.com/arioki
 */

class CardVH(itemView: View) : BaseViewHolder(itemView) {

    override fun bind(comment: QMessage) {
        super.bind(comment)
        val data = JSONObject(comment.payload)
        itemView.tv_title.text = data.getString("title")
        itemView.tv_message.text = data.getString("description")

        Nirmana.getInstance().get()
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.ic_qiscus_add_image)
                    .error(R.drawable.ic_qiscus_add_image)
                    .dontAnimate()
                    .transforms(CenterCrop(), RoundedCorners(16))
            )
            .load(data.getString("image"))
            .into(itemView.image)
        setUpLinks()
        try {
            val dataButton = JSONObject(data.getJSONArray("buttons")[0].toString())
            val payload = JSONObject(dataButton.get("payload").toString())
            val url = payload.getString("url")
            val type = dataButton.getString("type")
            val postbackText = dataButton.getString("postback_text")
            itemView.btn_msg.setOnClickListener {
                when (type) {
                    "link" -> WebViewHelper.launchUrl(itemView.context, Uri.parse(url))
                    "postback" -> {
                        val postBackMessage = QMessage.generatePostBackMessage(
                            comment.chatRoomId,
                            postbackText,
                            payload
                        )
                        sendComment(postBackMessage)
                    }
                }

            }
            itemView.btn_msg.text = dataButton.getString("label")
        } catch (e: Exception) {
        }
    }

    private fun sendComment(comment: QMessage) {
        MultichannelWidget.instance.component.chatroomRepository.sendComment(
            comment.chatRoomId,
            comment,
            {
                it
            },
            {
                it
            })
    }

    @SuppressLint("DefaultLocale", "RestrictedApi")
    private fun setUpLinks() {
        val text = itemView.tv_message.text.toString().toLowerCase()
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
        val text: CharSequence = itemView.tv_message.text.toString()
        val span = ClickSpan(listener)
        if (start == -1) {
            return
        }
        if (text is Spannable) {
            (text as Spannable).setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val s: SpannableString = SpannableString.valueOf(text)
            s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            itemView.tv_message.text = s
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