package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetColor
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.util.ResourceManager
import com.qiscus.sdk.chat.core.data.model.QMessage

class EventVH(
    itemView: View,
    config: QiscusMultichannelWidgetConfig,
    color: QiscusMultichannelWidgetColor
) : BaseViewHolder(itemView, config, color) {

    private val tvEvent: TextView? = itemView.findViewById(R.id.tvEvent)

    init {
        tvEvent?.let {
            it.background = ResourceManager.getTintDrawable(
                ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.rounded_date
                ), color.getTimeBackgroundColor()
            )
            it.setTextColor(color.getSystemEventTextColor())
        }
    }

    override fun bind(comment: QMessage) {
        super.bind(comment)
        tvEvent?.text = comment.text

        if (config.isShowSystemMessage()) hidenUI()
    }

    fun hidenUI() {
        itemView.layoutParams.width = 0
        itemView.layoutParams.height = 0
    }
}