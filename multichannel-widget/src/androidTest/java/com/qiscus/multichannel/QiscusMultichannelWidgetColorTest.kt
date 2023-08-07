package com.qiscus.multichannel

import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.sdk.chat.core.QiscusCore
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock

@ExtendWith(InstrumentationBaseTest::class)
internal class QiscusMultichannelWidgetColorTest : InstrumentationBaseTest() {

    private var color: QiscusMultichannelWidgetColor? = null

    @BeforeAll
    fun setUp() {
        setUpComponent()
        MockitoAnnotations.openMocks(this)
        val core: QiscusCore = mock()
        MultichannelConst.setQiscusCore(core)
        `when`(MultichannelConst.qiscusCore()?.apps).thenReturn(application!!)

        color = QiscusMultichannelWidgetColor()
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
        color = null
    }

    @Test
    fun setTest() {
        color!!.setStatusBarColor(R.color.colorPrimary)
            .setNavigationColor(R.color.colorPrimary)
            .setNavigationTitleColor(R.color.colorPrimary)
            .setBaseColor(R.color.colorPrimary)
            .setEmptyBacgroundColor(R.color.colorPrimary)
            .setEmptyTextColor(R.color.colorPrimary)
            .setTimeBackgroundColor(R.color.colorPrimary)
            .setTimeLabelTextColor(R.color.colorPrimary)
            .setLeftBubbleColor(R.color.colorPrimary)
            .setRightBubbleColor(R.color.colorPrimary)
            .setLeftBubbleTextColor(R.color.colorPrimary)
            .setRightBubbleTextColor(R.color.colorPrimary)
            .setFieldChatBorderColor(R.color.colorPrimary)
            .setSystemEventTextColor(R.color.colorPrimary)
            .setSendContainerColor(R.color.colorPrimary)
            .setSendContainerBackgroundColor(R.color.colorPrimary)
    }

    @Test
    fun getTest() {
        color!!.getStatusBarColor()
        color!!.getNavigationColor()
        color!!.getNavigationTitleColor()
        color!!.getBaseColor()
        color!!.getEmptyBacgroundColor()
        color!!.getEmptyTextColor()
        color!!.getTimeBackgroundColor()
        color!!.getTimeLabelTextColor()
        color!!.getLeftBubbleColor()
        color!!.getRightBubbleColor()
        color!!.getLeftBubbleTextColor()
        color!!.getRightBubbleTextColor()
        color!!.getFieldChatBorderColor()
        color!!.getSystemEventTextColor()
        color!!.getSendContainerColor()
        color!!.getSendContainerBackgroundColor()
    }
}