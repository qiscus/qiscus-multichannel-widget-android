package com.qiscus.qiscusmultichannel.ui.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.ui.chat.viewholder.*
import com.qiscus.sdk.chat.core.custom.data.model.QiscusComment
import com.qiscus.sdk.chat.core.custom.util.QiscusAndroidUtil
import com.qiscus.sdk.chat.core.custom.util.QiscusDateUtil

/**
 * Created on : 19/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class CommentsAdapter(val context: Context) :
    SortedRecyclerViewAdapter<QiscusComment, BaseViewHolder>() {

    private var lastDeliveredCommentId: Long = 0
    private var lastReadCommentId: Long = 0
    private var selectedComment: QiscusComment? = null

    override val itemClass: Class<QiscusComment>
        get() = QiscusComment::class.java

    override fun compare(item1: QiscusComment, item2: QiscusComment): Int {
        if (item2 == item1) { //Same comments
            return 0
        } else if (item2.id == -1L && item1.id == -1L) { //Not completed comments
            return item2.time.compareTo(item1.time)
        } else if (item2.id != -1L && item1.id != -1L) { //Completed comments
            return QiscusAndroidUtil.compare(item2.id, item1.id)
        } else if (item2.id == -1L) {
            return 1
        } else if (item1.id == -1L) {
            return -1
        }
        return item2.time.compareTo(item1.time)
    }

    override fun getItemViewType(position: Int): Int {
        val comment = data.get(position)
        when (comment.type) {
            QiscusComment.Type.TEXT -> return if (comment.isMyComment) TYPE_MY_TEXT else TYPE_OPPONENT_TEXT

            QiscusComment.Type.REPLY -> {
                return if (comment.isMyComment) TYPE_MY_REPLY else TYPE_OPPONENT_REPLY
            }

            QiscusComment.Type.IMAGE -> {
                return if (comment.isMyComment) TYPE_MY_IMAGE else TYPE_OPPONENT_IMAGE
            }
            QiscusComment.Type.FILE -> {
                return if (comment.isMyComment) TYPE_MY_FILE else TYPE_OPPONENT_FILE
            }
            QiscusComment.Type.SYSTEM_EVENT -> return TYPE_EVENT
            QiscusComment.Type.CARD -> return TYPE_CARD
            QiscusComment.Type.CAROUSEL -> return TYPE_CAROUSEL
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
            else -> return LayoutInflater.from(context).inflate(R.layout.item_message_not_supported_mc, parent, false)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            TYPE_MY_TEXT, TYPE_OPPONENT_TEXT -> TextVH(getView(parent, viewType))
            TYPE_MY_IMAGE, TYPE_OPPONENT_IMAGE -> ImageVH(getView(parent, viewType))
            TYPE_MY_REPLY, TYPE_OPPONENT_REPLY -> ReplyVH(getView(parent, viewType))
            TYPE_EVENT -> EventVH(getView(parent, viewType))
            TYPE_MY_FILE, TYPE_OPPONENT_FILE -> FileVH(getView(parent, viewType))
            TYPE_CARD -> CardVH(getView(parent, viewType))
            TYPE_CAROUSEL -> CarouselVH(getView(parent, viewType))
            else -> NoSupportVH(getView(parent,viewType))
        }
    }

    override fun getItemCount(): Int = data.size()

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(data.get(position))
        holder.pstn = position

        holder.setNeedToShowName(false)

        if (position == data.size() - 1) {
            holder.setNeedToShowDate(true)
        } else {
            holder.setNeedToShowDate(
                !QiscusDateUtil.isDateEqualIgnoreTime(
                    data.get(position).time,
                    data.get(position + 1).time
                )
            )
        }

        setOnClickListener(holder.itemView, position)
    }

    override fun addOrUpdate(comments: List<QiscusComment>) {
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

    override fun addOrUpdate(comment: QiscusComment) {
        val index = findPosition(comment)
        if (index == -1) {
            data.add(comment)
        } else {
            data.updateItemAt(index, comment)
        }
        notifyDataSetChanged()
    }

    override fun remove(comment: QiscusComment) {
        data.remove(comment)
        notifyDataSetChanged()
    }

    fun setSelectedComment(comment: QiscusComment) = apply { this.selectedComment = comment }

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
            if (data.get(i).state > QiscusComment.STATE_SENDING) {
                if (data.get(i).id <= lastReadCommentId) {
                    if (data.get(i).state == QiscusComment.STATE_READ) {
                        break
                    }
                    data.get(i).state = QiscusComment.STATE_READ
                } else if (data.get(i).id <= lastDeliveredCommentId) {
                    if (data.get(i).state >= QiscusComment.STATE_DELIVERED) {
                        break
                    }
                    data.get(i).state = QiscusComment.STATE_DELIVERED
                }
            }
        }
    }

    fun getLatestSentComment(): QiscusComment? {
        val size = data.size()
        for (i in 0 until size) {
            val comment = data.get(i)
            if (comment.state >= QiscusComment.STATE_ON_QISCUS) {
                return comment
            }
        }
        return null
    }


    interface RecyclerViewItemClickListener {
        fun onItemClick(view: View, position: Int)

        fun onItemLongClick(view: View, position: Int)
    }
    private val TYPE_NOT_SUPPORT = 0
    private val TYPE_MY_TEXT = 1
    private val TYPE_OPPONENT_TEXT = 2
    private val TYPE_MY_IMAGE = 3
    private val TYPE_OPPONENT_IMAGE = 4
    private val TYPE_MY_FILE = 5
    private val TYPE_OPPONENT_FILE = 6
    private val TYPE_MY_REPLY = 7
    private val TYPE_OPPONENT_REPLY = 8
    private val TYPE_EVENT = 9
    private val TYPE_CARD = 10
    private val TYPE_CAROUSEL = 11
}