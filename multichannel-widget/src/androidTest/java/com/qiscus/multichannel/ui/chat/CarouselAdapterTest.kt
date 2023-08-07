package com.qiscus.multichannel.ui.chat

import android.view.View
import android.widget.LinearLayout
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import org.junit.jupiter.api.Assertions.*

import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import com.qiscus.multichannel.ui.chat.viewholder.BaseViewHolder
import com.qiscus.multichannel.ui.webview.CustomTabsHelper
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.util.QiscusConst
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

@ExtendWith(InstrumentationBaseTest::class)
internal class CarouselAdapterTest : InstrumentationBaseTest() {

    private var adapter: CarouselAdapter? = null
    private lateinit var config: QiscusMultichannelWidgetConfig
    private val color = QiscusMultichannelWidgetColor()
    private lateinit var parentView: LinearLayout

    @BeforeAll
    fun setUp() {
        setUpComponent()
        MockitoAnnotations.openMocks(this)
        config = mock()

        parentView = LinearLayout(context)

        val core: QiscusCore = mock()
        whenever(core.apps).thenReturn(application!!)

        MultichannelConst.setQiscusCore(core)
        QiscusConst.setApps(application!!)

        adapter = CarouselAdapter(
            config, color, getData(), getQMessage(), getListener()
        )
    }

    private fun getQMessage() = QMessage().apply {
        id = 101
        chatRoomId = 100
        text = "text"
        rawType = "text"
        timestamp = Date()
    }

    private fun getData() = JSONArray().apply {
        put(JSONObject())
    }

    private fun getListener() =
        object : CommentsAdapter.ItemViewListener {
            override fun onSendComment(comment: QMessage) {
            }

            override fun onItemClick(view: View, position: Int) {
            }

            override fun onItemLongClick(view: View, position: Int) {
            }

            override fun onItemReplyClick(view: View, comment: QMessage) {
            }

            override fun stopAnotherAudio(comment: QMessage) {
            }

        }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
        adapter = null
    }

    @Test
    fun onCreateViewHolder() {
        adapter?.onCreateViewHolder(
            parentView, viewType = 1
        )
    }

    @Test
    fun getItemCount() {
        adapter?.itemCount
    }

    @Test
    fun onBindViewHolder() {
        val holder = BaseViewHolder(
            parentView.rootView, config, color
        )

        adapter?.onBindViewHolder(holder, position = 0)
    }
}