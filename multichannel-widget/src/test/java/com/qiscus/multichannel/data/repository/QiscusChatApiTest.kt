package com.qiscus.multichannel.data.repository

import org.junit.jupiter.api.Test

internal class QiscusChatApiTest {

    @Test
    fun createTest() {
        val isEnableLog = false
        QiscusChatApi.create(isEnableLog)
    }
}