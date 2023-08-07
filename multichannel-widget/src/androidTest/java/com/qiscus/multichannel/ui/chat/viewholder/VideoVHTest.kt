package com.qiscus.multichannel.ui.chat.viewholder

import android.view.View
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import org.junit.jupiter.api.Assertions.*

import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.sdk.chat.core.data.model.QMessage
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(BaseVHTest::class)
internal class VideoVHTest : BaseVHTest<VideoVH>(), BaseVHTest.ViewHolderForTest<VideoVH> {

    private val qMessage = getMessage(
        textMessage = "[file] https://www.link.com/image.mp4 [/file]",
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
        getViewHolder().run {
            bind(qMessage)
            runOnMainThread {
                thumbnail.performClick()

                thumbnail.performLongClick()
            }
        }
    }
    @Test
    fun bindWithoutListenerTest() {
        withListener = false
        getViewHolder().run {
            bind(qMessage)
            runOnMainThread {
                thumbnail.performLongClick()
            }
        }
    }

    override fun getLayout(): Int = R.layout.item_opponent_video_comment_mc

    override fun creteViewHolder(
        view: View,
        config: QiscusMultichannelWidgetConfig,
        color: QiscusMultichannelWidgetColor,
        viewType: Int
    ) = VideoVH(view, config, color, if (withListener) listener else null, viewType)
}