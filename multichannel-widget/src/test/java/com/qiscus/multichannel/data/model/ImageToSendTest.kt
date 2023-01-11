package com.qiscus.multichannel.data.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ImageToSendTest {

    @Test
    fun setTest() {
        val imageToSend = ImageToSend("path", "value")
        assertEquals(imageToSend.path, "path")
        assertEquals(imageToSend.caption, "value")
    }
}