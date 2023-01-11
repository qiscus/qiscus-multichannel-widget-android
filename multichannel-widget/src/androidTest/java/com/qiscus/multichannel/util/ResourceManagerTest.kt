package com.qiscus.multichannel.util

import com.qiscus.multichannel.R
import org.junit.jupiter.api.Assertions.*

import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import com.qiscus.sdk.chat.core.QiscusCore

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock

@ExtendWith(InstrumentationBaseTest::class)
internal class ResourceManagerTest : InstrumentationBaseTest() {

    private var core: QiscusCore? = null

    @BeforeEach
    fun setUp() {
        setUpComponent()
        core = mock()

        `when`(core!!.apps).thenReturn(application)
        MultichannelConst.setQiscusCore(core)
    }

    @AfterEach
    fun tearDown() {
        tearDownComponent()
        core = null
    }

    @Test
    fun getRunnerTest() {
        ResourceManager.DIMEN_ROUNDED_IMAGE = ResourceManager.getDimen(application!!.resources.displayMetrics, 5).toInt()
        assertNotNull(ResourceManager.DIMEN_ROUNDED_IMAGE)

        var statusBar: Int? = null
        ResourceManager.PENDING_STATE_COLOR = ResourceManager.getColor(statusBar, R.color.qiscus_statusbar_mc)
        if (statusBar == null) {
            statusBar = R.color.qiscus_statusbar_mc
            ResourceManager.READ_STATE_COLOR = ResourceManager.getColor(statusBar, R.color.qiscus_statusbar_mc)
            ResourceManager.FAILED_STATE_COLOR = ResourceManager.READ_STATE_COLOR
        }
        assertNotNull(ResourceManager.FAILED_STATE_COLOR)

        ResourceManager.IC_SELECTED_BACKGROUND = ResourceManager.getBgEventDrawable(
            ResourceManager.DIMEN_ROUNDED_IMAGE, ResourceManager.FAILED_STATE_COLOR!!
        )

        ResourceManager.IC_CHAT_FROM_ME = ResourceManager.IC_SELECTED_BACKGROUND
        ResourceManager.IC_CHAT_FROM = ResourceManager.IC_CHAT_FROM_ME
        assertNotNull(ResourceManager.IC_CHAT_FROM)
    }

}