package com.qiscus.multichannel.ui.chat.viewholder

import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.multichannel.util.ResourceManager
import com.qiscus.multichannel.util.SpannableUtils
import com.qiscus.sdk.chat.core.data.model.QMessage


/**
 * Created on : 22/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class TextVH(
    itemView: View,
    config: QiscusMultichannelWidgetConfig,
    color: QiscusMultichannelWidgetColor,
    private val viewType: Int
) : BaseViewHolder(itemView, config, color), SpannableUtils.ClickSpan.OnSpanListener {

    private val message: TextView = itemView.findViewById(R.id.tv_chat)
    private val spanUtils: SpannableUtils

    init {
        spanUtils = SpannableUtils(itemView.context, this)
        val backgroundColor: Int
        val colorText: Int

        if (viewType == CommentsAdapter.TYPE_MY_TEXT) {
            backgroundColor = color.getRightBubbleColor()
            colorText = color.getRightBubbleTextColor()
        } else {
            backgroundColor = color.getLeftBubbleColor()
            colorText = color.getLeftBubbleTextColor()
        }

        message.background = ResourceManager.getTintDrawable(
            ContextCompat.getDrawable(
                itemView.context,
                R.drawable.qiscus_rounded_chat_bg_mc
            ), backgroundColor
        )
        message.setTextColor(colorText)
    }

    override fun getChatFrom(): Drawable? =
        if (viewType == CommentsAdapter.TYPE_MY_TEXT) ResourceManager.IC_CHAT_FROM_ME else ResourceManager.IC_CHAT_FROM

    override fun bind(comment: QMessage) {
        super.bind(comment)
        message.text = comment.text
        spanUtils.setUpLinks(message.text.toString())
    }

    override fun onSpanResult(spanText: SpannableString) {
        message.text = spanText
    }

}