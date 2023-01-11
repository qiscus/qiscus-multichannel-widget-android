package com.qiscus.multichannel.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

internal class WidgetFileUtilTest {

    @Test
    fun runTest() {
        WidgetFileUtil()
        val isVideo = WidgetFileUtil.isVideo(File("name.mp4"))
        assertTrue(isVideo!!)
    }

    @Test
    fun runNotVideoTest() {
        val isVideo = WidgetFileUtil.isVideo(File("name.txt"))
        assertFalse(isVideo!!)
    }

    @Test
    fun runErrorTest() {
        WidgetFileUtil.isVideo(File("name.txtbsabs"))
    }
}