package com.qiscus.qiscusmultichannel.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetColor
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.ui.chat.viewholder.BaseViewHolder
import com.qiscus.qiscusmultichannel.ui.chat.viewholder.CardVH
import com.qiscus.sdk.chat.core.data.model.QMessage
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created on : 15/02/20
 * Author     : arioki
 * Name       : Yoga Setiawan
 * GitHub     : https://github.com/arioki
 */

class CarouselAdapter(
    private val config: QiscusMultichannelWidgetConfig,
    private val color: QiscusMultichannelWidgetColor,
    private val mData: JSONArray, val qiscusComment: QMessage
) :
    RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return CardVH(
            LayoutInflater.from(parent.context).inflate(R.layout.view_card_item, parent, false),
            config, color
        )
    }

    override fun getItemCount(): Int = mData.length()

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val qiscusComment =
            QMessage.generatePostBackMessage(
                qiscusComment.chatRoomId,
                qiscusComment.text,
                mData[position] as JSONObject?
            )
        holder.bind(qiscusComment)
    }

}