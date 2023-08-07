package com.qiscus.multichannel.ui.chat.viewholder

import android.view.View
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(BaseVHTest::class)
internal class StickerVHTest : BaseVHTest<StickerVH>(), BaseVHTest.ViewHolderForTest<StickerVH> {

    @BeforeAll
    fun setUp() {
        setViewType(CommentsAdapter.TYPE_MY_STICKER)
        setViewHolderForTest(this)
        setUpComponent()
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
    }

    @Test
    fun bindTest() {
        val qMessage = getMessage(
            textMessage = "[sticker] https://www.link.com/sticker.gif [/sticker]",
            type = "custom"
        )
        runOnMainThread {
            getViewHolder().bind(qMessage)
        }
    }

    @Test
    fun bindNotStickerEndTest() {
        val qMessage = getMessage(
            textMessage = "https://www.link.com/sticker.gif [/sticker]",
            type = "custom"
        )
        runOnMainThread {
            getViewHolder().bind(qMessage)
        }
    }

    @Test
    fun bindNotStickerFirsfTest() {
        val qMessage = getMessage(
            textMessage = "[sticker] https://www.link.com/sticker.gif",
            type = "custom"
        )
        runOnMainThread {
            getViewHolder().bind(qMessage)
        }
    }

    @Test
    fun bindFileTest() {
        val qMessage = getMessage(
            textMessage = "[file] https://www.link.com/sticker.gif [/file]",
            type = "custom"
        )
        runOnMainThread {
            getViewHolder().bind(qMessage)
        }
    }

    override fun getLayout(): Int = R.layout.item_opponent_sticker_mc

    override fun creteViewHolder(
        view: View,
        config: QiscusMultichannelWidgetConfig,
        color: QiscusMultichannelWidgetColor,
        viewType: Int
    ) = StickerVH(view, config, color)
}