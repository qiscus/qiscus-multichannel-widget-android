package com.qiscus.qiscusmultichannel.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.ui.chat.viewholder.BaseViewHolder
import com.qiscus.qiscusmultichannel.ui.chat.viewholder.CardVH
import com.qiscus.sdk.chat.core.custom.data.model.QiscusComment
import kotlinx.android.synthetic.main.item_card_mc.view.*
import org.json.JSONArray

/**
 * Created on : 15/02/20
 * Author     : arioki
 * Name       : Yoga Setiawan
 * GitHub     : https://github.com/arioki
 */

class CarouselAdapter(val mData: JSONArray, val qiscusComment: QiscusComment) :
    RecyclerView.Adapter<BaseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_mc, parent, false)
        view.dateOfMessage.visibility = View.GONE
        view.date.visibility = View.GONE
        view.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        return CardVH(view)
    }

    override fun getItemCount(): Int = mData.length()

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val qiscusComment =
            QiscusComment.generatePostBackMessage(
                qiscusComment.roomId,
                qiscusComment.message,
                mData[position].toString()
            )
        holder.bind(qiscusComment)
    }
}