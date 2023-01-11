package com.qiscus.multichannel.util

import com.qiscus.sdk.chat.core.data.model.QMessage
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class EventUtilTest {

    @Test
    fun `event checker`() {
        val eventClass = EventUtil().javaClass
        assertEquals(eventClass, EventUtil::class.java)
    }

    @Test
    fun `isChatEvent test custom`() {
        val comment = QMessage()
        comment.rawType = "custom"
        comment.text = ""
        comment.payload = cretePayload("chat_event")

        val isChat = EventUtil.isChatEvent(comment)
        assertTrue(isChat)
    }

    @Test
    fun `isChatEvent test not custom`() {
        val comment = QMessage()
        comment.rawType = "text"
        comment.text = ""
        comment.payload = cretePayload("chat_event")

        val isChat = EventUtil.isChatEvent(comment)
        assertFalse(isChat)
    }

    @Test
    fun `isChatEvent test not event`() {
        val comment = QMessage();
        comment.rawType = "custom"
        comment.text = ""
        comment.payload = cretePayload("not_chat_event")

        val isChat = EventUtil.isChatEvent(comment)
        assertFalse(isChat)
    }

    private fun cretePayload(content: String) = JSONObject()
        .put("content", JSONObject().put(content, content))
        .toString()
}