package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetColor
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.R
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

class CarouselVH(
    itemView: View,
    config: QiscusMultichannelWidgetConfig,
    color: QiscusMultichannelWidgetColor
) : BaseViewHolder(itemView, config, color) {

    private var onScrollIsReady = false

    init {
        itemView.container_carousel.setCardBackgroundColor(
            ContextCompat.getColor(
                itemView.context,
                R.color.qiscus_white_mc
            )
        )
        itemView.rv_carousel.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (onScrollIsReady) itemView.page_indicator_view.setSelected(
                    finPosition((recyclerView.layoutManager as LinearLayoutManager))
                )
            }
        })

        itemView.page_indicator_view.selectedColor = color.getNavigationColor()
        itemView.page_indicator_view.unselectedColor = color.getLeftBubbleColor()
    }

    private fun finPosition(manager: LinearLayoutManager): Int {
        return if (manager.findFirstCompletelyVisibleItemPosition() == 0) 0 else manager.findLastCompletelyVisibleItemPosition()
    }

    override fun bind(comment: QMessage) {
        super.bind(comment)
        val payload = JSONObject(comment.payload)
        payload.getJSONArray("cards").let {
            val adapter = CarouselAdapter(config, color, it, comment)
            itemView.rv_carousel.adapter = adapter
            itemView.page_indicator_view.count = adapter.itemCount
        }

        onScrollIsReady = true
    }

}
