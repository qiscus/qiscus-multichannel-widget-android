package com.qiscus.multichannel.util

import com.qiscus.sdk.chat.core.data.model.QMessage
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PNUtilTest {

    @Test
    fun `shouldShowPn test text message`() {
        val comment = createComment("text", "Hello")

        assertTrue(PNUtil.shouldShowPn(comment, isChatRoomActive = false, isMyMessage = false))
    }

    @Test
    fun `shouldShowPn test system event`() {
        val comment = createComment(
            "system_event", "agen 11 Maker was added to this conversation by Admin"
        )

        assertFalse(PNUtil.shouldShowPn(comment, isChatRoomActive = false, isMyMessage = false))
    }

    @Test
    fun `shouldShowPn test chat room active`() {
        val comment = createComment("text", "Hello")

        assertFalse(PNUtil.shouldShowPn(comment, isChatRoomActive = true, isMyMessage = false))
    }

    @Test
    fun `shouldShowPn test my message`() {
        val comment = createComment("text", "Hello")

        assertFalse(PNUtil.shouldShowPn(comment, isChatRoomActive = false, isMyMessage = true))
    }

    private fun createComment(rawType: String, text: String) = QMessage().apply {
        this.rawType = rawType
        this.text = text
    }

}
