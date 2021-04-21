package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.view.View
import com.qiscus.qiscusmultichannel.ui.chat.CommentsAdapter
import com.qiscus.qiscusmultichannel.util.*
import com.qiscus.sdk.chat.core.data.model.QMessage

/**
 * Created by huseinmuhdhor on 15/4/2021
 */
class VideoVH(itemView: View, var listener: CommentsAdapter.RecyclerViewItemClickListener?) : BaseImageVideoViewHolder(itemView) {

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