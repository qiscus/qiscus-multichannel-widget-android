package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.qiscus.qiscusmultichannel.ui.chat.CarouselAdapter
import com.qiscus.sdk.chat.core.data.model.QMessage
import kotlinx.android.synthetic.main.item_carousel_mc.view.*
import org.json.JSONObject

/**
 * Created on : 15/02/20
 * Author     : arioki
 * Name       : Yoga Setiawan
 * GitHub     : https://github.com/arioki
 */

class CarouselVH(itemView: View) : BaseViewHolder(itemView) {
    override fun bind(comment: QMessage) {
        super.bind(comment)
        val payload = comment.payload
        payload.getJSONArray("cards")?.let {
            val adapter = CarouselAdapter(it, comment)
            val rvCarousel = itemView.rv_carousel
            rvCarousel.layoutManager =
                LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            rvCarousel.adapter = adapter

        }
    }

}
