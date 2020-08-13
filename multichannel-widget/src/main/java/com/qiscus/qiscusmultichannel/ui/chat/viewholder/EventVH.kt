package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.view.View
import android.widget.TextView
import com.qiscus.qiscusmultichannel.R
import com.qiscus.sdk.chat.core.data.model.QMessage

class EventVH(itemView: View) : BaseViewHolder(itemView) {

    private val tvEvent: TextView? = itemView.findViewById(R.id.tvEvent)

    override fun bind(comment: QMessage) {
        super.bind(comment)
        tvEvent?.text = comment.text
    }
}