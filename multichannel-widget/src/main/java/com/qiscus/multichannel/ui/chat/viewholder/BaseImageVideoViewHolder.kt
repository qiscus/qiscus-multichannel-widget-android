package com.qiscus.multichannel.ui.chat.viewholder

import android.annotation.SuppressLint
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.util.PatternsCompat
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.multichannel.ui.webView.WebViewHelper
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.multichannel.util.ResourceManager
import com.qiscus.nirmana.Nirmana
import com.qiscus.sdk.chat.core.data.model.QMessage
import org.json.JSONObject
import java.io.File
import java.util.regex.Matcher

open class BaseImageVideoViewHolder(
    itemView: View,
    config: QiscusMultichannelWidgetConfig,
    color: QiscusMultichannelWidgetColor,
    viewType: Int
) : BaseViewHolder(itemView, config, color) {

    val thumbnail: ImageView = itemView.findViewById(R.id.iv_chat_comment)
    val message: TextView = itemView.findViewById(R.id.tv_chat)

    init {
        val backgroundColor: Int
        val colorText: Int

        if (viewType == CommentsAdapter.TYPE_MY_IMAGE || viewType == CommentsAdapter.TYPE_MY_VIDEO) {
            backgroundColor = color.getRightBubbleColor()
            colorText = color.getRightBubbleTextColor()
        } else {
            backgroundColor = color.getLeftBubbleColor()
            colorText = color.getLeftBubbleTextColor()
        }

        message.background = ResourceManager.getTintDrawable(
            ContextCompat.getDrawable(
                itemView.context,
                R.drawable.bg_opponent_caption_image
            ), backgroundColor
        )
        message.setTextColor(colorText)
    }

    override fun bind(comment: QMessage) {
        super.bind(comment)

        if (comment.rawType == "text") {
            val url = comment.attachmentUri.toString()
            message.visibility = View.GONE

            if (url.startsWith("http")) { //We have sent it
                showSentImage(comment, url)
            } else { //Still uploading the image
                showSendingImage(url)
            }
            setUpLinks()

        } else {
            try {
                val content = JSONObject(comment.payload)
                val url = content.getString("url")
                val caption = content.getString("caption")
                val filename = content.getString("file_name")

                if (url.startsWith("http")) { //We have sent it
                    showSentImage(comment, url)
                } else { //Still uploading the image
                    showSendingImage(url)
                }

                if (caption.isEmpty()) {
                    message.visibility = View.GONE
                } else {
                    message.visibility = View.VISIBLE
                    message.text = caption
                }
                setUpLinks()
            } catch (t: Throwable) {

            }
        }

    }

/*    private fun downloadFile(qiscusComment: QMessage, fileName: String, URLImage: String) {
        Const.qiscusCore()?.api
            ?.downloadFile(URLImage, fileName) {
                // here you can get the progress total downloaded
            }
            ?.doOnNext { file ->
                // here we update the local path of file
                QiscusCore.getDataStore()
                    .addOrUpdateLocalPath(qiscusComment.roomId, qiscusComment.id, file.absolutePath)

                QiscusImageUtil.addImageToGallery(file)

            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                itemView.context.showToast("success save image to gallery")
            }, {
                //on error
            })
    }*/

    private fun showSendingImage(url: String) {
        val localPath = File(url)
        showLocalImage(localPath)
    }

    private fun showSentImage(comment: QMessage, url: String) {
        val localPath = MultichannelConst.qiscusCore()?.dataStore?.getLocalPath(comment.id)
        localPath?.let { showLocalImage(it) } ?: Nirmana.getInstance().get()
            .load(url)
            .thumbnail(0.5f)
            .apply(
                RequestOptions()
                    .skipMemoryCache(false)
                    .placeholder(R.drawable.qiscus_image_placeholder)
                    .error(R.drawable.qiscus_image_placeholder)
                    .transform(CenterCrop(), RoundedCorners(ResourceManager.DIMEN_ROUNDED_IMAGE))
            )
            .into(thumbnail)
    }

    private fun showLocalImage(localPath: File) {
        Nirmana.getInstance().get()
            .load(localPath)
            .thumbnail(0.5f)
            .apply(
                RequestOptions()
                    .skipMemoryCache(false)
                    .placeholder(R.drawable.qiscus_image_placeholder)
                    .error(R.drawable.qiscus_image_placeholder)
                    .transform(CenterCrop(), RoundedCorners(ResourceManager.DIMEN_ROUNDED_IMAGE))
            )
            .into(thumbnail)
    }

    /*fun shareImage(fileImage: File) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/jpg"
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileImage))
        } else {
            intent.putExtra(
                Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(
                    itemView.context,
                    getAuthority(),
                    fileImage
                )
            )
        }
    }*/

    @SuppressLint("DefaultLocale", "RestrictedApi")
    private fun setUpLinks() {
        val text = message.text.toString().toLowerCase()
        val matcher: Matcher = PatternsCompat.AUTOLINK_WEB_URL.matcher(text)
        while (matcher.find()) {
            val start: Int = matcher.start()
            if (start > 0 && text[start - 1] == '@') {
                continue
            }
            val end: Int = matcher.end()
            clickify(start, end, object : ClickSpan.OnClickListener {
                override fun onClick() {
                    var url = text.substring(start, end)
                    if (!url.startsWith("http")) {
                        url = "http://$url"
                    }
                    WebViewHelper.launchUrl(itemView.context, Uri.parse(url))
                }
            })
        }
    }

    private fun clickify(start: Int, end: Int, listener: ClickSpan.OnClickListener) {
        val text: CharSequence = message.text.toString()
        val span = ClickSpan(listener)
        if (start == -1) {
            return
        }
        if (text is Spannable) {
            text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val s: SpannableString = SpannableString.valueOf(text)
            s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            message.text = s
        }
    }

    private class ClickSpan(private val listener: OnClickListener?) :
        ClickableSpan() {

        interface OnClickListener {
            fun onClick()
        }

        override fun onClick(widget: View) {
            listener?.onClick()
        }

    }

}