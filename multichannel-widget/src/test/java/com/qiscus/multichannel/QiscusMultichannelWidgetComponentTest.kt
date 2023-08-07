package com.qiscus.multichannel

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class QiscusMultichannelWidgetComponentTest {

    private var component: QiscusMultichannelWidgetComponent? = null

    @BeforeAll
    fun setUp() {
        component = QiscusMultichannelWidgetComponent().create(true)
    }

    @AfterAll
    fun tearDown() {
        component = null
    }

    @Test
    fun getChatroomRepository() {
        val repo = component?.getChatroomRepository()
        assertNotNull(repo)
    }

    @Test
    fun getQiscusChatRepository() {
        val repo = component?.getQiscusChatRepository()
        assertNotNull(repo)
    }
}