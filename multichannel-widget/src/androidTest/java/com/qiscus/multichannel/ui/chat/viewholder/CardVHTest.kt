package com.qiscus.multichannel.ui.chat.viewholder

import android.text.SpannableString
import android.view.View
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.multichannel.util.ResourceManager
import com.qiscus.sdk.chat.core.data.model.QMessage
import org.json.JSONObject

@ExtendWith(BaseVHTest::class)
internal class CardVHTest : BaseVHTest<CardVH>(), BaseVHTest.ViewHolderForTest<CardVH> {

    @BeforeAll
    fun setUp() {
        setViewType(CommentsAdapter.TYPE_CARD)
        setViewHolderForTest(this)
        setUpComponent()
        ResourceManager.DIMEN_ROUNDED_IMAGE = ResourceManager.getDimen(application!!.resources.displayMetrics, 5).toInt()
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
    }

    @Test
    fun bindTest() {
        val qMessage = getMessage().apply {
            payload = "{ " +
                    "\"title\" :\"title text\", " +
                    "\"description\" :\"description text\", " +
                    "\"image\" :\"https://www.image.com/image.jpg\", " +
                    "\"buttons\" : [" +
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
        runOnMainThread {
            getViewHolder().bind(qMessage)
        }
    }

    @Test
    fun onChatButtonClickTest() {
        withListener = true
        getViewHolder().onChatButtonClick(
            JSONObject("{" +
                    "\"type\" : \"postback\" ," +
                    "\"label\" : \"text\", " +
                    "\"postback_text\" : \"text\", " +
                    "\"payload\" :  { \"url\" : \"https://www.url.com\"}" +
                    "}")
        )
    }

    @Test
    fun sendCommentTest() {
        withListener = false
        getViewHolder().run {
            val sendComment = extractMethode(this, "sendComment")
            sendComment.call(this, QMessage())
        }
    }

    @Test
    fun onSpanResultTest() {
        getViewHolder().onSpanResult( SpannableString.valueOf("text"))
    }

    override fun getLayout(): Int = R.layout.item_card_mc

    override fun creteViewHolder(
        view: View,
        config: QiscusMultichannelWidgetConfig,
        color: QiscusMultichannelWidgetColor,
        viewType: Int
    ) = CardVH(view, config, color, if (withListener) listener else null)
}