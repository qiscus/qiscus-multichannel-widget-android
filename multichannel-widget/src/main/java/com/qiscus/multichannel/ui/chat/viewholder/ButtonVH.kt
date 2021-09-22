package com.qiscus.multichannel.ui.chat.viewholder

import android.net.Uri
import android.view.View
import androidx.core.content.ContextCompat
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.multichannel.ui.webView.WebViewHelper
import com.qiscus.multichannel.util.ResourceManager
import com.qiscus.sdk.chat.core.data.model.QMessage
import kotlinx.android.synthetic.main.item_button_mc.view.*
import org.json.JSONArray
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

    init {
        itemView.message.background = ResourceManager.getTintDrawable(
            ContextCompat.getDrawable(
                itemView.context,
                R.drawable.qiscus_rounded_chat_bg_mc
            ), color.getLeftBubbleColor()
        )
        itemView.contents.setTextColor(color.getLeftBubbleTextColor())
        itemView.tv_time.setTextColor(color.getLeftBubbleTextColor())
    }

    override fun bind(comment: QMessage) {
        super.bind(comment)
        this.chatRoomId = comment.chatRoomId

        try {
            setUpButtons(JSONObject(comment.payload).getJSONArray("buttons"))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        itemView.contents.text = comment.text
        itemView.tv_time.text = comment.timestamp.toString()
    }

    @Throws(JSONException::class)
    private fun setUpButtons(buttons: JSONArray) {
        itemView.buttonsContainer.removeAllViews()
        if (buttons.length() < 1) return

        for (i in 0 until buttons.length()) {
            val type = buttons.getJSONObject(i).optString("type", "")
            if ("postback" == type || "link" == type) {
                itemView.buttonsContainer.addView(
                    ChatButtonView(itemView.context, color, buttons.getJSONObject(i))
                        .setChatButtonClickListener(this)
                        .build()
                )
            }
        }

        itemView.buttonsContainer.visibility = View.VISIBLE
    }

    override fun onChatButtonClick(jsonButton: JSONObject?) {
        jsonButton?.let {
            when (it.getString("type")) {
                "link" -> WebViewHelper.launchUrl(
                    itemView.context,
                    Uri.parse(JSONObject(jsonButton.get("payload").toString()).getString("url"))
                )
                "postback" -> {
                    val postBackMessage = QMessage.generatePostBackMessage(
                        this.chatRoomId,
                        it.getString("postback_text"),
                        JSONObject(jsonButton.get("payload").toString())
                    )
                    sendComment(postBackMessage)
                }
            }

        }
    }

    private fun sendComment(comment: QMessage) {
        listener?.onSendComment(comment)
    }

}