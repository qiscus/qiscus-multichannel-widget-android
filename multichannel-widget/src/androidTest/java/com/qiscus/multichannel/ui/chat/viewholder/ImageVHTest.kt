package com.qiscus.multichannel.ui.chat.viewholder

import android.view.View
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(BaseVHTest::class)
internal class ImageVHTest : BaseVHTest<ImageVH>(), BaseVHTest.ViewHolderForTest<ImageVH> {

    private val qMessage = getMessage(
        textMessage = "[file] https://www.link.com/image.jpg [/file]",
        type = "file_attachment"
    )

    @BeforeAll
    fun setUp() {
        setViewHolderForTest(this)
        setViewType(CommentsAdapter.TYPE_MY_IMAGE)
        setUpComponent()
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
    }

    @Test
    fun bindTest() {
        withListener = true
        getViewHolder().apply {
            bind(qMessage)
            runOnMainThread {
                thumbnail.performClick()
            }
        }
    }

    @Test
    fun bindLongClickListenerTest() {
        withListener = true
        getViewHolder().apply {
            bind(qMessage)
            thumbnail.performLongClick()
        }
    }

    @Test
    fun bindLongClickListenerNullTest() {
        withListener = false
        getViewHolder().apply {
            bind(qMessage)
            thumbnail.performLongClick()
        }
    }

    override fun getLayout(): Int = R.layout.item_opponent_image_comment_mc

    override fun creteViewHolder(
        view: View,
        config: QiscusMultichannelWidgetConfig,
        color: QiscusMultichannelWidgetColor,
        viewType: Int
    ) = ImageVH(
        view, config, color, if (withListener) listener else null, viewType
    )

}