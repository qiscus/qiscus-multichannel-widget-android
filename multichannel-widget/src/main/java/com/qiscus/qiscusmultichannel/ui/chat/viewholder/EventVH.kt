package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.util.QiscusChatLocal
import com.qiscus.sdk.chat.core.data.model.QMessage

class EventVH(itemView: View) : BaseViewHolder(itemView) {

    private val tvEvent: TextView? = itemView.findViewById(R.id.tvEvent)
    private var linEvent : LinearLayout? = itemView.findViewById(R.id.linEvent)
    override fun bind(comment: QMessage) {
        super.bind(comment)
        tvEvent?.text = comment.text


        if (MultichannelWidget.config.getHideUIEvent()) {
            hidenUI()
        }

    }

    fun hidenUI(){
        itemView.layoutParams.width = 0
        itemView.layoutParams.height = 0
    }
}