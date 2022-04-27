package com.qiscus.multichannel.ui.chat.viewholder

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.multichannel.util.ResourceManager
import com.qiscus.nirmana.Nirmana
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.data.model.QiscusLocation

class LocationVH(
    itemView: View,
    config: QiscusMultichannelWidgetConfig,
    color: QiscusMultichannelWidgetColor,
    viewType: Int
) : BaseViewHolder(itemView, config, color) {

    private val mapImageView: ImageView = itemView.findViewById(R.id.iv_map_image)
    private val containerLoction: LinearLayoutCompat = itemView.findViewById(R.id.containerLoction)
    private val tvLocationName: TextView = itemView.findViewById(R.id.tv_location_name)
    private val tvLocationAddress: TextView = itemView.findViewById(R.id.tv_location_address)

    init {
        val backgroundColor: Int
        val colorText: Int

        if (viewType == CommentsAdapter.TYPE_MY_LOCATION) {
            backgroundColor = color.getRightBubbleColor()
            colorText = color.getRightBubbleTextColor()
        } else {
            backgroundColor = color.getLeftBubbleColor()
            colorText = color.getLeftBubbleTextColor()
        }

        containerLoction.background = ResourceManager.getTintDrawable(
            ContextCompat.getDrawable(
                itemView.context,
                R.drawable.bg_opponent_caption_image
            ), backgroundColor
        )
        tvLocationName.setTextColor(colorText)
        tvLocationAddress.setTextColor(colorText)
    }

    override fun bind(comment: QMessage) {
        super.bind(comment)
        tvLocationName.text = comment.location.name
        tvLocationAddress.text = comment.location.address
        renderImage(comment.location.thumbnailUrl)
        mapImageView.setOnClickListener {
            openMap(comment.location)
        }
    }

    private fun renderImage(url: String?) {
        if (url != null && url.isNotEmpty()) {
            Nirmana.getInstance().get()
                .asBitmap()
                .thumbnail(0.4f)
                .apply(
                    RequestOptions()
                        .skipMemoryCache(false)
                        .transform(
                            CenterCrop(),
                            RoundedCorners(ResourceManager.DIMEN_ROUNDED_IMAGE)
                        )
                        .error(R.drawable.ic_qiscus_placehalder_map)
                        .placeholder(R.drawable.ic_qiscus_placehalder_map)
                )
                .load(url)
                .into(mapImageView)
        }
    }

    private fun openMap(location: QiscusLocation) {
        itemView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(location.mapUrl)))
    }

}
