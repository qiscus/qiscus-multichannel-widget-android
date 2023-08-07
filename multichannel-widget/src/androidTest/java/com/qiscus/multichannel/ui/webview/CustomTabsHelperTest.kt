package com.qiscus.multichannel.ui.webview

import android.content.Intent
import org.junit.jupiter.api.Assertions.*

import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import org.junit.jupiter.api.AfterAll

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstrumentationBaseTest::class)
internal class CustomTabsHelperTest : InstrumentationBaseTest() {

    @BeforeAll
    fun setUp() {
        setUpComponent()
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
    }

    @Test
    fun addKeepAliveExtraTtest() {
        CustomTabsHelper.addKeepAliveExtra(context!!, Intent())
    }

    @Test
    fun getPackageNameToUseTest() {
        CustomTabsHelper.getPackageNameToUse(context!!)
    }

    @Test
    fun getPackageNameToUseExistTest() {
        CustomTabsHelper.getPackageNameToUse(context!!)
    }

    @Test
    fun getPackagesTest() {
        CustomTabsHelper.packages
    }

    @Test
    fun hasSpecializedHandlerIntentsTest() {
        val handler = extractMethode(CustomTabsHelper, "hasSpecializedHandlerIntents")
        handler.call(CustomTabsHelper, context!!, Intent())
    }
}