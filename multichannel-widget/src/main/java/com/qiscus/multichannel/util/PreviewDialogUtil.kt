package com.qiscus.multichannel.util

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.qiscus.multichannel.R
import com.qiscus.nirmana.Nirmana
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.util.QiscusDateUtil
import org.json.JSONObject


/**
 * Created by huseinmuhdhor on 15/4/2021
 */

class PreviewDialogUtil {

    private var mDialog: View? = null

    @SuppressLint("InflateParams")
    fun dialogViewImage(
        context: Context,
        qMessage: QMessage
    ) {
        mDialog = LayoutInflater.from(context).inflate(
            R.layout.image_dialog_view_mc, null, false
        )

        var exoPlayer: ExoPlayer? = null
        val imageView = mDialog!!.findViewById<ImageView>(R.id.ivDialogView)
        val videoView = mDialog!!.findViewById<StyledPlayerView>(R.id.exoplayerView)
        val ibDialogView = mDialog!!.findViewById<ImageButton>(R.id.ibDialogView)
        val tvSender = mDialog!!.findViewById<TextView>(R.id.tvSender)
        val tvDescription = mDialog!!.findViewById<TextView>(R.id.tvDescription)
        val tvDate = mDialog!!.findViewById<TextView>(R.id.tvDate)

        tvSender.text = qMessage.sender.name
        tvDate.text = QiscusDateUtil.toFullDateFormat(qMessage.timestamp)
        if (qMessage.type != QMessage.Type.TEXT) {
            val content = JSONObject(qMessage.payload)
            tvDescription.text = content.optString("caption")
        } else {
            tvDescription.text = ""
        }

        if (qMessage.type == QMessage.Type.VIDEO) {
            exoPlayer = ExoPlayer.Builder(context).build()

            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    super.onPlaybackStateChanged(state)
                    if (state == Player.STATE_READY) {
                        imageView.visibility = View.GONE
                        videoView.visibility = View.VISIBLE
                    }
                }
            })

            exoPlayer.setMediaSource(createMediaSource(context, qMessage.attachmentUri))
            exoPlayer.prepare()

            videoView.player = exoPlayer
            exoPlayer.playWhenReady = true
        } else if (qMessage.type == QMessage.Type.IMAGE) {
            Nirmana.getInstance().get()
                .load(qMessage.attachmentUri.toString())
                .fitCenter()
                .into(imageView)
            imageView.visibility = View.VISIBLE
            videoView.visibility = View.GONE
        }
        val dialogBuilder = AlertDialog.Builder(context, R.style.CustomeDialogFull)
            .setView(mDialog)
        val dialog = dialogBuilder.show()

        ibDialogView.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            if (qMessage.type == QMessage.Type.VIDEO) {
                exoPlayer!!.let {
                    it.stop()
                    it.release()
                }
            }
        }
    }

    fun createMediaSource(
        context: Context,
        uri: Uri
    ): MediaSource {
        val factory: DataSource.Factory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, context.getString(R.string.app_name))
        )
        return ProgressiveMediaSource.Factory(factory).createMediaSource(MediaItem.fromUri(uri))
    }
}