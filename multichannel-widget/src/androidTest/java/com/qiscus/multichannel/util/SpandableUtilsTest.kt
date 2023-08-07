package com.qiscus.multichannel.util

import android.text.SpannableString
import android.view.View
import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import org.junit.jupiter.api.Assertions.*

import com.qiscus.multichannel.ui.chat.viewholder.BaseVHTest
import org.junit.jupiter.api.AfterAll

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstrumentationBaseTest::class)
internal class SpandableUtilsTest : InstrumentationBaseTest() {

    private lateinit var spannableUtils: SpannableUtils

    @BeforeAll
    fun setUp() {
        setUpComponent()
    }

    @BeforeEach
    fun before() {
        setActivity()
        spannableUtils = SpannableUtils(activity!!, getListener())
    }

    private fun getListener() = object : SpannableUtils.ClickSpan.OnSpanListener {
        override fun onSpanResult(spanText: SpannableString) {
            //ignore
        }

    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
    }

    @Test
    fun setUpWithFirstAttTest() {
        val text = "@text.com"
        spannableUtils.setUpLinks(text)
    }

    @Test
    fun setUpWithAttTest() {
        val text = "w@text.com"
        spannableUtils.setUpLinks(text)
    }

    @Test
    fun setUpTest() {
        val text = "text.com"
        spannableUtils.setUpLinks(text)
    }

    @Test
    fun clickifyMinusTest() {
        val text = "text"
        val clickify = extractMethode(spannableUtils, "clickify")
        clickify.call(spannableUtils, text, -1, text.length - 1)
    }

    @Test
    fun clickSpanTest() {
        val click = SpannableUtils.ClickSpan(getClickListener())
        click.onClick(View(context))
    }

    @Test
    fun handleClickTest() {
        val text = "https://www.link.com"
        val handleClick = extractMethode(spannableUtils, "handleClick")
        val click = handleClick.call(spannableUtils, text, 0, text.length -1)  as SpannableUtils.ClickSpan.OnClickListener
        click.onClick()
    }

    @Test
    fun handleClickHttpsNotExisrTest() {
        val text = "www.link.com"
        val handleClick = extractMethode(spannableUtils, "handleClick")
        val click = handleClick.call(spannableUtils, text, 0, text.length -1)  as SpannableUtils.ClickSpan.OnClickListener
        click.onClick()
    }

    private fun getClickListener() = object : SpannableUtils.ClickSpan.OnClickListener {
        override fun onClick() {
            // ignored
        }

    }
}