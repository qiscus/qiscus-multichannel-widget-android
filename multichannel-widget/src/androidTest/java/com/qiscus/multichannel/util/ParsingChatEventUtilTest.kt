package com.qiscus.multichannel.util

import com.qiscus.multichannel.R
import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.model.QAccount
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock

@ExtendWith(InstrumentationBaseTest::class)
internal class ParsingChatEventUtilTest : InstrumentationBaseTest() {

    private var util: ParsingChatEventUtil? = null
    private var core: QiscusCore? = null

    @BeforeAll
    fun setup() {
        setUpComponent()
        MockitoAnnotations.openMocks(this)
        core = mock()

        MultichannelConst.setQiscusCore(core)
        `when`(core!!.apps).thenReturn(application)

        util = ParsingChatEventUtil.instance
    }

    @AfterAll
    fun teardown() {
        tearDownComponent()
        util = null
        core = null
    }

    @Test
    fun parsingMessageSameIdTest() {
        val userId = "email@mail.com"

        val json = JSONObject().apply {
            put("type", "type1")
            put("subject_username", "userName")
            put("subject_email", userId)
        }

        val result = util?.parsingMessage(
            json, QAccount().apply {
                id = userId
            }
        )

        assertEquals(result, "You")
    }

    @Test
    fun parsingMessageNotSameIdTest() {
        val userId = "email@mail.com"

        val json = JSONObject().apply {
            put("type", "type1")
            put("subject_username", "userName")
            put("subject_email", userId)
        }

        val result = util?.parsingMessage(
            json, QAccount().apply {
                id = "222"
            }
        )

        assertEquals(result, "userName")
    }

    @Test
    fun getStringTest() {
        val getString = extractMethode(util!!, "getString", 1)
        val result = getString.call(util!!, R.string.qiscus_you_mc)
        assertEquals(result, "You")
    }

    @Test
    fun getStringTwoTest() {
        val getString = extractMethode(util!!, "getString", 2)
        val result = getString.call(util!!, R.string.hello_second_fragment, "10")
        assertEquals(result, "Hello second fragment. Arg: 10")
    }


}