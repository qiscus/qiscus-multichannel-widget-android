package com.qiscus.multichannel.util

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TemplateUtilTest {

    private var util: TemplateUtil? = null

    @BeforeEach
    fun setUp() {
        util = TemplateUtil()
    }

    @AfterEach
    fun tearDown() {
        util = null
    }

    @Test
    fun getDefaultTemplateTest() {
        val list = TemplateUtil.getDefaultTemplate()
        assertEquals("Halo", list[0])
    }

    @Test
    fun getCustomerTemplateTest() {
        val list = TemplateUtil.getCustomerTemplate()
        assertEquals("Halo", list[0])
    }

    @Test
    fun generateTemplateTest() {
        val result = TemplateUtil.generateTemplate("Hallo")
        assertEquals("{ \"template\" : Hallo }", result)
    }
}