package com.qiscus.multichannel.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class MultichannelQMessageUtilsTest {

    @Test
    fun runNullTest() {
        MultichannelQMessageUtils()
        val name = MultichannelQMessageUtils.getFileName(null)
        assertEquals(name, "")
    }

    @Test
    fun runEmptyTest() {
        val name = MultichannelQMessageUtils.getFileName("msg")
        assertEquals(name, "")
    }

    @Test
    fun runTest() {
        val name = MultichannelQMessageUtils.getFileName("[file] ok/fileName.mp4 [/file]").trim()
        assertEquals(name, "fileName.mp4")
    }

    @Test
    fun runStickerTest() {
        val name = MultichannelQMessageUtils.getFileName("[sticker] ok/fileName.gif [/sticker]").trim()
        assertEquals(name, "fileName.gif")
    }

}