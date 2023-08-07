package com.qiscus.multichannel.ui.chat.viewholder

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.media.MediaMetadataRetriever
import android.text.format.DateUtils
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.multichannel.ui.view.AudioMessageView
import com.qiscus.multichannel.ui.view.QiscusProgressView
import com.qiscus.multichannel.util.AudioHandler
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.multichannel.util.ResourceManager
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil
import io.reactivex.functions.Action
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class AudioVH(
    itemView: View,
    config: QiscusMultichannelWidgetConfig,
    color: QiscusMultichannelWidgetColor,
    private val viewType: Int,
    private var listener: CommentsAdapter.ItemViewListener?,
    private val audioHandler: AudioHandler
) : BaseViewHolder(itemView, config, color), AudioMessageView, QMessage.ProgressListener,
    QMessage.DownloadingListener {

    private val retriever: MediaMetadataRetriever = MediaMetadataRetriever()
    private val playButton: AppCompatImageView = itemView.findViewById(R.id.iv_play)
    private val seekBar: AppCompatSeekBar = itemView.findViewById(R.id.seekbar)
    private val durationView: AppCompatTextView = itemView.findViewById(R.id.tv_duration)
    private val progressView: QiscusProgressView =
        itemView.findViewById<View>(R.id.progress) as QiscusProgressView

    private var qiscusComment: QMessage? = null
    private var isLocal = false
    private var audioPlayingId: Long = 0
    private var currentPosition = 0
    private var playerState = AudioHandler.STATE_STOP
    private var duration = 0
    private var isSeekBarTouch = false
    private var subscriptionDuration: Subscription? = null

    init {
        val backgroundColor: Int
        val colorIcon: Int
        val colorText: Int

        if (viewType == CommentsAdapter.TYPE_MY_AUDIO) {
            backgroundColor = color.getRightBubbleColor()
            colorText = color.getRightBubbleTextColor()
            colorIcon = color.getRightBubbleTextColor()
        } else {
            backgroundColor = color.getLeftBubbleColor()
            colorText = color.getLeftBubbleTextColor()
            colorIcon = color.getNavigationColor()
        }

        itemView.findViewById<View>(R.id.containerBackground).background =
            ResourceManager.getTintDrawable(
                ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.qiscus_rounded_chat_bg_mc
                ), backgroundColor
            )
        playButton.setColorFilter(colorIcon)
        durationView.setTextColor(colorText)

        seekBar.apply {
            if (progressDrawable == null) return@apply

            val layerDrawable: LayerDrawable = progressDrawable as LayerDrawable
            val progress = layerDrawable.findDrawableByLayerId(android.R.id.progress) as Drawable
            val secondary =
                layerDrawable.findDrawableByLayerId(android.R.id.secondaryProgress) as Drawable
            val background =
                layerDrawable.findDrawableByLayerId(android.R.id.background) as Drawable

            progress.colorFilter = PorterDuffColorFilter(colorIcon, PorterDuff.Mode.SRC_ATOP)
            secondary.colorFilter = PorterDuffColorFilter(colorIcon, PorterDuff.Mode.SRC_ATOP)
            background.colorFilter = PorterDuffColorFilter(colorText, PorterDuff.Mode.SRC_ATOP)

            layerDrawable.setDrawableByLayerId(android.R.id.progress, progress)
            layerDrawable.setDrawableByLayerId(android.R.id.secondaryProgress, secondary)
            layerDrawable.setDrawableByLayerId(android.R.id.background, background)
            thumb.colorFilter = PorterDuffColorFilter(colorIcon, PorterDuff.Mode.SRC_ATOP)
        }

        playButton.setOnClickListener {
            qiscusComment?.let {
                if (!it.isDownloading && isLocal) {
                    playAudio(
                        it,
                        MultichannelConst.qiscusCore()!!.dataStore.getLocalPath(it.id).absolutePath
                    )
                } else {
                    itemView.callOnClick()
                }
            }

        }

        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener(qiscusComment))
    }

    private fun onSeekBarChangeListener(qiscusComment: QMessage?) = object : OnSeekBarChangeListener {

        override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) { }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            isSeekBarTouch = true
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            qiscusComment?.let {
                isSeekBarTouch = false
                currentPosition = seekBar.progress
                setTimeRemaining((duration - seekBar.progress).toLong())
                audioHandler.seekToPosition(it.id, seekBar.progress)
            }
        }
    }

    override fun getChatFrom(): Drawable? =
        if (viewType == CommentsAdapter.TYPE_MY_AUDIO) ResourceManager.IC_CHAT_FROM_ME else ResourceManager.IC_CHAT_FROM

    override fun bind(comment: QMessage) {
        super.bind(comment)
        qiscusComment = comment
        comment.setProgressListener(this)
        comment.setDownloadingListener(this)

        if (audioPlayingId == -2L && playerState == AudioHandler.STATE_PLAY) {
            audioHandler.pauseMedia()
            onPauseAudio()
        } else {
            val file = MultichannelConst.qiscusCore()!!.dataStore.getLocalPath(comment.id)
            isLocal = file != null
            getDuration(
                if (isLocal) file.absolutePath.toString() else ""
            ) { setDurationView() }
            initView(comment)
        }
    }

    private fun initView(comment: QMessage) {
        setUpPlayButton()
        onDownloading(comment, comment.isDownloading)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    private fun setDurationView() {
        seekBar.max = duration
        seekBar.progress = currentPosition
        setTimeRemaining(duration.toLong())
    }

    private fun playAudio(comment: QMessage, path: String) {
        if (duration > 0) {
            val state: Int = audioHandler.load(comment.id, path, playerState, currentPosition)
            if (state == AudioHandler.STATE_PLAY) {
//                MultichanneAudioNotification.showNotif(itemView.context, qiscusComment, duration)
                listener?.stopAnotherAudio(comment)
            } else if (state == AudioHandler.STATE_PAUSE) {
                onPauseAudio()
            }
        } else {
            itemView.callOnClick()
        }
    }

    private fun setUpPlayButton() {
        playButton.setImageResource(
            if (!isLocal) R.drawable.ic_qiscus_download_file
            else if (playerState == AudioHandler.STATE_PLAY) R.drawable.ic_qiscus_pause_audio
            else R.drawable.ic_qiscus_play_audio
        )
    }

    private fun setTimeRemaining(duration: Long) {
        durationView.text = DateUtils.formatElapsedTime(duration / 1000)
    }

    private fun onPlayingAudio(currentPosition: Int) {
        this.currentPosition = currentPosition
        setTimeRemaining((duration - currentPosition).toLong())
        setUpPlayButton()
        if (!isSeekBarTouch) seekBar.progress = currentPosition
    }

    fun onPauseAudio() {
        isSeekBarTouch = false
        playButton.setImageResource(R.drawable.ic_qiscus_play_audio)
        playerState = AudioHandler.STATE_PAUSE
    }

    fun destroyAudio() {
        if (qiscusComment != null && audioPlayingId == -1L) {
            isSeekBarTouch = false
            audioHandler.unlisten()
            audioHandler.destroyMedia()
            audioPlayingId = 0
            qiscusComment = null
        }

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    @Subscribe
    override fun onPlayAudio(event: AudioHandler.PlayingAudio) {
        audioPlayingId = event.audioId
        if (qiscusComment?.id == event.audioId) {
            playerState = event.playerState
            QiscusAndroidUtil.runOnUIThread { onPlayingAudio(event.currentPosition) }
        }
    }

    override fun stopAudio(audioId: Long, isStop: Boolean) {
        audioPlayingId = audioId
        isSeekBarTouch = false
        if (isStop) {
            QiscusAndroidUtil.runOnUIThread {
                currentPosition = 0
                playerState = AudioHandler.STATE_STOP
                setUpPlayButton()
                setDurationView()
            }
        }
    }

    private fun getDuration(path: String, onDataReady: Action) {
        if (path.isEmpty()) {
            duration = 0
            onDataReady.run()
            return
        }
        subscriptionDuration = Observable.just(path)
            .map {
                try {
                    retriever.setDataSource(path)
                    return@map retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
                        .toInt()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                duration
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ duration: Int ->
                this.duration = duration
                onDataReady.run()
            }) { obj: Throwable -> obj.printStackTrace() }
    }

    fun onProgress(total: Long) {
        QiscusAndroidUtil.runOnUIThread { progressView.setProgress(total.toInt()) }
    }

    override fun onProgress(qiscusComment: QMessage, percentage: Int) {
        if (qiscusComment == this.qiscusComment) {
            onProgress(percentage.toLong())
        }
    }

    override fun onDownloading(comment: QMessage, isDownloading: Boolean) {
        if (this.qiscusComment == comment) {
            progressView.setProgress(comment.progress)
            if (isDownloading) {
                progressView.setVisibility(View.VISIBLE)
                playButton.visibility = View.INVISIBLE

            } else if (comment.progress == 100) {
                progressView.setVisibility(View.GONE)
                playButton.visibility = View.VISIBLE
                playButton.setImageResource(R.drawable.ic_qiscus_play_audio)

                val file = MultichannelConst.qiscusCore()!!.dataStore.getLocalPath(comment.id)
                isLocal = file != null
                getDuration(
                    if (isLocal) file.absolutePath.toString() else "",
                ) {
                    setDurationView()
                }
            }

        }
    }


}
