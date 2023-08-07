package com.qiscus.multichannel.ui.chat.viewholder

import android.text.SpannableString
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.multichannel.util.ResourceManager
import com.qiscus.multichannel.util.SpannableUtils
import com.qiscus.nirmana.Nirmana
import com.qiscus.sdk.chat.core.data.model.QMessage
import org.json.JSONObject


/**
 * Created on : 11/02/20
 * Author     : arioki
 * Name       : Yoga Setiawan
 * GitHub     : https://github.com/arioki
 */

class CardVH(
    itemView: View,
    config: QiscusMultichannelWidgetConfig,
    color: QiscusMultichannelWidgetColor,
    private val listener: CommentsAdapter.ItemViewListener?
) : BaseViewHolder(itemView, config, color), ChatButtonView.ChatButtonClickListener, SpannableUtils.ClickSpan.OnSpanListener {

    private val spanleUtils: SpannableUtils
    private var chatRoomId: Long = 0
    private var description: TextView = itemView.findViewById(R.id.description)
    private var title: TextView = itemView.findViewById(R.id.title)
    private var thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
    private var containerBackground: ViewGroup = itemView.findViewById(R.id.containerBackground)
    private var buttonsContainer: ViewGroup = itemView.findViewById(R.id.buttonsContainer)

    init {
        spanleUtils = SpannableUtils(itemView.context, this)
        description.setTextColor(color.getLeftBubbleTextColor())
        title.setTextColor(color.getLeftBubbleTextColor())
    }

    override fun bind(comment: QMessage) {
        super.bind(comment)
        this.chatRoomId = comment.chatRoomId
        val data = JSONObject(comment.payload)
        title.text = data.getString("title")
        description.text = data.getString("description")

        Nirmana.getInstance().get()
            .load(data.getString("image"))
            .thumbnail(0.3f)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.qiscus_image_placeholder)
                    .error(R.drawable.qiscus_image_placeholder)
                    .centerCrop()
                    .transform(CenterCrop(), RoundedCorners(ResourceManager.DIMEN_ROUNDED_IMAGE))
            )
            .into(thumbnail)

        spanleUtils.setUpLinks(description.text.toString())

        ChatButtonView.setUpButtons(
            buttonsContainer,
            data.getJSONArray("buttons"),
            LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
        ) {
            ChatButtonView(itemView.context, color, it)
                .setChatButtonClickListener(this)
                .build()
        }

        containerBackground.background = ResourceManager.getTintDrawable(
            ContextCompat.getDrawable(
                itemView.context,
                R.drawable.qiscus_rounded_chat_bg_mc
            ), color.getLeftBubbleColor()
        )
    }

    private fun sendComment(comment: QMessage) {
        listener?.onSendComment(comment)
    }

    /*@SuppressLint("DefaultLocale", "RestrictedApi")
    private fun setUpLinks() {
        val text = description.text.toString().toLowerCase()
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
        val text: CharSequence = description.text.toString()
        val span = ClickSpan(listener)
        if (start == -1) {
            return
        }
        if (text is Spannable) {
            text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val s: SpannableString = SpannableString.valueOf(text)
            s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            description.text = s
        }
    }

    @Throws(JSONException::class)
    private fun setUpButtons(buttons: JSONArray) {
        buttonsContainer.removeAllViews()
        if (buttons.length() < 1) return

        var type: String
        for (i in 0 until buttons.length()) {
            type = buttons.getJSONObject(i).optString("type", "")
            if (type == "postback" || type == "link") {
                buttonsContainer.addView(
                    ChatButtonView(itemView.context, color, buttons.getJSONObject(i))
                        .setChatButtonClickListener(this)
                        .build(),
                    LinearLayoutCompat.LayoutParams(
                        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                    )
                )
            }
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

    override fun onChatButtonClick(jsonButton: JSONObject) {
        ChatButtonView.handleButtonClick(itemView.context, chatRoomId, jsonButton) {
           sendComment(it)
        }
    }

    override fun onSpanResult(spanText: SpannableString) {
        description.text = spanText
    }

}