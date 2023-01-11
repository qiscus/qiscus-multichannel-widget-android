package com.qiscus.multichannel.util

import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstrumentationBaseTest::class)
internal class QiscusConverterUtilTest : InstrumentationBaseTest() {

    @BeforeAll
    fun setup() {
        setUpComponent()
    }

    @AfterAll
    fun teardown() {
        tearDownComponent()
    }

    @Test
    fun dp2pxTest() {
        val twoDp = QiscusConverterUtil.dp2px(application!!.resources, 2F)
        assertNotNull(twoDp)
    }

    @Test
    fun sp2pxTest() {
        val twoSp = QiscusConverterUtil.sp2px(application!!.resources, 2F)
        assertNotNull(twoSp)
    }
}