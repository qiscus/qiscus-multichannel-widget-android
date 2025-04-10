package com.qiscus.multichannel.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.viewholder.*
import com.qiscus.multichannel.util.AudioHandler
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.util.QiscusDateUtil
import rx.functions.Action2

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
    private val userLoginId: String

    init {
        userLoginId = MultichannelConst.qiscusCore()!!.qiscusAccount.id
    }

    override val itemClass: Class<QMessage>
        get() = QMessage::class.java

    override fun compare(item1: QMessage, item2: QMessage): Int {
        if (item2 == item1) { //Same comments
            return 0
        } else if (item2.id == -1L && item1.id == -1L) { //Not completed comments
            return item2.timestamp.compareTo(item1.timestamp)
        } else if (item2.id > -1L && item1.id > -1L) { //Completed comments
            return item2.id.compareTo(item1.id)
        } else if (item2.id == -1L) {
            return 1
        } else if (item1.id == -1L) {
            return -1
        }
        return item2.timestamp.compareTo(item1.timestamp)
    }

    override fun getItemViewType(position: Int): Int {
        val comment = data.get(position)
        when (comment.type) {
            QMessage.Type.TEXT -> {
                return if (comment.isAttachment) {
                    if (comment.isMyComment(userLoginId)) TYPE_MY_IMAGE else TYPE_OPPONENT_IMAGE
                } else {
                    if (comment.isMyComment(userLoginId)) TYPE_MY_TEXT else TYPE_OPPONENT_TEXT
                }
            }
            QMessage.Type.LINK -> {
                return if (isSticker(comment.text)) {
                    if (comment.isMyComment(userLoginId)) TYPE_MY_STICKER else TYPE_OPPONENT_STICKER
                } else if (comment.isMyComment(userLoginId)) TYPE_MY_TEXT else TYPE_OPPONENT_TEXT
            }
            QMessage.Type.REPLY -> {
                return if (comment.isMyComment(userLoginId)) TYPE_MY_REPLY else TYPE_OPPONENT_REPLY
            }
            QMessage.Type.IMAGE -> {
                return if (comment.isMyComment(userLoginId)) TYPE_MY_IMAGE else TYPE_OPPONENT_IMAGE
            }
            QMessage.Type.VIDEO -> {
                return if (comment.isMyComment(userLoginId)) TYPE_MY_VIDEO else TYPE_OPPONENT_VIDEO
            }
            QMessage.Type.FILE -> {
                return if (comment.isMyComment(userLoginId)) TYPE_MY_FILE else TYPE_OPPONENT_FILE
            }
            QMessage.Type.AUDIO -> {
                return if (comment.isMyComment(userLoginId)) TYPE_MY_AUDIO else TYPE_OPPONENT_AUDIO
            }
            QMessage.Type.CUSTOM -> {
                return if (comment.isMyComment(userLoginId)) TYPE_MY_STICKER else TYPE_OPPONENT_STICKER
            }
            QMessage.Type.LOCATION -> {
                return if (comment.isMyComment(userLoginId)) TYPE_MY_LOCATION else TYPE_OPPONENT_LOCATION
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
                ReplyVH(getView(parent, viewType), config, color, itemViewListener, viewType)

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

    override fun addOrUpdate(items: List<QMessage>) {
        val lastPosition = data.size() - 1
        var index: Int
        var comment: QMessage

        for (i in items.indices) {
            comment = items[i]
            index = findPosition(comment)

            if (index == -1) {
                data.add(comment)
                notifyItemInserted(data.size() - 1)
            } else {
                data.updateItemAt(index, comment)
                notifyItemChanged(index)
            }
        }
        notifyItemChanged(lastPosition)
    }

    override fun addOrUpdate(item: QMessage) {
        val lastPosition = data.size() - 1
        val index = findPosition(item)

        if (index == -1) {
            data.add(item)
            notifyItemInserted(data.size() - 1)
        } else {
            data.updateItemAt(index, item)
            notifyItemChanged(index)
        }
        notifyItemChanged(lastPosition)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun remove(item: QMessage) {
        data.remove(item)
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

    fun clearSelected(position: Int) {
        val size = data.size()
        if (position > size - 1) return

        data[position].isSelected = false
        notifyItemChanged(position)
    }

    fun clearSelected() {
        val size = data.size()
        for (i in size - 1 downTo 0) {
            if (data.get(i).isSelected) {
                data.get(i).isSelected = false
                notifyItemChanged(i)
            }
        }
    }

    fun updateLastDeliveredComment(lastDeliveredCommentId: Long) {
        this.lastDeliveredCommentId = lastDeliveredCommentId
        updateCommentState()
    }

    fun updateLastReadComment(lastReadCommentId: Long) {
        this.lastReadCommentId = lastReadCommentId
        this.lastDeliveredCommentId = lastReadCommentId
        updateCommentState()
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
                    notifyItemChanged(i)
                } else if (data.get(i).id <= lastDeliveredCommentId) {
                    if (data.get(i).status >= QMessage.STATE_DELIVERED) {
                        break
                    }
                    data.get(i).status = QMessage.STATE_DELIVERED
                    notifyItemChanged(i)
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

    fun goToComment(commentId: Long, action: Action2<QMessage, Int>) {
        val size = data.size()
        var comment: QMessage

        for (i in 0 until size) {
            comment = data.get(i)
            if (comment.id == commentId) {
                comment.isSelected = true
                action.call(comment, i)
                notifyItemChanged(i)
                break
            }
        }
    }

    fun getLatestComment(): QMessage {
        return data[data.size() - 1]
    }

    override fun onInserted(position: Int, count: Int) {
        notifyItemRangeInserted(position, count)
    }

    override fun onRemoved(position: Int, count: Int) {
        notifyItemRangeRemoved(position, count)
        notifyItemRangeChanged(position, data.size())
    }

    override fun onChanged(position: Int, count: Int) {
        notifyItemRangeChanged(position, count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        notifyItemMoved(fromPosition, toPosition)
        notifyItemChanged(toPosition)
    }

    interface ItemViewListener {
        fun onSendComment(comment: QMessage)

        fun onItemClick(view: View, position: Int)

        fun onItemLongClick(view: View, position: Int)

        fun onItemReplyClick(view: View, comment: QMessage)

        fun stopAnotherAudio(comment: QMessage)
    }

    companion object {
        const val TYPE_NOT_SUPPORT = 0
        const val TYPE_MY_TEXT = 1
        const val TYPE_OPPONENT_TEXT = 2
        const val TYPE_MY_IMAGE = 3
        const val TYPE_OPPONENT_IMAGE = 4
        const val TYPE_MY_VIDEO = 5
        const val TYPE_OPPONENT_VIDEO = 6
        const val TYPE_MY_FILE = 7
        const val TYPE_OPPONENT_FILE = 8
        const val TYPE_MY_REPLY = 9
        const val TYPE_OPPONENT_REPLY = 10
        const val TYPE_EVENT = 11
        const val TYPE_CARD = 12
        const val TYPE_CAROUSEL = 13
        const val TYPE_BUTTON = 14
        const val TYPE_MY_STICKER = 15
        const val TYPE_OPPONENT_STICKER = 16
        const val TYPE_MY_LOCATION = 17
        const val TYPE_OPPONENT_LOCATION = 18
        const val TYPE_MY_AUDIO = 19
        const val TYPE_OPPONENT_AUDIO = 20
    }

}