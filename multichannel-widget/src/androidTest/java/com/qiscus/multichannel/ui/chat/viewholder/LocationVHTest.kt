package com.qiscus.multichannel.ui.chat.viewholder

import android.view.View
import android.widget.ImageView
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import com.qiscus.multichannel.R
import com.qiscus.multichannel.util.ResourceManager
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.data.model.QiscusLocation

@ExtendWith(BaseVHTest::class)
internal class LocationVHTest : BaseVHTest<LocationVH>(), BaseVHTest.ViewHolderForTest<LocationVH> {

    @BeforeAll
    fun setUp() {
        setViewHolderForTest(this)
        setUpComponent()
        ResourceManager.DIMEN_ROUNDED_IMAGE = ResourceManager.getDimen(application!!.resources.displayMetrics, 5).toInt()
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
    }

    private fun getMessageLocation(thumbnailUrls: String?) = getMessage(type = "location").apply {
        location = QiscusLocation().apply {
            thumbnailUrl = thumbnailUrls
        }
    }

    @Test
    fun bindMeTest() {
        setViewType(CommentsAdapter.TYPE_MY_LOCATION)
        val qMessage = getMessageLocation("https://www.location.com/thumbnailUrl.jpg")
        getViewHolder().run {
            runOnMainThread {
                bind(qMessage)
                val mapImageView = extractField<ImageView>(this, "mapImageView")
                mapImageView!!.performClick()
            }
        }
    }

    @Test
    fun bindMeThumbnailUrlsEmptyTest() {
        setViewType(CommentsAdapter.TYPE_MY_LOCATION)
        val qMessage = getMessageLocation("")
        runOnMainThread {
            getViewHolder().bind(qMessage)
        }
    }

    @Test
    fun bindOpponentTest() {
        setViewType(CommentsAdapter.TYPE_OPPONENT_LOCATION)
        val qMessage = getMessageLocation(null)
        runOnMainThread {
            getViewHolder().bind(qMessage)
        }
    }

    override fun getLayout(): Int = R.layout.item_opponent_location_mc

    override fun creteViewHolder(
        view: View,
        config: QiscusMultichannelWidgetConfig,
        color: QiscusMultichannelWidgetColor,
        viewType: Int
    ) = LocationVH(view, config, color, viewType)
}