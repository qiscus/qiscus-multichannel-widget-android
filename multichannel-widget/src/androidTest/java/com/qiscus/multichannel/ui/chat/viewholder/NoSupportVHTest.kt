package com.qiscus.multichannel.ui.chat.viewholder

import android.view.View
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter

@ExtendWith(BaseVHTest::class)
internal class NoSupportVHTest : BaseVHTest<NoSupportVH>(), BaseVHTest.ViewHolderForTest<NoSupportVH> {

    @BeforeAll
    fun setUp() {
        setViewType(CommentsAdapter.TYPE_NOT_SUPPORT)
        setViewHolderForTest(this)
        setUpComponent()
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
    }

    @Test
    fun bindTest() {
        getViewHolder().bind(
            getMessage(type = "not_support")
        )
    }

    override fun getLayout(): Int = R.layout.item_message_not_supported_mc

    override fun creteViewHolder(
        view: View,
        config: QiscusMultichannelWidgetConfig,
        color: QiscusMultichannelWidgetColor,
        viewType: Int
    ) = NoSupportVH(view, config, color)

}