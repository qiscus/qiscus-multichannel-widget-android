package com.qiscus.multichannel.ui.chat.image

import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestOptions
import com.qiscus.multichannel.R
import com.qiscus.multichannel.data.model.ImageToSend
import com.qiscus.nirmana.Nirmana

class ImagePagerAdapter(
    private val context: Context,
    private val view: ImagePagerListener,
    private val dataList: List<ImageToSend>,
    private val imageWidth: Int
) : PagerAdapter() {

    private val requestOption: RequestOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.DATA)
        .format(DecodeFormat.PREFER_RGB_565)
        .dontAnimate()
        .dontTransform()
        .downsample(DownsampleStrategy.NONE)
        .placeholder(R.drawable.bg_image_before_download)

    override fun getCount(): Int {
        return if (dataList[dataList.size - 1].path == context.getString(R.string.add_image)) {
            dataList.size - 1
        } else dataList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val iv = QiscusTouchImageView(context).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = ViewGroup.LayoutParams(
                imageWidth,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setOnClickListener { view.onItemClick(position) }
        }

        Nirmana.getInstance().get()
            .load(Uri.parse("file://" + dataList[position].path))
            .apply(requestOption)
            .into(iv)

        container.addView(iv, 0)
        return iv
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        Nirmana.getInstance().get().clear((`object` as ImageView))
        container.removeView(`object`)
    }

    interface ImagePagerListener {
        fun onItemClick(position: Int)
    }

}