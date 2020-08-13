package com.qiscus.qiscusmultichannel.ui.chat.viewholder

import android.annotation.SuppressLint
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.util.PatternsCompat
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.ui.view.QiscusProgressView
import com.qiscus.qiscusmultichannel.ui.webView.WebViewHelper
import com.qiscus.qiscusmultichannel.util.Const
import com.qiscus.sdk.chat.core.data.model.QMessage
import org.json.JSONObject
import java.util.regex.Matcher

/**
 * Created on : 2019-09-17
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class FileVH(itemView: View) : BaseViewHolder(itemView), QMessage.ProgressListener,
    QMessage.DownloadingListener {

    private val tvTitle: TextView? = itemView.findViewById(R.id.tvTitle)
    private val message: TextView? = itemView.findViewById(R.id.message)
    private val ivDownloadIcon: ImageView? = itemView.findViewById(R.id.ivDownloadIcon)
    private val progressView: QiscusProgressView? =
        itemView.findViewById<View>(R.id.qcpProgressView) as QiscusProgressView?
    lateinit var qiscusComment: QMessage

    @SuppressLint("SetTextI18n")
    override fun bind(comment: QMessage) {
        super.bind(comment)

        this.qiscusComment = comment
        comment.setProgressListener(this)
        comment.setDownloadingListener(this)
        setUpDownloadIcon(comment)
        setUpLinks()
        try {
            val content = comment.payload
            val title = content.getString("file_name")
            val url = content.getString("url")
            val tipe = url.split(".")
            tvTitle?.text = title.toString()
            message?.text = "${tipe[tipe.size - 1].toUpperCase()} File"
        } catch (ex: Exception) {

        }

        if (Const.qiscusCore()?.getDataStore()?.getLocalPath(comment.id) != null) {
            ivDownloadIcon?.visibility = View.GONE
        }
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
        val me = Const.qiscusCore()?.getQiscusAccount()?.getId()
        qiscusComment.isMyComment(me)
        if (ivDownloadIcon != null) {
            if (qiscusComment.status <= QMessage.STATE_SENDING) {
                if (qiscusComment.isMyComment(me)) {
                    ivDownloadIcon.setImageResource(R.drawable.ic_qiscus_upload_file_mc)
                } else {
                    ivDownloadIcon.setImageResource(R.drawable.ic_qiscus_opponent_upload_file_mc)
                }
            } else {
                if (qiscusComment.isMyComment(me)) {
                    ivDownloadIcon.setImageResource(R.drawable.ic_qiscus_download_file)
                } else {
                    ivDownloadIcon.setImageResource(R.drawable.ic_qiscus_opponent_download_file)
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun setUpLinks() {
        val text = message?.text.toString().toLowerCase()
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
        val text: CharSequence = message?.text.toString()
        val span = ClickSpan(listener)
        if (start == -1) {
            return
        }
        if (text is Spannable) {
            (text as Spannable).setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val s: SpannableString = SpannableString.valueOf(text)
            s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            message?.text = s
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