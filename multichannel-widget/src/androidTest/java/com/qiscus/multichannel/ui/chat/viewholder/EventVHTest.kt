package com.qiscus.multichannel.ui.chat.viewholder

import android.view.View
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import org.mockito.kotlin.whenever

@ExtendWith(BaseVHTest::class)
internal class EventVHTest : BaseVHTest<EventVH>(), BaseVHTest.ViewHolderForTest<EventVH> {

    private val qMessage = getMessage(
        textMessage = "System Event Message!",
        type = "system_event"
    )
    @BeforeAll
    fun setUp() {
        setViewType(CommentsAdapter.TYPE_EVENT)
        setViewHolderForTest(this)
        setUpComponent()
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
    }

    @Test
    fun bindTest() {
        config.setShowSystemMessage(true)
        getViewHolder().bind(qMessage)
    }

    @Test
    fun bindHildeUiTest() {
        config.setShowSystemMessage(false)
        getViewHolder().bind(qMessage)
    }

    override fun getLayout(): Int = R.layout.item_event_mc

    override fun creteViewHolder(
        view: View,
        config: QiscusMultichannelWidgetConfig,
        color: QiscusMultichannelWidgetColor,
        viewType: Int
    ) = EventVH(view, config, color)
}