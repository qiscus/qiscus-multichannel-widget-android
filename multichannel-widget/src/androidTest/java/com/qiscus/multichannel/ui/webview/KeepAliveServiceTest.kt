package com.qiscus.multichannel.ui.webview

import android.content.Intent
import org.junit.jupiter.api.Assertions.*

import com.qiscus.multichannel.basetest.InstrumentationBaseTest

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstrumentationBaseTest::class)
internal class KeepAliveServiceTest : InstrumentationBaseTest() {

    private lateinit var service: KeepAliveService

    @Test
    fun onBind() {
        service = KeepAliveService()
        service.onBind(Intent())
    }
}