package com.qiscus.multichannel.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.os.Build
import android.util.Log
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created on : 14/11/20
 * Author     : mmnuradityo
 * GitHub     : https://github.com/mmnuradityo
 */
open class AudioHandler(private val context: Context) : OnPreparedListener, OnCompletionListener,
    OnSeekCompleteListener {

    private val observer: MediaObserver
    private val audioManager: AudioManager
    private val broadcastReceiver: BroadcastReceiver
    private var mediaPlayer: MediaPlayer? = null
    private var playerState: Int = STATE_STOP
    private var focusRequest: AudioFocusRequest? = null
    private var audioId: Long = 0
    private var isListened = true
    private var resumePosition = 0

    fun initMediaPlayer(audioId: Long, mediaPath: String?, resumePosition: Int) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        } else if (mediaPlayer!!.isPlaying) {
            pauseMedia()
            mediaPlayer!!.stop()
        }
        this.audioId = audioId
        this.resumePosition = resumePosition
        playerState = STATE_PLAY
        try {
            mediaPlayer?.let {
                it.reset()
                it.setOnPreparedListener(this)
                it.setDataSource(mediaPath)
                it.prepare()
            }
        } catch (e: IOException) {
            // ignored
        }
    }

    fun load(audioId: Long, mediaPath: String?, playerState: Int, resumePosition: Int): Int {
        when (playerState) {
            STATE_STOP, STATE_PAUSE -> initMediaPlayer(audioId, mediaPath, resumePosition)
            STATE_PLAY -> {
                pauseMedia()
                this.playerState = STATE_PAUSE
                this.resumePosition = mediaPlayer!!.currentPosition
                onPlayingAudio(mediaPlayer!!.currentPosition)
            }
        }
        return this.playerState
    }

    private fun playMedia() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.seekTo(resumePosition)
        }
        audioRequestFocus()
    }

    fun seekToPosition(audioId: Long, position: Int) {
        if (this.audioId == audioId && mediaPlayer!!.isPlaying) {
            observer.stop()
            playerState = STATE_PLAY
            mediaPlayer!!.seekTo(position)
        }
    }

    fun stopMedia() {
        if (mediaPlayer != null) {
            observer.stop()
            playerState = STATE_STOP
            resumePosition = 0
            mediaPlayer!!.stop()
            onPlayingAudio(0)
        }
        //      MultichanneAudioNotification.stopNotif(context);
        abandonAudioFocus()
    }

    fun pauseMedia() {
        if (mediaPlayer!!.isPlaying) {
            observer.stop()
            mediaPlayer!!.pause()
        }
        //      MultichanneAudioNotification.stopNotif(context);
        abandonAudioFocus()
    }

    fun destroyMedia() {
        if (mediaPlayer == null) return
        try {
            if (mediaPlayer!!.isPlaying) {
                observer.stop()
                mediaPlayer!!.stop()
                mediaPlayer!!.release()
            }
        } catch (e: IllegalStateException) {
            // ignored
        }
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        if (audioRequestFocus()) {
            playMedia()
        }
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) {
        stopMedia()
    }

    override fun onSeekComplete(mediaPlayer: MediaPlayer) {
        mediaPlayer.start()
        observer.start()
        Thread(observer).start()
    }

    fun onPlayingAudio(currentPosition: Int) {
        EventBus.getDefault().post(PlayingAudio(audioId, currentPosition, playerState))
    }

    fun unlisten() {
        isListened = false
    }

    fun audioRequestFocus(): Boolean {
        val result: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(focusRequest!!)
        } else {
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }
        return if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d("AudioFocus", "Audio focus received")
            true
        } else {
            Log.d("AudioFocus", "Audio focus NOT received")
            false
        }
    }

    private fun audioRequestListener() {
        val afChangeListener = OnAudioFocusChangeListener { focusChange: Int ->
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                stopMedia()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(afChangeListener)
                .build()
        }
    }

    fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(focusRequest!!)
        }
        audioManager.abandonAudioFocus(null)
    }

    fun detach() {
        try {
//         MultichanneAudioNotification.stopService(context);
            context.unregisterReceiver(broadcastReceiver)
            abandonAudioFocus()
        } catch (e: Exception) {
            // ignored
        }
    }

    class PlayingAudio(val audioId: Long, val currentPosition: Int, val playerState: Int)
    private inner class MediaObserver : Runnable {
        private val stopPlay = AtomicBoolean(false)
        fun stop() {
            stopPlay.set(true)
        }

        fun start() {
            stopPlay.set(false)
        }

        override fun run() {
            while (!stopPlay.get()) {
                try {
                    if (isListened) onPlayingAudio(mediaPlayer!!.currentPosition)
                    Thread.sleep(200)
                } catch (e: InterruptedException) {
                    //ignored
                    Thread.currentThread().interrupt()
                }
            }
        }
    }

    companion object {
        const val STATE_STOP: Int = 0
        const val STATE_PAUSE: Int = 1
        const val STATE_PLAY: Int = 2
    }

    init {
        observer = MediaObserver()
        mediaPlayer = MediaPlayer()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaPlayer!!.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mediaPlayer!!.setOnCompletionListener(this)
        mediaPlayer!!.setOnPreparedListener(this)
        mediaPlayer!!.setOnSeekCompleteListener(this)
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                playerState = STATE_PAUSE
                pauseMedia()
                onPlayingAudio(mediaPlayer!!.currentPosition)
            }
        }
        //      context.registerReceiver(broadcastReceiver, new IntentFilter(MultichanneAudioNotification.ACTION_STOP_FROM_NOTIF));
        audioRequestListener()
    }
}