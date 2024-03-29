package com.qiscus.multichannel.ui.chat.viewholder

import android.view.View
import android.widget.TextView
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.sdk.chat.core.data.model.QMessage

/**
 * Created on : 17/02/20
 * Author     : arioki
 * Name       : Yoga Setiawan
 * GitHub     : https://github.com/arioki
 */

class NoSupportVH(
    itemView: View,
    config: QiscusMultichannelWidgetConfig,
    color: QiscusMultichannelWidgetColor
) : BaseViewHolder(itemView, config, color) {

    override fun bind(comment: QMessage) {
        super.bind(comment)
        itemView.findViewById<TextView>(R.id.tv_chat).text = itemView.context.getString(R.string.qiscus_type_not_support_mc)
    }

}