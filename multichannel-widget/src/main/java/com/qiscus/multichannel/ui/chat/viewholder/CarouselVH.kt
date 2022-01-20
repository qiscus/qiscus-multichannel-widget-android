package com.qiscus.multichannel.ui.chat.viewholder

import android.content.res.ColorStateList
import android.os.Build
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CarouselAdapter
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.multichannel.util.ResourceManager
import com.qiscus.sdk.chat.core.data.model.QMessage
import kotlinx.android.synthetic.main.item_carousel_mc.view.*
import org.json.JSONObject
import android.graphics.drawable.GradientDrawable





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

    init {
        itemView.container_carousel.setCardBackgroundColor(
            ContextCompat.getColor(
                itemView.context,
                R.color.qiscus_white_mc
            )
        )

        itemView.dot_indicator.apply {
            setSpacing(4)
            setSize(8)
            setSelectedColor(color.getNavigationColor())
            setUnselectedColor(color.getLeftBubbleColor())
        }

        itemView.rv_carousel.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (onScrollIsReady) {
                    itemView.dot_indicator.setPosition(
                        finPosition(recyclerView.layoutManager as LinearLayoutManager)
                    )
                }
            }
        })

    }

    private fun finPosition(manager: LinearLayoutManager): Int {
        return if (manager.findFirstCompletelyVisibleItemPosition() == 0) 0
        else manager.findLastCompletelyVisibleItemPosition()
    }

    override fun bind(comment: QMessage) {
        super.bind(comment)
        JSONObject(comment.payload).getJSONArray("cards").let {
            val adapter = CarouselAdapter(config, color, it, comment, listener)
            itemView.rv_carousel.adapter = adapter
            itemView.dot_indicator.createDotIndicator(adapter.itemCount)
        }

        onScrollIsReady = true
    }


}
