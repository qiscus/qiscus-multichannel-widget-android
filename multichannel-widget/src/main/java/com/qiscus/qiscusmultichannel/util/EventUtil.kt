package com.qiscus.qiscusmultichannel.util

import com.qiscus.sdk.chat.core.data.model.QMessage
import org.json.JSONObject

/**
 * Created on : 2019-10-21
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class EventUtil {

    companion object {
        fun isChatEvent(comment: QMessage): Boolean {
            if (comment.type != QMessage.Type.CUSTOM) return false

            val json = JSONObject(comment.payload)
            return json.getJSONObject("content").has("chat_event")
        }
    }
}