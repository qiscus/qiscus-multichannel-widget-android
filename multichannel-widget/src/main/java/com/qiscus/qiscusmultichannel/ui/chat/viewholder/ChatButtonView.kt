package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetColor
import com.qiscus.qiscusmultichannel.R
import kotlinx.android.synthetic.main.view_chat_button.view.*
import org.json.JSONObject

class ChatButtonView(
    context: Context,
    color: QiscusMultichannelWidgetColor,
    private val jsonButton: JSONObject
) : View.OnClickListener {

    private val itemView: View =
        LayoutInflater.from(context).inflate(R.layout.view_chat_button, null)
    private var chatButtonClickListener: ChatButtonClickListener? = null

    init {
        itemView.button.setTextColor(color.getLeftBubbleTextColor())
        itemView.liner.setBackgroundColor(color.getFieldChatBorderColor())
        itemView.button.text = jsonButton.optString("label", "Button")
        itemView.button.setOnClickListener(this)
    }

    fun setChatButtonClickListener(chatButtonClickListener: ChatButtonClickListener?): ChatButtonView {
        this.chatButtonClickListener = chatButtonClickListener
        return this
    }

    override fun onClick(v: View) {
        chatButtonClickListener!!.onChatButtonClick(jsonButton)
    }

    fun build(): View {
        return itemView
    }

    interface ChatButtonClickListener {
        fun onChatButtonClick(jsonButton: JSONObject?)
    }

}
