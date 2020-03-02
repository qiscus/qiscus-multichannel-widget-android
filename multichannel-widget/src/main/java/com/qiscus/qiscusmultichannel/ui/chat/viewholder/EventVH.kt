package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.view.View
import android.widget.TextView
import com.qiscus.qiscusmultichannel.R
import com.qiscus.sdk.chat.core.custom.data.model.QiscusComment

class EventVH(itemView: View) : BaseViewHolder(itemView) {

    private val tvEvent: TextView? = itemView.findViewById(R.id.tvEvent)

    override fun bind(comment: QiscusComment) {
        super.bind(comment)
        tvEvent?.text = comment.message
    }
}