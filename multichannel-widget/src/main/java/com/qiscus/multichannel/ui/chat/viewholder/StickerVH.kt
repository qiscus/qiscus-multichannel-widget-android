package com.qiscus.multichannel.ui.chat.viewholder

import android.view.View
import com.bumptech.glide.request.RequestOptions
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.nirmana.Nirmana
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.util.QiscusFileUtil

/**
 * Created on : 20/08/21
 * Author     : mmnuradityo
 * GitHub     : https://github.com/mmnuradityo
 */
class StickerVH(
    itemView: View,
    config: QiscusMultichannelWidgetConfig,
    color: QiscusMultichannelWidgetColor
) : BaseViewHolder(itemView, config, color) {

    override fun bind(comment: QMessage) {
        super.bind(comment)
        Nirmana.getInstance().get()
            .asBitmap()
            .thumbnail(0.3f)
            .apply(
                RequestOptions()
                    .skipMemoryCache(false)
                    .placeholder(R.drawable.qiscus_image_placeholder)
                    .error(R.drawable.qiscus_image_placeholder)
            )
            .load(QiscusFileUtil.getThumbnailURL(setStickerAttachment(comment.text)))
            .into(itemView.findViewById(R.id.iv_chat_sticker))
    }

    private fun setStickerAttachment(message: String): String {
        val fileNameEndIndex = message.lastIndexOf("[/sticker]")
        val fileNameBeginIndex = message.indexOf("[sticker]")

        return if (fileNameBeginIndex > -1 && fileNameEndIndex > -1) message.substring(
            fileNameBeginIndex + 1,
            fileNameEndIndex
        ).trim() else ""
    }

}