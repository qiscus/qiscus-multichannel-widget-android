package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.bumptech.glide.request.RequestOptions
import com.qiscus.nirmana.Nirmana
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetColor
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.util.DateUtil
import com.qiscus.qiscusmultichannel.util.ResourceManager
import com.qiscus.sdk.chat.core.data.model.QMessage

/**
 * Created on : 22/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
open class BaseViewHolder(
    itemView: View,
    val config: QiscusMultichannelWidgetConfig,
    val color: QiscusMultichannelWidgetColor
) : RecyclerView.ViewHolder(itemView) {

    private val sender: TextView? = itemView.findViewById(R.id.tv_name_sender)
    private val avatar: ImageView? = itemView.findViewById(R.id.img_sender_avatar)
    private val time: TextView? = itemView.findViewById(R.id.tv_time)
    private var chatFrom: View? = itemView.findViewById(R.id.chat_from)
    private val containerDateIndicator: ViewStub? =
        itemView.findViewById(R.id.container_date_indicator)
    private var firstIndicator: TextView? = null
    private var dateOfMessage: TextView? = null
    private val state: ImageView? = itemView.findViewById(R.id.state_indicator)
    var pstn = 0

    init {
        containerDateIndicator?.setOnInflateListener { _: ViewStub?, view: View ->
            firstIndicator = view.findViewById(R.id.tv_first_chat_indicator)
            dateOfMessage = view.findViewById(R.id.tv_date)

            dateOfMessage?.setTextColor(color.getTimeLabelTextColor())
            dateOfMessage?.background = ResourceManager.getTintDrawable(
                ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.rounded_date
                ), color.getTimeBackgroundColor()
            )
        }
        time?.setTextColor(color.getTimeLabelTextColor())
        sender?.setTextColor(color.getRightBubbleColor())
    }

    open fun bind(comment: QMessage) {
        time?.text = DateUtil.getTimeStringFromDate(comment.timestamp)
        dateOfMessage?.text = DateUtil.toFullDate(comment.timestamp)

        renderState(comment)

        itemView.background =
            if (comment.isSelected) ResourceManager.IC_SELECTED_BACKGROUND else null

    }

    fun setNeedToShowDate(showDate: Boolean): Boolean {
        if (showDate) containerDateIndicator?.visibility = View.VISIBLE
        dateOfMessage?.visibility = if (showDate) View.VISIBLE else View.GONE
        return showDate
    }

    fun showFirstMessageIndicator(
        showDate: Boolean,
        data: SortedList<QMessage>,
        position: Int
    ) {
        val nextPosition = position + 1
        if (showDate || data.get(nextPosition).type == QMessage.Type.CARD
            || data.get(nextPosition).type == QMessage.Type.CAROUSEL
            || data.get(nextPosition).type == QMessage.Type.SYSTEM_EVENT
        ) {
            setNeedNameAndAvatar(true, data.get(position))
        } else if (data.get(position).sender.id.equals(data.get(nextPosition).sender.id)) {
            setNeedNameAndAvatar(false, null)
        } else {
            setNeedNameAndAvatar(true, data.get(position))
        }
    }

    private fun setNeedNameAndAvatar(isShow: Boolean, comment: QMessage?) {
        if (isShow) {
            chatFrom?.let {
                it.background = getChatFrom()
                it.visibility = View.VISIBLE
            }

            if (config.isAvatarActived()) {
                sender?.text = comment!!.sender.name
                sender?.visibility = View.VISIBLE
                avatar?.let {
                    Nirmana.getInstance().get()
                        .load(comment.sender.avatarUrl)
                        .apply(
                            RequestOptions()
                                .circleCrop()
                                .placeholder(R.drawable.ic_avatar)
                                .error(R.drawable.ic_avatar)
                                .dontAnimate()
                        )
                        .into(it)
                    it.visibility = View.VISIBLE
                }
            } else {
                sender?.visibility = View.GONE
                avatar?.visibility = View.GONE
            }
        } else {
            chatFrom?.visibility = View.GONE
            sender?.visibility = View.GONE
            avatar?.visibility = if (config.isAvatarActived()) View.INVISIBLE else View.GONE
        }
    }

    open fun getChatFrom(): Drawable? = null

    private fun renderState(comment: QMessage) = with(ResourceManager) {
        if (state != null) {
            when (comment.status) {
                QMessage.STATE_PENDING, QMessage.STATE_SENDING -> {
                    state.setColorFilter(PENDING_STATE_COLOR!!)
                    state.setImageResource(R.drawable.ic_qiscus_info_time)
                }
                QMessage.STATE_SENT -> {
                    state.setColorFilter(PENDING_STATE_COLOR!!)
                    state.setImageResource(R.drawable.ic_qiscus_sending)
                }
                QMessage.STATE_DELIVERED -> {
                    state.setColorFilter(PENDING_STATE_COLOR!!)
                    state.setImageResource(R.drawable.ic_qiscus_read)
                }
                QMessage.STATE_READ -> {
                    state.setColorFilter(READ_STATE_COLOR!!)
                    state.setImageResource(R.drawable.ic_qiscus_read)
                }
                QMessage.STATE_FAILED -> {
                    state.setColorFilter(FAILED_STATE_COLOR!!)
                    state.setImageResource(R.drawable.ic_qiscus_sending_failed)
                }
            }
        }
    }

    fun showFirstTextIndicator(isShow: Boolean) {
        firstIndicator?.visibility = if (isShow) View.VISIBLE else View.GONE
    }

}