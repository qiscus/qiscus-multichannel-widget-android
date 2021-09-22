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
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.multichannel.ui.view.QiscusProgressView
import com.qiscus.multichannel.ui.webView.WebViewHelper
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.multichannel.util.ResourceManager
import com.qiscus.sdk.chat.core.data.model.QMessage
import org.json.JSONObject
import java.util.regex.Matcher

/**
 * Created on : 2019-09-17
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class FileVH(
    itemView: View,
    config: QiscusMultichannelWidgetConfig,
    color: QiscusMultichannelWidgetColor,
    viewType: Int
) : BaseViewHolder(itemView, config, color), QMessage.ProgressListener,
    QMessage.DownloadingListener {

    private val containerMessage: View? = itemView.findViewById(R.id.background)
    private val ivAttachment: ImageView? = itemView.findViewById(R.id.iv_icon_file)
    private val tvTitle: TextView? = itemView.findViewById(R.id.tv_title_file)
    private val extention: TextView? = itemView.findViewById(R.id.tv_extension_file)
    private val ivDownloadIcon: ImageView? = itemView.findViewById(R.id.btn_download_or_upload)
    private val progressView: QiscusProgressView? =
        itemView.findViewById<View>(R.id.pb_file) as QiscusProgressView?
    lateinit var qiscusComment: QMessage

    init {
        val backgroundColor: Int
        val iconColor: Int

        if (viewType == CommentsAdapter.TYPE_OPPONENT_FILE) {
            backgroundColor = color.getLeftBubbleColor()
            iconColor = color.getNavigationColor()
            tvTitle?.setTextColor(color.getLeftBubbleTextColor())
            extention?.setTextColor(color.getLeftBubbleTextColor())
        } else {
            backgroundColor = color.getRightBubbleColor()
            iconColor = color.getRightBubbleTextColor()
            tvTitle?.setTextColor(color.getRightBubbleTextColor())
            extention?.setTextColor(color.getRightBubbleTextColor())
        }

        containerMessage?.background = ResourceManager.getTintDrawable(
            ContextCompat.getDrawable(
                itemView.context,
                R.drawable.qiscus_rounded_chat_bg_mc
            ), backgroundColor
        )
        ivAttachment?.setImageDrawable(
            ResourceManager.getTintDrawable(
                ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.ic_qiscus_doc
                ), iconColor
            )
        )
        ivDownloadIcon?.setImageDrawable(
            ResourceManager.getTintDrawable(
                ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.ic_qiscus_download_file
                ), iconColor
            )
        )

    }

    @SuppressLint("SetTextI18n")
    override fun bind(comment: QMessage) {
        super.bind(comment)

        this.qiscusComment = comment
        comment.setProgressListener(this)
        comment.setDownloadingListener(this)
        setUpDownloadIcon(comment)
        setUpLinks()
        try {
            val content = JSONObject(comment.payload)
            val title = content.getString("file_name")
            val url = content.getString("url")
            val tipe = url.split(".")
            tvTitle?.text = title.toString()
            extention?.text = "${tipe[tipe.size - 1].toUpperCase()} File"
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        ivDownloadIcon?.visibility =
            if (MultichannelConst.qiscusCore()?.dataStore?.getLocalPath(comment.id) != null) View.GONE else View.VISIBLE

    }

    override fun onProgress(qiscusComment: QMessage?, percentage: Int) {
        ivDownloadIcon?.visibility = View.GONE
        progressView?.setVisibility(View.GONE)

        if (qiscusComment == this.qiscusComment && progressView != null) {
            progressView.setProgress(percentage)
        }
    }

    override fun onDownloading(qiscusComment: QMessage?, downloading: Boolean) {
        ivDownloadIcon?.visibility = View.GONE
        if (qiscusComment == this.qiscusComment && progressView != null) {
            progressView.setVisibility(if (downloading) View.VISIBLE else View.GONE)
        }
    }

    private fun setUpDownloadIcon(qiscusComment: QMessage) {
        ivDownloadIcon?.let {
            it.rotation = if (qiscusComment.status <= QMessage.STATE_SENDING) 180F else 0F
        }
    }

    @SuppressLint("DefaultLocale", "RestrictedApi")
    private fun setUpLinks() {
        val text = extention?.text.toString().toLowerCase()
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
        val text: CharSequence = extention?.text.toString()
        val span = ClickSpan(listener)
        if (start == -1) {
            return
        }
        if (text is Spannable) {
            text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val s: SpannableString = SpannableString.valueOf(text)
            s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            extention?.text = s
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