package com.qiscus.multichannel.ui.chat.viewholder

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.multichannel.util.ResourceManager
import com.qiscus.sdk.chat.core.data.model.QMessage
import org.json.JSONException
import org.json.JSONObject

/**
 * Created on : 20/08/21
 * Author     : mmnuradityo
 * GitHub     : https://github.com/mmnuradityo
 */
class ButtonVH(
    itemView: View,
    config: QiscusMultichannelWidgetConfig,
    color: QiscusMultichannelWidgetColor,
    private val listener: CommentsAdapter.ItemViewListener?
) : BaseViewHolder(itemView, config, color), ChatButtonView.ChatButtonClickListener {

    private var chatRoomId: Long = 0
    private var message: LinearLayoutCompat
    private var contents: TextView
    private var tvTime: TextView
    private var buttonsContainer: LinearLayoutCompat

    init {
        message = itemView.findViewById(R.id.message)
        contents = itemView.findViewById(R.id.contents)
        tvTime = itemView.findViewById(R.id.tv_time)
        buttonsContainer = itemView.findViewById(R.id.buttonsContainer)

        message.background = ResourceManager.getTintDrawable(
            ContextCompat.getDrawable(
                itemView.context,
                R.drawable.qiscus_rounded_chat_bg_mc
            ), color.getLeftBubbleColor()
        )
        contents.setTextColor(color.getLeftBubbleTextColor())
        tvTime.setTextColor(color.getLeftBubbleTextColor())
    }

    override fun bind(comment: QMessage) {
        super.bind(comment)
        this.chatRoomId = comment.chatRoomId

        try {
            ChatButtonView.setUpButtons(
                buttonsContainer, JSONObject(comment.payload).getJSONArray("buttons")
            ) {
                ChatButtonView(itemView.context, color, it)
                    .setChatButtonClickListener(this)
                    .build()
            }
        } catch (e: JSONException) {
            // ignored
        }
        contents.text = comment.text
        tvTime.text = comment.timestamp.toString()
    }

    override fun onChatButtonClick(jsonButton: JSONObject) {
        ChatButtonView.handleButtonClick(itemView.context, chatRoomId, jsonButton) {
            sendComment(it)
        }
    }

    private fun sendComment(comment: QMessage) {
        listener?.onSendComment(comment)
    }

}