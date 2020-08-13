package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.qiscus.nirmana.Nirmana
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.util.DateUtil
import com.qiscus.sdk.chat.core.data.model.QMessage

/**
 * Created on : 22/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val avatar: ImageView? = itemView.findViewById(R.id.avatar_driver)
    private val sender: TextView? = itemView.findViewById(R.id.sender)
    private val date: TextView? = itemView.findViewById(R.id.date)
    private val dateOfMessage: TextView? = itemView.findViewById(R.id.dateOfMessage)
    @Nullable
    private val state: ImageView? = itemView.findViewById(R.id.state)

    private val pendingStateColor: Int =
        ContextCompat.getColor(itemView.context, R.color.pending_message_mc)
    private val readStateColor: Int =
        ContextCompat.getColor(itemView.context, R.color.read_message_mc)
    private val failedStateColor: Int =
        ContextCompat.getColor(itemView.context, R.color.qiscus_red_mc)
    private val selectedCommentBackground: Drawable =
        ColorDrawable(ContextCompat.getColor(itemView.context, R.color.qiscus_selected_mc))
    var pstn = 0

    open fun bind(comment: QMessage) {
        avatar?.let {
            Nirmana.getInstance().get()
                .setDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.drawable.ic_qiscus_avatar)
                        .error(R.drawable.ic_qiscus_avatar)
                        .dontAnimate()
                )
                .load(comment.sender.avatarUrl)
                .into(it)
        }
        sender?.text = comment.sender.name
        date?.text = DateUtil.getTimeStringFromDate(comment.timestamp)
        dateOfMessage?.text = DateUtil.toFullDate(comment.timestamp)

        renderState(comment)

        itemView.background = if (comment.isSelected) selectedCommentBackground else null

    }

    open fun setNeedToShowDate(showDate: Boolean) {
        dateOfMessage?.visibility = if (showDate) View.VISIBLE else View.GONE
    }

    open fun setNeedToShowName(showName: Boolean) {
        sender?.visibility = if (showName) View.VISIBLE else View.GONE
    }

    private fun renderState(comment: QMessage) {
        if (state != null) {
            when (comment.status) {
                QMessage.STATE_PENDING, QMessage.STATE_SENDING -> {
                    state.setColorFilter(pendingStateColor)
                    state.setImageResource(R.drawable.ic_qiscus_info_time)
                }
                QMessage.STATE_SENT -> {
                    state.setColorFilter(pendingStateColor)
                    state.setImageResource(R.drawable.ic_qiscus_sending)
                }
                QMessage.STATE_DELIVERED -> {
                    state.setColorFilter(pendingStateColor)
                    state.setImageResource(R.drawable.ic_qiscus_read)
                }
                QMessage.STATE_READ -> {
                    state.setColorFilter(readStateColor)
                    state.setImageResource(R.drawable.ic_qiscus_read)
                }
                QMessage.STATE_FAILED -> {
                    state.setColorFilter(failedStateColor)
                    state.setImageResource(R.drawable.ic_qiscus_sending_failed)
                }
            }
        }
    }
}