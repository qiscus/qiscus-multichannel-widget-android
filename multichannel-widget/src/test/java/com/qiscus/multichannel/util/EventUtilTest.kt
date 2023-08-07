package com.qiscus.multichannel.util

import android.provider.SyncStateContract.Helpers
import com.qiscus.multichannel.*
import com.qiscus.multichannel.data.model.DataInitialChat
import com.qiscus.multichannel.data.model.response.ResponseInitiateChat
import com.qiscus.multichannel.data.repository.QiscusChatRepository
import com.qiscus.sdk.chat.core.data.model.QMessage
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

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
        val comment = QMessage()
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