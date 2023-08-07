package com.qiscus.multichannel.ui.chat.image

import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.qiscus.multichannel.R
import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import com.qiscus.multichannel.data.model.ImageToSend
import com.qiscus.nirmana.Nirmana
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstrumentationBaseTest::class)
internal class ImagePagerAdapterTest : InstrumentationBaseTest() {

    private lateinit var adapter: ImagePagerAdapter
    private val dataList: MutableList<ImageToSend> = arrayListOf()
    private lateinit var parent: LinearLayout

    @BeforeAll
    fun setUp() {
        setUpComponent()

        Nirmana.init(application!!)

        dataList.add(ImageToSend(path = "path"))
        parent = LinearLayout(context)
    }

    @BeforeEach
    fun before() {
        setActivity()

        val displayMetrics = DisplayMetrics()
        activity!!.display?.getRealMetrics(displayMetrics)

        adapter = ImagePagerAdapter(
            activity!!, getView(), dataList, displayMetrics.widthPixels
        )
    }

    private fun getView() = object : ImagePagerAdapter.ImagePagerListener {
        override fun onItemClick(position: Int) {
            // ignored
        }

    }

    @AfterAll
    fun tearDown() {
        val iv = ImageView(context)
        parent.addView(iv)
        adapter.destroyItem(
            parent, 0, iv
        )
    }

    @Test
    fun getCountTest() {
        assertEquals(
            1, adapter.count
        )
    }

    @Test
    fun getCountAddImageTest() {
        dataList.add(ImageToSend(path = context!!.getString(R.string.add_image)))

        assertEquals(
            1, adapter.count
        )
    }

    @Test
    fun isViewFromObjectTest() {
        val v = View(context)
        assertTrue(
            adapter.isViewFromObject(v, v)
        )
    }

    @Test
    fun isViewFromObjectFalseTest() {
        val v = View(context)
        val s = "text"
        assertFalse(
            adapter.isViewFromObject(v, s)
        )
    }

    @Test
    fun instantiateItemTest() {
        runOnMainThread {
            adapter.instantiateItem(parent, 0)
        }
    }
}