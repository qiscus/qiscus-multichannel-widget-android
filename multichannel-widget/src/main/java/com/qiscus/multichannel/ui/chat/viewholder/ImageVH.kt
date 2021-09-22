package com.qiscus.multichannel.ui.chat.viewholder

import android.view.View
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.multichannel.util.PreviewDialogUtil
import com.qiscus.sdk.chat.core.data.model.QMessage

/**
 * Created on : 22/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
open class ImageVH(
    itemView: View,
    config: QiscusMultichannelWidgetConfig,
    color: QiscusMultichannelWidgetColor,
    private val listener: CommentsAdapter.ItemViewListener?,
    viewType: Int
) : BaseImageVideoViewHolder(itemView, config, color, viewType) {

    override fun bind(comment: QMessage) {
        super.bind(comment)
        thumbnail.setOnClickListener {
            PreviewDialogUtil.dialogViewImage(itemView.context, comment)
        }

        thumbnail.setOnLongClickListener {
            listener?.onItemLongClick(itemView, adapterPosition)
            true
        }
    }
}