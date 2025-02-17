package com.qiscus.multichannel.ui.chat.image

import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.R
import org.junit.jupiter.api.Assertions.*

import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import com.qiscus.multichannel.data.model.ImageToSend
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.nirmana.Nirmana
import com.qiscus.sdk.chat.core.QiscusCore
import org.junit.jupiter.api.AfterAll

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExtendWith(InstrumentationBaseTest::class)
internal class ImagePreviewAdapterTest : InstrumentationBaseTest() {

    private lateinit var adapter: ImagePreviewAdapter
    private val dataList: MutableList<ImageToSend> = arrayListOf()
    private lateinit var parent: LinearLayout

    @BeforeAll
    fun setUp() {
        setUpComponent()

        MockitoAnnotations.openMocks(this)

        val core: QiscusCore = mock()
        whenever(core.apps).thenReturn(application!!)
        MultichannelConst.setQiscusCore(core)

        Nirmana.init(application!!)
        parent = LinearLayout(context)
    }

    @BeforeEach
    fun before() {
        setActivity()

        val displayMetrics = DisplayMetrics()
        activity!!.display?.getRealMetrics(displayMetrics)

        adapter = ImagePreviewAdapter(
            activity!!, getListener(), dataList, displayMetrics.widthPixels, QiscusMultichannelWidgetColor()
        )
        adapter.selectedPosition = 0
    }

    private fun getListener() = object : ImagePreviewAdapter.ImagePreviewListener {
        override fun onImagePreviewClick(position: Int) {
            // ignored
        }

        override fun onAddImage() {
            // ignored
        }

        override fun onItemDelete(position: Int) {
            // ignored
        }

    }

    @AfterAll
    fun tearDown() {
        adapter.clearOtherSelected(0)
        adapter.clearSelected(0)
    }

    @Test
    fun getItemCountTest() {
        addItem(2, true)
        assertEquals(
            adapter.itemCount, dataList.size
        )
    }

    @Test
    fun getItemCountMoreThen30LastNotAddImageTest() {
        addItem(32, false)
        assertEquals(
            adapter.itemCount, dataList.size
        )
    }

    @Test
    fun getItemCountMoreThen30Test() {
        addItem(31, true)
        assertEquals(
            adapter.itemCount, dataList.size - 1
        )
    }

    private fun addItem(count: Int, isAddImage: Boolean) {
        dataList.clear()

        for (i in 0 until count) {
            dataList.add(ImageToSend(path = "path_$i"))
            adapter.notifyItemInserted(0)
        }
        if (isAddImage) {
            dataList.add(ImageToSend(path = context!!.getString(R.string.add_image)))
            adapter.notifyItemInserted(0)
        }

    }

    @Test
    fun onCreateViewHolderTest() {
        adapter.onCreateViewHolder(parent, 1)
    }

    @Test
    fun onBindViewHolderTest() {
        runOnMainThread {
            addItem(2, false)
            val holder = ImagePreviewAdapter.Holder(
                LayoutInflater.from(activity).inflate(R.layout.item_image_preview_mc, parent, false),
                QiscusMultichannelWidgetColor()
            )
            adapter.onBindViewHolder(holder, 1)

            val ivClick = extractField<ImageView>(holder, "imgPrev")
            ivClick?.performClick()
        }
    }

    @Test
    fun onBindViewHolderNotBindTest() {
        runOnMainThread {
            addItem(2, true)
            val holder = ImagePreviewAdapter.Holder(
                LayoutInflater.from(activity).inflate(R.layout.item_image_preview_mc, parent, false),
                QiscusMultichannelWidgetColor()
            )
            adapter.onBindViewHolder(holder, 2)
        }
    }

    @Test
    fun onBindViewHolderLastNotAddImageSize32Test() {
        runOnMainThread {
            addItem(32, true)
            val holder = ImagePreviewAdapter.Holder(
                LayoutInflater.from(activity).inflate(R.layout.item_image_preview_mc, parent, false),
                QiscusMultichannelWidgetColor()
            )
            adapter.onBindViewHolder(holder, dataList.size - 1)
        }
    }

    @Test
    fun onBindViewHolderLastNotAddImageTest() {
        runOnMainThread {
            addItem(2, true)
            val holder = ImagePreviewAdapter.Holder(
                LayoutInflater.from(activity).inflate(R.layout.item_image_preview_mc, parent, false),
                QiscusMultichannelWidgetColor()
            )
            adapter.onBindViewHolder(holder, 0)
        }
    }
}