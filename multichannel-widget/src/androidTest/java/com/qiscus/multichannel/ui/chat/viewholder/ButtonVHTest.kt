package com.qiscus.multichannel.ui.chat.viewholder

import android.view.View
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(BaseVHTest::class)
internal class ButtonVHTest : BaseVHTest<ButtonVH>(), BaseVHTest.ViewHolderForTest<ButtonVH> {

    @BeforeAll
    fun setUp() {
        setViewType(CommentsAdapter.TYPE_BUTTON)
        setViewHolderForTest(this)
        setUpComponent()
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
    }

    @Test
    fun bindPostBackTest() {
        val qMessage = getMessage().apply {
            payload = "{ \"buttons\" : [" +
                    "{" +
                    "\"type\" : \"postback\" ," +
                    "\"label\" : \"text\", " +
                    "\"postback_text\" : \"text\", " +
                    "\"payload\" :  { \"url\" : \"https://www.url.com\"}" +
                    "}, {" +
                    "\"type\" : \"link\", " +
                    "\"payload\" :  { \"url\" : \"https://www.url.com\"}" +
                    "}, {" +
                    "\"type\" : \"not_found\"" +
                    "}" +
                    "] }"
        }
        getViewHolder().bind(qMessage)
    }

    @Test
    fun bindErrorTest() {
        val qMessage = getMessage().apply {
            payload = "{ \"buttons\" : [" +
                    "\"type\" : \"not_found\"" +
                    "] }"
        }
        getViewHolder().bind(qMessage)
    }

    @Test
    fun bindNot_foundTest() {
        withListener = false
        val qMessage = getMessage().apply {
            payload = "{ \"buttons\" : [" +
                    "{ \"type\" : \"not_found\" }" +
                    "] }"
        }
        getViewHolder().run {
            bind(qMessage)
            val sendComment = extractMethode(this, "sendComment")
            sendComment.call(this, qMessage)
        }
    }

    @Test
    fun bindEmptyTest() {
        val qMessage = getMessage().apply {
            payload = "{ \"buttons\" : [ ] }"
        }
        getViewHolder().bind(qMessage)
    }

    @Test
    fun onChatButtonClickPostBackTest() {
        withListener = true
        val payload = "{" +
                "\"type\" : \"postback\" ," +
                "\"label\" : \"text\", " +
                "\"postback_text\" : \"text\", " +
                "\"payload\" :  { \"url\" : \"https://www.url.com\"}" +
                "}"
        val jsonObject = JSONObject(payload)
        getViewHolder().onChatButtonClick(jsonObject)
    }

    @Test
    fun onChatButtonClickLinkTest() {
        val payload = "{" +
                "\"type\" : \"link\", " +
                "\"payload\" :  { \"url\" : \"https://www.url.com\"}" +
                "}"
        val jsonObject = JSONObject(payload)
        getViewHolder().onChatButtonClick(jsonObject)
    }

    @Test
    fun onChatButtonClickNotFoundTest() {
        val payload = "{" +
                "\"type\" : \"not_found\"" +
                "}"
        val jsonObject = JSONObject(payload)
        getViewHolder().onChatButtonClick(jsonObject)
    }

    override fun getLayout(): Int = R.layout.item_button_mc

    override fun creteViewHolder(
        view: View,
        config: QiscusMultichannelWidgetConfig,
        color: QiscusMultichannelWidgetColor,
        viewType: Int
    ) = ButtonVH(view, config, color, if (withListener) listener else null)
}