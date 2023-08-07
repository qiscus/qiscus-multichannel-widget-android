package com.qiscus.multichannel.ui.chat.viewholder

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.SpannableString
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.request.RequestOptions
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.multichannel.util.*
import com.qiscus.nirmana.Nirmana
import com.qiscus.sdk.chat.core.data.model.QMessage
import org.json.JSONObject

/**
 * Created on : 28/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
@SuppressLint("NewApi")
class ReplyVH(
    itemView: View,
    config: QiscusMultichannelWidgetConfig,
    color: QiscusMultichannelWidgetColor,
    private val listener: CommentsAdapter.ItemViewListener?,
    private val viewType: Int
) : BaseViewHolder(itemView, config, color), SpannableUtils.ClickSpan.OnSpanListener {

    private var origin: QMessage? = null
    private val qiscusAccount = MultichannelConst.qiscusCore()!!.qiscusAccount
    private val containerReply = itemView.findViewById<LinearLayoutCompat>(R.id.container_reply)
    private val tvChat = itemView.findViewById<TextView>(R.id.tv_chat)
    private val tvRepliedUsername = itemView.findViewById<TextView>(R.id.tv_replied_username)
    private val tvRepliedMessage = itemView.findViewById<TextView>(R.id.tv_replied_message)
    private val ivPlay = itemView.findViewById<ImageView>(R.id.iv_play)
    private val imgRepliedImage = itemView.findViewById<ImageView>(R.id.img_replied_image)
    private val vsReply = itemView.findViewById<RelativeLayout>(R.id.vs_reply)
    private val spannableUtils: SpannableUtils

    init {
        spannableUtils = SpannableUtils(itemView.context, this)
        val backgroundColor: Int
        val colorText: Int

        if (viewType == CommentsAdapter.TYPE_MY_REPLY) {
            backgroundColor = color.getRightBubbleColor()
            colorText = color.getRightBubbleTextColor()
        } else {
            backgroundColor = color.getLeftBubbleColor()
            colorText = color.getLeftBubbleTextColor()
        }

        containerReply.background = ResourceManager.getTintDrawable(
            ContextCompat.getDrawable(
                itemView.context,
                R.drawable.qiscus_rounded_chat_bg_mc
            ), backgroundColor
        )
        tvChat.setTextColor(colorText)
        if (BuildVersionProviderUtil.get().isSamesOrAbove(Build.VERSION_CODES.LOLLIPOP)) {
            ivPlay.imageTintList = ColorStateList.valueOf(color.getNavigationColor())
        }

        vsReply.setOnClickListener { v ->
            origin?.let {
                listener?.onItemReplyClick(v, it)
            }
        }
    }

    override fun getChatFrom(): Drawable? =
        if (viewType == CommentsAdapter.TYPE_MY_REPLY) ResourceManager.IC_CHAT_FROM_ME else ResourceManager.IC_CHAT_FROM

    override fun bind(comment: QMessage) {
        super.bind(comment)
        origin = comment.replyTo

        tvRepliedUsername.text =
            if (qiscusAccount.id == origin?.sender?.id) itemView.context.getString(R.string.qiscus_you_mc) else origin?.sender?.name

        tvChat.text = comment.text

        spannableUtils.setUpLinks(comment.text.lowercase())

        when (origin?.type) {
            QMessage.Type.IMAGE, QMessage.Type.VIDEO -> {
                val obj = JSONObject(origin!!.payload)
                imgRepliedImage.visibility = View.VISIBLE

                ivPlay.visibility =
                    if (origin?.type == QMessage.Type.IMAGE) View.GONE else View.VISIBLE

                tvRepliedMessage.text =
                    if (obj.getString("caption") == "") {
                        if (origin?.type == QMessage.Type.IMAGE) "Image" else "Video"
                    } else obj.getString("caption")

                Nirmana.getInstance().get()
                    .load(origin?.attachmentUri)
                    .thumbnail(0.1f)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.qiscus_image_placeholder)
                            .error(R.drawable.qiscus_image_placeholder)
                            .skipMemoryCache(false)
                    )
                    .into(imgRepliedImage)
            }
            QMessage.Type.FILE -> {
                imgRepliedImage.visibility = View.VISIBLE
                ivPlay.visibility = View.GONE

                tvRepliedMessage.text = MultichannelQMessageUtils.getFileName(origin?.text)

                imgRepliedImage.setImageDrawable(
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
                ivPlay.visibility = View.GONE
                imgRepliedImage.visibility = View.GONE
                tvRepliedMessage.text = origin?.text
            }
        }
    }

    override fun onSpanResult(spanText: SpannableString) {
        tvChat.text = spanText
    }

    /*@SuppressLint("DefaultLocale", "RestrictedApi")
    private fun setUpLinks() {
        val text = tvChat.text.toString().toLowerCase()
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
        val text: CharSequence = tvChat.text.toString()
        val span = ClickSpan(listener)
        if (start == -1) {
            return
        }
        if (text is Spannable) {
            text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val s: SpannableString = SpannableString.valueOf(text)
            s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            tvChat.text = s
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

    }*/
}