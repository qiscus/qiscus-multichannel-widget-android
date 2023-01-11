package com.qiscus.multichannel.util

import android.widget.EditText
import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstrumentationBaseTest::class)
internal class CommonUtilTest: InstrumentationBaseTest() {

    @BeforeAll
    fun setup() {
        setUpComponent()
    }

    @AfterAll
    fun teardown() {
        tearDownComponent()
    }

    @Test
    fun getAuthorityTest() {
        assertNotNull(getAuthority())
    }

    @Test
    fun afterTextChangedDelayedTest() {
       runOnMainThread {
           val et = EditText(context).apply {
               afterTextChangedDelayed({
                   // ignored
               }, {
                   // ignored
               })
           }

           et.setText("setText")
           et.setText("refresh Text")
       }
    }

}