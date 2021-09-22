package com.qiscus.multichannel.ui.view

import com.qiscus.multichannel.util.AudioHandler

interface AudioMessageView {
    fun onPlayAudio(event: AudioHandler.PlayingAudio)

    fun stopAudio(audioId: Long, isStop: Boolean)
}
