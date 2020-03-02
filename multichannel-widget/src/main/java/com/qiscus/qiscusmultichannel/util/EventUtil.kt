package com.qiscus.qiscusmultichannel.util

import com.qiscus.sdk.chat.core.custom.data.model.QiscusComment
import org.json.JSONObject

/**
 * Created on : 2019-10-21
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class EventUtil {

    companion object {
        fun isChatEvent(comment: QiscusComment): Boolean {
            if (comment.type != QiscusComment.Type.CUSTOM) return false

            val json = JSONObject(comment.extraPayload)
            return json.getJSONObject("content").has("chat_event")
        }
    }
}