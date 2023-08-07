package com.qiscus.multichannel.ui.chat.viewholder

import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CarouselAdapter
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.multichannel.ui.view.DotIndicatorView
import com.qiscus.sdk.chat.core.data.model.QMessage
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
    color: QiscusMultichannelWidgetColor,
    private val listener: CommentsAdapter.ItemViewListener?
) : BaseViewHolder(itemView, config, color) {

    private var onScrollIsReady = false
    private val containerCarousel: CardView
    private val dotIndicator: DotIndicatorView
    private val rvCarousel: RecyclerView

    init {
        containerCarousel = itemView.findViewById(R.id.container_carousel)
        dotIndicator = itemView.findViewById(R.id.dot_indicator)
        rvCarousel = itemView.findViewById(R.id.rv_carousel)

        containerCarousel.setCardBackgroundColor(
            ContextCompat.getColor(
                itemView.context,
                R.color.qiscus_white_mc
            )
        )

        dotIndicator.apply {
            setSpacing(4)
            setSize(8)
            setSelectedColor(color.getNavigationColor())
            setUnselectedColor(color.getLeftBubbleColor())
        }

        rvCarousel.addOnScrollListener(
            onScrollListener(onScrollIsReady)
        )

    }

    private fun onScrollListener(onScrollIsReady: Boolean) = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (onScrollIsReady) {
                dotIndicator.setPosition(
                    findPosition(recyclerView.layoutManager as LinearLayoutManager)
                )
            }
        }
    }

    private fun findPosition(manager: LinearLayoutManager): Int {
        return if (manager.findFirstCompletelyVisibleItemPosition() == 0) 0
        else manager.findLastCompletelyVisibleItemPosition()
    }

    override fun bind(comment: QMessage) {
        super.bind(comment)
        JSONObject(comment.payload).getJSONArray("cards").let {
            val adapter = CarouselAdapter(config, color, it, comment, listener)
            rvCarousel.adapter = adapter
            dotIndicator.createDotIndicator(adapter.itemCount)
        }

        onScrollIsReady = true
    }


}
