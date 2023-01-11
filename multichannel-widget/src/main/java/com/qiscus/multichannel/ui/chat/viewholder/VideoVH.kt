package com.qiscus.multichannel.ui.chat.viewholder

import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.multichannel.util.PreviewDialogUtil
import com.qiscus.multichannel.util.ResourceManager
import com.qiscus.sdk.chat.core.data.model.QMessage

/**
 * Created by huseinmuhdhor on 15/4/2021
 */
class VideoVH(
    itemView: View,
    config: QiscusMultichannelWidgetConfig,
    color: QiscusMultichannelWidgetColor,
    private val listener: CommentsAdapter.ItemViewListener?,
    viewType: Int
) : BaseImageVideoViewHolder(itemView, config, color, viewType) {

    private val ivPlay: ImageView? = itemView.findViewById(R.id.ivPlay)

    init {
        ivPlay?.setImageDrawable(
            ResourceManager.getTintDrawable(
                ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.ic_play
                ),
                color.getNavigationColor()
            )
        )
    }

    override fun bind(comment: QMessage) {
        super.bind(comment)
        thumbnail.setOnClickListener {
            PreviewDialogUtil().dialogViewImage(itemView.context, comment)
        }

        thumbnail.setOnLongClickListener {
            listener?.onItemLongClick(itemView, absoluteAdapterPosition)
            true
        }
    }

}