package com.qiscus.qiscusmultichannel.ui.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetColor
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.ui.chat.viewholder.*
import com.qiscus.qiscusmultichannel.util.AudioHandler
import com.qiscus.qiscusmultichannel.util.MultichannelConst
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.util.QiscusDateUtil

/**
 * Created on : 19/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class CommentsAdapter(
    private val context: Context,
    private val config: QiscusMultichannelWidgetConfig,
    private val color: QiscusMultichannelWidgetColor,
    private val audioHandler: AudioHandler
) : SortedRecyclerViewAdapter<QMessage, BaseViewHolder>() {

    private var audioPlayerId: Long = 0
    private var lastDeliveredCommentId: Long = 0
    private var lastReadCommentId: Long = 0
    private var selectedComment: QMessage? = null

    override val itemClass: Class<QMessage>
        get() = QMessage::class.java

    override fun compare(item1: QMessage, item2: QMessage): Int {
        if (item2 == item1) { //Same comments
            return 0
        } else if (item2.id == -1L && item1.id == -1L) { //Not completed comments
            return item2.timestamp.compareTo(item1.timestamp)
        } else if (item2.id != -1L && item1.id != -1L) { //Completed comments
            return MultichannelConst.qiscusCore()?.androidUtil?.compare(item2.id, item1.id)!!
        } else if (item2.id == -1L) {
            return 1
        } else if (item1.id == -1L) {
            return -1
        }
        return item2.timestamp.compareTo(item1.timestamp)
    }

    override fun getItemViewType(position: Int): Int {
        val me = MultichannelConst.qiscusCore()?.qiscusAccount?.id
        val comment = data.get(position)
        when (comment.type) {
            QMessage.Type.TEXT -> {
                return if (comment.isAttachment) {
                    if (comment.isMyComment(me)) TYPE_MY_IMAGE else TYPE_OPPONENT_IMAGE
                } else {
                    if (comment.isMyComment(me)) TYPE_MY_TEXT else TYPE_OPPONENT_TEXT
                }
            }
            QMessage.Type.LINK -> {
                return if (isSticker(comment.text)) {
                    if (comment.isMyComment(me)) TYPE_MY_STICKER else TYPE_OPPONENT_STICKER
                } else if (comment.isMyComment(me)) TYPE_MY_TEXT else TYPE_OPPONENT_TEXT
            }
            QMessage.Type.REPLY -> {
                return if (comment.isMyComment(me)) TYPE_MY_REPLY else TYPE_OPPONENT_REPLY
            }
            QMessage.Type.IMAGE -> {
                return if (comment.isMyComment(me)) TYPE_MY_IMAGE else TYPE_OPPONENT_IMAGE
            }
            QMessage.Type.VIDEO -> {
                return if (comment.isMyComment(me)) TYPE_MY_VIDEO else TYPE_OPPONENT_VIDEO
            }
            QMessage.Type.FILE -> {
                return if (comment.isMyComment(me)) TYPE_MY_FILE else TYPE_OPPONENT_FILE
            }
            QMessage.Type.AUDIO -> {
                return if (comment.isMyComment(me)) TYPE_MY_AUDIO else TYPE_OPPONENT_AUDIO
            }
            QMessage.Type.CUSTOM -> {
                return if (comment.isMyComment(me)) TYPE_MY_STICKER else TYPE_OPPONENT_STICKER
            }
            QMessage.Type.LOCATION -> {
                return if (comment.isMyComment(me)) TYPE_MY_LOCATION else TYPE_OPPONENT_LOCATION
            }
            QMessage.Type.SYSTEM_EVENT -> return TYPE_EVENT
            QMessage.Type.CARD -> return TYPE_CARD
            QMessage.Type.CAROUSEL -> return TYPE_CAROUSEL
            QMessage.Type.BUTTONS -> return TYPE_BUTTON
            else -> return TYPE_NOT_SUPPORT
        }
    }

    private fun getView(parent: ViewGroup, viewType: Int): View {
        when (viewType) {
            TYPE_MY_TEXT -> return LayoutInflater.from(context)
                .inflate(R.layout.item_my_text_comment_mc, parent, false)
            TYPE_OPPONENT_TEXT -> return LayoutInflater.from(context)
                .inflate(R.layout.item_opponent_text_comment_mc, parent, false)
            TYPE_MY_IMAGE -> return LayoutInflater.from(context)
                .inflate(R.layout.item_my_image_comment_mc, parent, false)
            TYPE_OPPONENT_IMAGE -> return LayoutInflater.from(context)
                .inflate(R.layout.item_opponent_image_comment_mc, parent, false)
            TYPE_MY_VIDEO -> return LayoutInflater.from(context)
                .inflate(R.layout.item_my_video_comment_mc, parent, false)
            TYPE_OPPONENT_VIDEO -> return LayoutInflater.from(context)
                .inflate(R.layout.item_opponent_video_comment_mc, parent, false)
            TYPE_MY_REPLY -> return LayoutInflater.from(context)
                .inflate(R.layout.item_my_reply_mc, parent, false)
            TYPE_OPPONENT_REPLY -> return LayoutInflater.from(context)
                .inflate(R.layout.item_opponent_reply_mc, parent, false)
            TYPE_EVENT -> return LayoutInflater.from(context)
                .inflate(R.layout.item_event_mc, parent, false)
            TYPE_MY_FILE -> return LayoutInflater.from(context)
                .inflate(R.layout.item_my_file_mc, parent, false)
            TYPE_OPPONENT_FILE -> return LayoutInflater.from(context)
                .inflate(R.layout.item_opponent_file_mc, parent, false)
            TYPE_CARD -> return LayoutInflater.from(context)
                .inflate(R.layout.item_card_mc, parent, false)
            TYPE_CAROUSEL -> return LayoutInflater.from(context)
                .inflate(R.layout.item_carousel_mc, parent, false)
            TYPE_BUTTON -> return LayoutInflater.from(context)
                .inflate(R.layout.item_button_mc, parent, false)
            TYPE_MY_STICKER -> return LayoutInflater.from(context)
                .inflate(R.layout.item_my_sticker_mc, parent, false)
            TYPE_OPPONENT_STICKER -> return LayoutInflater.from(context)
                .inflate(R.layout.item_opponent_sticker_mc, parent, false)
            TYPE_MY_LOCATION -> return LayoutInflater.from(context)
                .inflate(R.layout.item_my_location_mc, parent, false)
            TYPE_OPPONENT_LOCATION -> return LayoutInflater.from(context)
                .inflate(R.layout.item_opponent_location_mc, parent, false)
            TYPE_MY_AUDIO -> return LayoutInflater.from(context)
                .inflate(R.layout.item_my_audio_mc, parent, false)
            TYPE_OPPONENT_AUDIO -> return LayoutInflater.from(context)
                .inflate(R.layout.item_opponent_audio_mc, parent, false)
            else -> return LayoutInflater.from(context)
                .inflate(R.layout.item_message_not_supported_mc, parent, false)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            TYPE_MY_TEXT, TYPE_OPPONENT_TEXT ->
                TextVH(getView(parent, viewType), config, color, viewType)

            TYPE_MY_IMAGE, TYPE_OPPONENT_IMAGE ->
                ImageVH(getView(parent, viewType), config, color, itemViewListener, viewType)

            TYPE_MY_VIDEO, TYPE_OPPONENT_VIDEO ->
                VideoVH(getView(parent, viewType), config, color, itemViewListener, viewType)

            TYPE_MY_REPLY, TYPE_OPPONENT_REPLY ->
                ReplyVH(getView(parent, viewType), config, color, viewType)

            TYPE_EVENT ->
                EventVH(getView(parent, viewType), config, color)

            TYPE_MY_FILE, TYPE_OPPONENT_FILE ->
                FileVH(getView(parent, viewType), config, color, viewType)

            TYPE_MY_AUDIO, TYPE_OPPONENT_AUDIO ->
                AudioVH(
                    getView(parent, viewType),
                    config,
                    color,
                    viewType,
                    itemViewListener,
                    audioHandler
                )

            TYPE_CARD ->
                CardVH(getView(parent, viewType), config, color, itemViewListener)

            TYPE_CAROUSEL ->
                CarouselVH(getView(parent, viewType), config, color, itemViewListener)

            TYPE_BUTTON ->
                ButtonVH(getView(parent, viewType), config, color, itemViewListener)

            TYPE_MY_STICKER, TYPE_OPPONENT_STICKER ->
                StickerVH(getView(parent, viewType), config, color)

            TYPE_MY_LOCATION, TYPE_OPPONENT_LOCATION ->
                LocationVH(getView(parent, viewType), config, color, viewType)

            else ->
                NoSupportVH(getView(parent, viewType), config, color)
        }
    }

    private fun isSticker(message: String): Boolean {
        return message.contains("[sticker]") && message.contains("[/sticker]")
    }

    override fun getItemCount(): Int = data.size()

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (position == data.size() - 1) {
            holder.showFirstMessageIndicator(holder.setNeedToShowDate(true), data, position)
            holder.showFirstTextIndicator(true)
        } else {
            holder.showFirstMessageIndicator(
                holder.setNeedToShowDate(
                    !QiscusDateUtil.isDateEqualIgnoreTime(
                        data.get(position).timestamp,
                        data.get(position + 1).timestamp
                    )
                ), data, position
            )
            holder.showFirstTextIndicator(false)
        }

        holder.bind(data.get(position))
        holder.pstn = position

        setOnClickListener(holder.itemView, position)

        if (holder is AudioVH) holder
            .stopAudio(
                audioPlayerId,
                audioPlayerId == -2L || data[position].id != audioPlayerId
            )
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        if (holder is AudioVH) {
            holder.destroyAudio()
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.audioPlayerId = RecyclerView.NO_ID
    }

    override fun addOrUpdate(comments: List<QMessage>) {
        for (comment in comments) {
            val index = findPosition(comment)
            if (index == -1) {
                data.add(comment)
            } else {
                data.updateItemAt(index, comment)
            }
        }
        notifyDataSetChanged()
    }

    override fun addOrUpdate(comment: QMessage) {
        val index = findPosition(comment)
        if (index == -1) {
            data.add(comment)
        } else {
            data.updateItemAt(index, comment)
        }
        notifyDataSetChanged()
    }

    override fun remove(comment: QMessage) {
        data.remove(comment)
        notifyDataSetChanged()
    }

    fun stopAnotherAudio(messageId: Long) {
        audioPlayerId = messageId
        for (i in 0 until data.size()) {
            if (data[i].type === QMessage.Type.AUDIO && data[i].id != messageId) {
                notifyItemChanged(i, 1)
            }
        }
    }

    fun setSelectedComment(comment: QMessage) = apply { this.selectedComment = comment }

    fun getSelectedComment() = selectedComment

    fun clearSelected() {
        val size = data.size()
        for (i in size - 1 downTo 0) {
            if (data.get(i).isSelected) {
                data.get(i).isSelected = false
            }
        }
        notifyDataSetChanged()
    }

    fun updateLastDeliveredComment(lastDeliveredCommentId: Long) {
        this.lastDeliveredCommentId = lastDeliveredCommentId
        updateCommentState()
        notifyDataSetChanged()
    }

    fun updateLastReadComment(lastReadCommentId: Long) {
        this.lastReadCommentId = lastReadCommentId
        this.lastDeliveredCommentId = lastReadCommentId
        updateCommentState()
        notifyDataSetChanged()
    }

    private fun updateCommentState() {
        val size = data.size()
        for (i in 0 until size) {
            if (data.get(i).status > QMessage.STATE_SENDING) {
                if (data.get(i).id <= lastReadCommentId) {
                    if (data.get(i).status == QMessage.STATE_READ) {
                        break
                    }
                    data.get(i).status = QMessage.STATE_READ
                } else if (data.get(i).id <= lastDeliveredCommentId) {
                    if (data.get(i).status >= QMessage.STATE_DELIVERED) {
                        break
                    }
                    data.get(i).status = QMessage.STATE_DELIVERED
                }
            }
        }
    }

    fun getLatestSentComment(): QMessage? {
        val size = data.size()
        for (i in 0 until size) {
            val comment = data.get(i)
            if (comment.status >= QMessage.STATE_SENT) {
                return comment
            }
        }
        return null
    }


    interface ItemViewListener {
        fun onSendComment(comment: QMessage)

        fun onItemClick(view: View, position: Int)

        fun onItemLongClick(view: View, position: Int)

        fun stopAnotherAudio(comment: QMessage)
    }

    companion object {
        val TYPE_NOT_SUPPORT = 0
        val TYPE_MY_TEXT = 1
        val TYPE_OPPONENT_TEXT = 2
        val TYPE_MY_IMAGE = 3
        val TYPE_OPPONENT_IMAGE = 4
        val TYPE_MY_VIDEO = 5
        val TYPE_OPPONENT_VIDEO = 6
        val TYPE_MY_FILE = 7
        val TYPE_OPPONENT_FILE = 8
        val TYPE_MY_REPLY = 9
        val TYPE_OPPONENT_REPLY = 10
        val TYPE_EVENT = 11
        val TYPE_CARD = 12
        val TYPE_CAROUSEL = 13
        val TYPE_BUTTON = 14
        val TYPE_MY_STICKER = 15
        val TYPE_OPPONENT_STICKER = 16
        val TYPE_MY_LOCATION = 17
        val TYPE_OPPONENT_LOCATION = 18
        val TYPE_MY_AUDIO = 19
        val TYPE_OPPONENT_AUDIO = 20
    }

}