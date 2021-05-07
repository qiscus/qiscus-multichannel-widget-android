package com.qiscus.qiscusmultichannel.ui.chat.image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.qiscus.nirmana.Nirmana
import com.qiscus.qiscusmultichannel.MultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.util.WidgetFileUtil
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.data.model.QiscusPhoto
import kotlinx.android.synthetic.main.activity_send_image_confirmation_mc.*
import java.io.File

class SendImageConfirmationActivity : AppCompatActivity() {

    lateinit var qiscusChatRoom: QChatRoom
    lateinit var qiscusPhoto: QiscusPhoto

    companion object {
        val EXTRA_ROOM = "extra_room2"
        val EXTRA_PHOTOS = "extra_photos2"
        val EXTRA_CAPTIONS = "extra_captions2"

        fun generateIntent(
            context: Context,
            qiscusChatRoom: QChatRoom,
            qiscusPhoto: QiscusPhoto
        ): Intent {
            val intent = Intent(context, SendImageConfirmationActivity::class.java)
            intent.putExtra(EXTRA_ROOM, qiscusChatRoom)
            intent.putExtra(EXTRA_PHOTOS, qiscusPhoto)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_image_confirmation_mc)

        val room = intent.getParcelableExtra<QChatRoom>(EXTRA_ROOM)

        if (room == null) {
            finish()
            return
        } else {
            this.qiscusChatRoom = room
        }

        val photo = intent.getParcelableExtra<QiscusPhoto>(EXTRA_PHOTOS)
        if (photo != null) {
            this.qiscusPhoto = photo
            initPhotos()
        } else {
            finish()
            return
        }

        buttonSend.setOnClickListener { confirm() }
    }

    private fun initPhotos() {
        val file = qiscusPhoto.photoFile
        val isVideo = WidgetFileUtil.isVideo(file)!!

        if (isVideo && MultichannelWidgetConfig.getVideoPreviewOnSend()) {
            initVideo(file)
        } else {
            Nirmana.getInstance().get()
                .setDefaultRequestOptions(
                    RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                )
                .load(file)
                .into(ivImage)
            progressBar.visibility = View.GONE
        }
    }

    private fun initVideo(file: File?) {
        val uri = Uri.fromFile(file)
        val player = SimpleExoPlayer.Builder(this).build()

        player.setMediaSource(createMediaResource(uri))
        player.prepare()
        player.playWhenReady = false
        exoplayerView.player = player

        player.addListener(object : Player.EventListener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    exoplayerView.visibility = View.VISIBLE
                    ivImage.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }
            }
        })
    }

    private fun createMediaResource(uri: Uri): ProgressiveMediaSource {
        val factory =
            DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)))

        return ProgressiveMediaSource.Factory(factory).createMediaSource(MediaItem.fromUri(uri))
    }

    private fun confirm() {
        val data = Intent()
        data.putExtra(
            EXTRA_PHOTOS,
            qiscusPhoto
        )
        data.putExtra(EXTRA_CAPTIONS, etCaption.text.toString())
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    override fun onStop() {
        super.onStop()
        exoplayerView.player?.stop()
        exoplayerView.player?.release()
    }
}
