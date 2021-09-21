package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetColor
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.ui.chat.CommentsAdapter
import com.qiscus.qiscusmultichannel.util.PreviewDialogUtil
import com.qiscus.qiscusmultichannel.util.ResourceManager
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
            PreviewDialogUtil.dialogViewImage(itemView.context, comment)
        }

        thumbnail.setOnLongClickListener {
            listener?.onItemLongClick(itemView, absoluteAdapterPosition)
            true
        }
    }

}