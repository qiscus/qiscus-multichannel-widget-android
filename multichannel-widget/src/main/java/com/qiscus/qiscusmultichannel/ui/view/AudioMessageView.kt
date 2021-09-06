package com.qiscus.qiscusmultichannel.ui.view

import com.qiscus.qiscusmultichannel.util.AudioHandler

interface AudioMessageView {
    fun onPlayAudio(event: AudioHandler.PlayingAudio)

    fun stopAudio(audioId: Long, isStop: Boolean)
}
