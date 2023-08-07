package com.qiscus.multichannel.ui.chat.viewholder

import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExtendWith(BaseVHTest::class)
internal class CarouselVHTest : BaseVHTest<CarouselVH>(), BaseVHTest.ViewHolderForTest<CarouselVH> {

    @BeforeAll
    fun setUp() {
        setViewType(CommentsAdapter.TYPE_CAROUSEL)
        setViewHolderForTest(this)
        setUpComponent()
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
    }

    @Test
    fun bindTest() {
        val qMessage = getMessage().apply {
            payload = "{" +
                    "\"cards\" : [ " +
                    "{" +
                    "\"type\" : \"postback\" ," +
                    "\"postback_text\" : \"text\", " +
                    "\"payload\" :  { \"url\" : \"https://www.url.com\"}" +
                    "}" +
                    "] }"
        }
        getViewHolder().bind(qMessage)
    }

    @Test
    fun onScrollListenerTest() {
        val onScrollReady = true
        val rv = RecyclerView(context!!)
        rv.layoutManager = LinearLayoutManager(context)

        getViewHolder().run {
            val onScrollListener = extractMethode(this, "onScrollListener")
            val listener = onScrollListener.call(this, onScrollReady) as  RecyclerView.OnScrollListener
            listener.onScrolled(rv, 0, 0)
        }
    }

    @Test
    fun onScrollListenerFalseTest() {
        val onScrollReady = false
        getViewHolder().run {
            val onScrollListener = extractMethode(this, "onScrollListener")
            val listener = onScrollListener.call(this, onScrollReady) as  RecyclerView.OnScrollListener
            listener.onScrolled(RecyclerView(context!!), 0, 0)
        }
    }

    @Test
    fun findPositionTest() {
        val lm = mock<LinearLayoutManager>()
        whenever(lm.findFirstCompletelyVisibleItemPosition()).thenReturn(0)

        getViewHolder().run {
            val findPosition = extractMethode(this, "findPosition")
            val position = findPosition.call(this, lm) as Int
            assertEquals(position, 0)
        }
    }


    @Test
    fun findPositionVisibleItemPositionTest() {
        val lm = mock<LinearLayoutManager>()
        whenever(lm.findFirstCompletelyVisibleItemPosition()).thenReturn(1)
        whenever(lm.findLastCompletelyVisibleItemPosition()).thenReturn(10)

        getViewHolder().run {
            val findPosition = extractMethode(this, "findPosition")
            val position = findPosition.call(this, lm) as Int
            assertEquals(position, 10)
        }
    }

    override fun getLayout(): Int = R.layout.item_carousel_mc

    override fun creteViewHolder(
        view: View,
        config: QiscusMultichannelWidgetConfig,
        color: QiscusMultichannelWidgetColor,
        viewType: Int
    ) = CarouselVH(view, config, color, if (withListener) listener else null)
}