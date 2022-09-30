package com.qiscus.multichannel.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on : 14/11/20
 * Author     : mmnuradityo
 * GitHub     : https://github.com/mmnuradityo
 */
public class AudioHandler implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnSeekCompleteListener {

    public final static int STATE_STOP = 0;
    public final static int STATE_PAUSE = 1;
    public final static int STATE_PLAY = 2;
    private final MediaObserver observer;
    private final AudioManager audioManager;
    private final Context context;
    private final BroadcastReceiver broadcastReceiver;
    private MediaPlayer mediaPlayer;
    private int playerState = STATE_STOP;
    private AudioFocusRequest focusRequest;
    private long audioId = 0;
    private boolean isListened = true;
    private int resumePosition = 0;

    public AudioHandler(Context context) {
        this.context = context;
        this.observer = new MediaObserver();
        this.mediaPlayer = new MediaPlayer();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
        }
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.mediaPlayer.setOnCompletionListener(this);
        this.mediaPlayer.setOnPreparedListener(this);
        this.mediaPlayer.setOnSeekCompleteListener(this);
        this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                playerState = STATE_PAUSE;
                pauseMedia();
                onPlayingAudio(mediaPlayer.getCurrentPosition());
            }
        };
//      context.registerReceiver(broadcastReceiver, new IntentFilter(MultichanneAudioNotification.ACTION_STOP_FROM_NOTIF));
        audioRequestListener();
    }

    public void initMediaPlayer(long audioId, String mediaPath, int resumePosition) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        } else if (mediaPlayer.isPlaying()) {
            pauseMedia();
            mediaPlayer.stop();
        }

        this.audioId = audioId;
        this.resumePosition = resumePosition;
        this.playerState = STATE_PLAY;

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mediaPath);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepare();
        } catch (IOException e) {

        }
    }

    public int load(long audioId, String mediaPath, int playerState, int resumePosition) {
        switch (playerState) {
            case STATE_STOP:
            case STATE_PAUSE:
                initMediaPlayer(audioId, mediaPath, resumePosition);
                break;
            case STATE_PLAY:
                pauseMedia();
                this.playerState = STATE_PAUSE;
                this.resumePosition = mediaPlayer.getCurrentPosition();
                onPlayingAudio(mediaPlayer.getCurrentPosition());
                break;
        }
        return this.playerState;
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
        }
        audioRequestFocus();
    }

    public void seekToPosition(long audioId, int position) {
        if (this.audioId == audioId && mediaPlayer.isPlaying()) {
            observer.stop();
            playerState = STATE_PLAY;
            mediaPlayer.seekTo(position);
        }
    }

    public void stopMedia() {
        if (mediaPlayer != null) {
            observer.stop();
            playerState = STATE_STOP;
            resumePosition = 0;
            mediaPlayer.stop();
            onPlayingAudio(0);
        }
//      MultichanneAudioNotification.stopNotif(context);
        abandonAudioFocus();
    }

    public void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            observer.stop();
            mediaPlayer.pause();
        }
//      MultichanneAudioNotification.stopNotif(context);
        abandonAudioFocus();
    }

    public void destroyMedia() {
        if (mediaPlayer == null) return;

        try {
            if (mediaPlayer.isPlaying()) {
                observer.stop();
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        } catch (IllegalStateException e) {

        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (audioRequestFocus()) {
            playMedia();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        observer.start();
        new Thread(observer).start();
    }

    public void onPlayingAudio(@NonNull int currentPosition) {
        EventBus.getDefault().post(new PlayingAudio(audioId, currentPosition, playerState));
    }

    public void unlisten() {
        this.isListened = false;
    }

    public boolean audioRequestFocus() {
        final int result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            result = audioManager.requestAudioFocus(focusRequest);
        } else {
            result = audioManager.requestAudioFocus(null,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d("AudioFocus", "Audio focus received");
            return true;
        } else {
            Log.d("AudioFocus", "Audio focus NOT received");
            return false;
        }

    }

    private void audioRequestListener() {
        AudioManager.OnAudioFocusChangeListener afChangeListener = focusChange -> {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                stopMedia();
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(afChangeListener)
                    .build();
        }
    }

    public void abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(focusRequest);
        }
        audioManager.abandonAudioFocus(null);
    }

    public void detach() {
        try {
//         MultichanneAudioNotification.stopService(context);
            context.unregisterReceiver(broadcastReceiver);
            abandonAudioFocus();
        } catch (Exception e) {

        }
    }

    public static class PlayingAudio {

        private final long audioId;
        private final int currentPosition;
        private final int playerState;

        private PlayingAudio(long audioId, int currentPosition, int playerState) {
            this.audioId = audioId;
            this.currentPosition = currentPosition;
            this.playerState = playerState;
        }

        public long getAudioId() {
            return audioId;
        }

        public int getCurrentPosition() {
            return currentPosition;
        }

        public int getPlayerState() {
            return playerState;
        }

    }

    private class MediaObserver implements Runnable {

        private final AtomicBoolean stopPlay = new AtomicBoolean(false);

        public void stop() {
            stopPlay.set(true);
        }

        public void start() {
            stopPlay.set(false);
        }

        @Override
        public void run() {
            while (!stopPlay.get()) {
                try {
                    if (isListened) onPlayingAudio(mediaPlayer.getCurrentPosition());
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    //ignored
                }
            }
        }

    }

}
