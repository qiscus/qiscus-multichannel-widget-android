package com.qiscus.multichannel.util

import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstrumentationBaseTest::class)
internal class AudioHandlerTest : InstrumentationBaseTest() {

    private var audioHandler: AudioHandler? = null
    private val audioId: Long = 1201L

    @BeforeAll
    fun setUp() {
        setUpComponent()
        audioHandler = AudioHandler(context!!).apply {
            initMediaPlayer(
                audioId, "path", 0
            )
        }
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
        audioHandler?.let{
            it.unlisten()
            it.detach()
            it.stopMedia()
            it.destroyMedia()
        }
        audioHandler = null
    }

    @Test
    fun loadTest() {
        audioHandler?.let {
            it.initMediaPlayer(
                audioId, "path", 0
            )
            it.load(
                audioId, "path", AudioHandler.STATE_PLAY, 10
            )
        }
    }

    @Test
    fun seekToPositionTest() {
        audioHandler?.seekToPosition(audioId, 0)
    }

    @Test
    fun pauseTest() {
        audioHandler?.pauseMedia()
    }

}