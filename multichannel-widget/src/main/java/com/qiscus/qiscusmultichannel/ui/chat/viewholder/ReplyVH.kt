package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.util.PatternsCompat
import com.bumptech.glide.request.RequestOptions
import com.qiscus.nirmana.Nirmana
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetColor
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.ui.chat.CommentsAdapter
import com.qiscus.qiscusmultichannel.ui.webView.WebViewHelper
import com.qiscus.qiscusmultichannel.util.Const
import com.qiscus.qiscusmultichannel.util.ResourceManager
import com.qiscus.sdk.chat.core.data.model.QMessage
import kotlinx.android.synthetic.main.item_my_reply_mc.view.*
import org.json.JSONObject
import java.util.regex.Matcher

/**
 * Created on : 28/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class ReplyVH(
    itemView: View,
    config: QiscusMultichannelWidgetConfig,
    color: QiscusMultichannelWidgetColor,
    private val viewType: Int
) : BaseViewHolder(itemView, config, color) {

    private val qiscusAccount = Const.qiscusCore()?.qiscusAccount!!

    init {
        val backgroundColor: Int
        val colorText: Int

        if (viewType == CommentsAdapter.TYPE_MY_REPLY) {
            backgroundColor = color.getRightBubbleColor()
            colorText = color.getRightBubbleTextColor()
        } else {
            backgroundColor = color.getLeftBubbleColor()
            colorText = color.getLeftBubbleTextColor()
        }

        itemView.container_reply.background = ResourceManager.getTintDrawable(
            ContextCompat.getDrawable(
                itemView.context,
                R.drawable.qiscus_rounded_chat_bg_mc
            ), backgroundColor
        )
        itemView.tv_chat.setTextColor(colorText)
    }

    override fun getChatFrom(): Drawable? =
        if (viewType == CommentsAdapter.TYPE_MY_REPLY) ResourceManager.IC_CHAT_FROM_ME else ResourceManager.IC_CHAT_FROM

    override fun bind(comment: QMessage) {
        super.bind(comment)
        val origin = comment.replyTo

        itemView.tv_replied_username?.text =
            if (qiscusAccount.id == origin.sender.id) itemView.context.getString(R.string.qiscus_you_mc) else origin.sender.name

        itemView.tv_replied_message?.text = origin.text
        itemView.tv_chat.text = comment.text

        setUpLinks()

        when (origin.type) {
            QMessage.Type.TEXT -> {
                itemView.img_replied_image.visibility = View.GONE
            }
            QMessage.Type.IMAGE -> {
                val obj = JSONObject(origin.payload)
                itemView.img_replied_image.visibility = View.VISIBLE
                itemView.tv_replied_message.text =
                    if (obj.getString("caption") == "") "Image" else obj.getString("caption")
                Nirmana.getInstance().get()
                    .load(origin.attachmentUri)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.qiscus_image_placeholder)
                            .error(R.drawable.qiscus_image_placeholder)
                            .dontAnimate()
                    )
                    .into(itemView.img_replied_image)
            }
            QMessage.Type.FILE -> {
                itemView.img_replied_image.visibility = View.VISIBLE
                itemView.img_replied_image.setImageDrawable(
                    ResourceManager.getTintDrawable(
                        ContextCompat.getDrawable(
                            itemView.context,
                            R.drawable.ic_qiscus_doc
                        ), if (viewType == CommentsAdapter.TYPE_MY_TEXT)
                            ContextCompat.getColor(itemView.context, R.color.colorAccent)
                        else color.getNavigationColor()
                    )
                )
            }
            else -> {
                itemView.img_replied_image.visibility = View.GONE
                itemView.tv_replied_message.text = origin.text
            }
        }
    }

    @SuppressLint("DefaultLocale", "RestrictedApi")
    private fun setUpLinks() {
        val text = itemView.tv_chat.text.toString().toLowerCase()
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
        val text: CharSequence = itemView.tv_chat.text.toString()
        val span = ClickSpan(listener)
        if (start == -1) {
            return
        }
        if (text is Spannable) {
            text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val s: SpannableString = SpannableString.valueOf(text)
            s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            itemView.tv_chat.text = s
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