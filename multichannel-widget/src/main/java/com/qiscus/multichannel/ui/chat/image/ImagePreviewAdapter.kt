package com.qiscus.multichannel.ui.chat.image

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.R
import com.qiscus.multichannel.data.model.ImageToSend
import com.qiscus.nirmana.Nirmana

class ImagePreviewAdapter(
    private val context: Context,
    private val listener: ImagePreviewListener,
    private val dataList: List<ImageToSend>,
    private val imageWidth: Int,
    private val color: QiscusMultichannelWidgetColor
) : RecyclerView.Adapter<ImagePreviewAdapter.Holder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    var selectedPosition = 0

    fun clearOtherSelected(position: Int) {
        selectedPosition = position
        for (i in dataList.indices) {
            if (position != i) notifyItemChanged(i)
        }
    }

    fun clearSelected(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(inflater.inflate(R.layout.item_image_preview_mc, parent, false), color)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.also {
            it.setListener(listener)
            if (dataList[position].path != context.getString(R.string.add_image)) {
                it.bind(dataList[position].path, imageWidth, position, this@ImagePreviewAdapter)
                it.onItemSelected(selectedPosition)
            } else if (dataList.size < 31) {
                it.bind()
            }
        }
    }

    override fun getItemCount(): Int {
        return if (dataList.size > 30
            && dataList[dataList.size - 1].path == context.getString(R.string.add_image)
        ) {
            dataList.size - 1
        } else dataList.size
    }

    interface ImagePreviewListener {
        fun onImagePreviewClick(position: Int)
        fun onAddImage()
        fun onItemDelete(position: Int)
    }

    class Holder(val view: View, color: QiscusMultichannelWidgetColor) :
        RecyclerView.ViewHolder(view) {

        private var backgroundImage: View = view.findViewById(R.id.backgroundImage)
        private var btnAddImage: AppCompatImageView = view.findViewById(R.id.btnAddImage)
        private var imgPrev: AppCompatImageView = view.findViewById(R.id.imgPrev)
        private var btnDelete: View = view.findViewById(R.id.btnDelete)
        private var itemPosition: Int = 0
        private var listener: ImagePreviewListener? = null

        init {
            backgroundImage.setBackgroundColor(color.getNavigationColor())
            btnAddImage.setColorFilter(color.getNavigationColor())
        }

        fun setListener(listener: ImagePreviewListener?) {
            this.listener = listener
        }

        fun bind(
            imagePath: String?,
            imageWidth: Int,
            itemPosition: Int,
            adapter: ImagePreviewAdapter
        ) {
            this.itemPosition = itemPosition

            btnAddImage.visibility = View.GONE
            imgPrev.visibility = View.VISIBLE
            btnDelete.visibility = View.VISIBLE

            Nirmana.getInstance().get()
                .asBitmap()
                .thumbnail(0.3f)
                .apply(
                    RequestOptions()
                        .format(DecodeFormat.PREFER_RGB_565)
                        .skipMemoryCache(false)
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .override(150, 150)
                        .error(R.drawable.jupuk_image_placeholder)
                )
                .load(Uri.parse("file://$imagePath"))
                .into(imgPrev)

            imgPrev.setOnClickListener {
                adapter.clearOtherSelected(itemPosition)
                listener!!.onImagePreviewClick(itemPosition)
                onItemSelected(itemPosition)
            }

            btnDelete.setOnClickListener { listener!!.onItemDelete(itemPosition) }
        }

        fun bind() {
            backgroundImage.visibility = View.GONE
            imgPrev.visibility = View.GONE
            btnAddImage.visibility = View.VISIBLE
            btnDelete.visibility = View.GONE
            btnAddImage.setOnClickListener { listener!!.onAddImage() }
        }

        fun onItemSelected(selectedPosition: Int) {
            backgroundImage.visibility =
                if (selectedPosition == itemPosition) View.VISIBLE else View.GONE
        }

    }

}

