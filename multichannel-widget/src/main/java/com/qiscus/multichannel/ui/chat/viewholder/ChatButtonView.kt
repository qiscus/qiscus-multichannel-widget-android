package com.qiscus.multichannel.ui.chat.viewholder

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.webview.WebViewHelper
import com.qiscus.sdk.chat.core.data.model.QMessage
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ChatButtonView(
    context: Context,
    color: QiscusMultichannelWidgetColor,
    private val jsonButton: JSONObject
) : View.OnClickListener {

    private val itemView: View =
        LayoutInflater.from(context).inflate(R.layout.view_chat_button, null)
    private var chatButtonClickListener: ChatButtonClickListener? = null
    private val button: TextView = itemView.findViewById(R.id.button)
    private val liner: View = itemView.findViewById(R.id.liner)

    init {
        button.setTextColor(color.getLeftBubbleTextColor())
        liner.setBackgroundColor(color.getFieldChatBorderColor())
        button.text = jsonButton.optString("label", "Button")
        button.setOnClickListener(this)
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
        fun onChatButtonClick(jsonButton: JSONObject)
    }

    companion object {
        @Throws(JSONException::class)
        fun setUpButtons(
            container: ViewGroup,
            buttons: JSONArray,
            layoutParams: LayoutParams? = null,
            onAddButtonView: (JSONObject) -> View
        ) {
            container.removeAllViews()
            if (buttons.length() < 1) return

            var v: View
            for (i in 0 until buttons.length()) {
                val type = buttons.getJSONObject(i).optString("type", "")
                if ("postback" == type || "link" == type) {
                    v = onAddButtonView.invoke(buttons.getJSONObject(i))

                    if (layoutParams == null) {
                        container.addView(v)
                    } else {
                        container.addView(v, layoutParams)
                    }
                }
            }

            container.visibility = View.VISIBLE
        }

        fun handleButtonClick(
            context: Context,
            chatRoomId: Long,
            jsonButton: JSONObject,
            onPostBack: (QMessage) -> Unit
        ) {
            when (jsonButton.getString("type")) {
                "link" -> WebViewHelper.launchUrl(
                    context,
                    Uri.parse(JSONObject(jsonButton.get("payload").toString()).getString("url"))
                )
                "postback" -> {
                    val postBackMessage = QMessage.generatePostBackMessage(
                        chatRoomId,
                        jsonButton.getString("postback_text"),
                        JSONObject(jsonButton.get("payload").toString())
                    )
                    onPostBack.invoke(postBackMessage)
                }
            }
        }
    }

}
