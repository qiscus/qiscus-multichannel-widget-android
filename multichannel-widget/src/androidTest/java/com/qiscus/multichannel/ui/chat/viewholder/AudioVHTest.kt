package com.qiscus.multichannel.ui.chat.viewholder

import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.multichannel.util.AudioHandler
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.sdk.chat.core.data.local.QiscusDataStore
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File

@ExtendWith(BaseVHTest::class)
internal class AudioVHTest : BaseVHTest<AudioVH>(), BaseVHTest.ViewHolderForTest<AudioVH> {

    private lateinit var dataStore: QiscusDataStore
    private lateinit var audioHandler: AudioHandler
    private val qMessage = getMessage(
        textMessage = "[file] https://www.link.com/file_audio.mp3 [/file]",
        type = "file_attachment"
    ).apply {
        uniqueId = "unique_01"
    }

    @BeforeAll
    fun setUp() {
        setViewType(CommentsAdapter.TYPE_MY_AUDIO)
        setViewHolderForTest(this)
        setUpComponent()

        audioHandler = AudioHandler(application!!)
        dataStore = mock()

        whenever(MultichannelConst.qiscusCore()!!.dataStore).thenReturn(dataStore)
        whenever(dataStore.getLocalPath(anyLong())).thenReturn(File("path"))
        whenever(MultichannelConst.qiscusCore()!!.appsHandler).thenReturn(Handler(application!!.mainLooper))

        QiscusAndroidUtil(MultichannelConst.qiscusCore())
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
    }

    @Test
    fun attachDetachTest() {
        getViewHolder().run {
            destroyAudio()
            val init = extractMethode(this, "initView")
            init.call(this, qMessage)
            init.call(this, qMessage)
            destroyAudio()
        }
    }

    @Test
    fun getChatFromTest() {
        setViewType(CommentsAdapter.TYPE_OPPONENT_AUDIO)
        getViewHolder().getChatFrom()
    }

    @Test
    fun bindTest() {
        setViewType(CommentsAdapter.TYPE_MY_AUDIO)
        getViewHolder().run {
            bind(qMessage)
            getChatFrom()

            val playButton = extractField<ImageView>(this, "playButton")
            playButton?.performClick()
        }
    }

    @Test
    fun bindAudioPlayTest() {
        setViewType(CommentsAdapter.TYPE_MY_AUDIO)
        getViewHolder().run {
            val audioPlayingId = extractFieldOnly(this, "audioPlayingId")
            audioPlayingId?.set(this, -2L)
            val playerState = extractFieldOnly(this, "playerState")
            playerState?.set(this, AudioHandler.STATE_PLAY)

            bind(qMessage)
        }
    }

    @Test
    fun bindAudioPlayStateNotPlayTest() {
        setViewType(CommentsAdapter.TYPE_MY_AUDIO)
        getViewHolder().run {
            val audioPlayingId = extractFieldOnly(this, "audioPlayingId")
            audioPlayingId?.set(this, -2L)
            val playerState = extractFieldOnly(this, "playerState")
            playerState?.set(this, AudioHandler.STATE_STOP)

            whenever(
                MultichannelConst.qiscusCore()!!.dataStore.getLocalPath(anyLong())
            ).thenReturn(null)

            bind(qMessage)

            whenever(
                MultichannelConst.qiscusCore()!!.dataStore.getLocalPath(anyLong())
            ).thenReturn(File("path"))
        }
    }

    @Test
    fun bindAudioPlayIdNotMinus2Test() {
        setViewType(CommentsAdapter.TYPE_MY_AUDIO)
        getViewHolder().run {
            val audioPlayingId = extractFieldOnly(this, "audioPlayingId")
            audioPlayingId?.set(this, 2L)
            val playerState = extractFieldOnly(this, "playerState")
            playerState?.set(this, AudioHandler.STATE_PLAY)

            bind(qMessage)
        }
    }

    @Test
    fun onSeekBarChangeListenerTest() {
        getViewHolder().run {
            val onPlayingAudio = extractMethode(this, "onPlayingAudio")
            onPlayingAudio.call(this, 0)

            val onSeekBarChangeListener = extractMethode(this, "onSeekBarChangeListener")
            val listener =
                onSeekBarChangeListener.call(this, qMessage) as SeekBar.OnSeekBarChangeListener

            val seekBar = SeekBar(context!!).apply {
                progress = 0
            }
            listener.onProgressChanged(seekBar, 0, true)
            listener.onStartTrackingTouch(seekBar)
            onPlayingAudio.call(this, 0)

            listener.onStopTrackingTouch(seekBar)
        }
    }

    @Test
    fun onSeekBarChangeListenerNullTest() {
        getViewHolder().run {
            val onSeekBarChangeListener = extractMethode(this, "onSeekBarChangeListener")
            val listener =
                onSeekBarChangeListener.call(this, null) as SeekBar.OnSeekBarChangeListener

            val seekBar = SeekBar(context!!)
            listener.onStopTrackingTouch(seekBar)
        }
    }

    @Test
    fun onPlayAudioTest() {
        getViewHolder().run {
            val onPlayingAudio = extractMethode(this, "onPlayingAudio")
            onPlayingAudio.call(this, 100)
            onPlayingAudio.call(this, 0)
        }
    }

    @Test
    fun onPauseAudioTest() {
        getViewHolder().onPauseAudio()
    }

    @Test
    fun destroyAudioTest() {
        getViewHolder().run {
            val qiscusComment = extractFieldOnly(this, "qiscusComment")
            qiscusComment?.set(this, qMessage)
            val audioPlayingId = extractFieldOnly(this, "audioPlayingId")
            audioPlayingId?.set(this, -1)

            destroyAudio()
        }
    }

    @Test
    fun destroyAudioPlayIdNotMinusTest() {
        getViewHolder().run {
            val qiscusComment = extractFieldOnly(this, "qiscusComment")
            qiscusComment?.set(this, qMessage)

            destroyAudio()
        }
    }

    @Test
    fun playButtonTest() {
        getViewHolder().run {
            val isLocal = extractFieldOnly(this, "isLocal")
            isLocal?.set(this, false)

            val qiscusComment = extractFieldOnly(this, "qiscusComment")
            qiscusComment?.set(this, QMessage().apply {
                id = qMessage.id
                uniqueId = qMessage.uniqueId
                isDownloading = false
            })

            val playButton = extractField<ImageView>(this, "playButton")
            playButton?.performClick()

            destroyAudio()
        }
    }

    @Test
    fun playButtonDownloadTest() {
        getViewHolder().run {
            val isLocal = extractFieldOnly(this, "isLocal")
            isLocal?.set(this, false)

            val qiscusComment = extractFieldOnly(this, "qiscusComment")
            qiscusComment?.set(this, QMessage().apply {
                id = qMessage.id
                uniqueId = qMessage.uniqueId
                isDownloading = true
            })

            val playButton = extractField<ImageView>(this, "playButton")
            playButton?.performClick()

            destroyAudio()
        }
    }

    @Test
    fun playButtonNullTest() {
        getViewHolder().run {
            val playButton = extractField<ImageView>(this, "playButton")
            playButton?.performClick()
        }
    }

    @Test
    fun onPlayAudioEventTest() {
        val event = AudioHandler.PlayingAudio(qMessage.id, 0, AudioHandler.STATE_PLAY)
        getViewHolder().run {
            onPlayAudio(event)

            val qiscusComment = extractFieldOnly(this, "qiscusComment")
            qiscusComment?.set(this, qMessage)
            onPlayAudio(event)
        }
    }

    @Test
    fun onPlayAudioEventNotSameTest() {
        val event = AudioHandler.PlayingAudio(1, 0, AudioHandler.STATE_PLAY)
        getViewHolder().run {
            onPlayAudio(event)

            val qiscusComment = extractFieldOnly(this, "qiscusComment")
            qiscusComment?.set(this, qMessage)
        }
    }

    /*@Test
    fun stopAudio() {
    }*/

    @Test
    fun onProgressTest() {
        getViewHolder().onProgress(100)
    }

    @Test
    fun onProgressNotNullTest() {
        getViewHolder().run {
            val qiscusComment = extractFieldOnly(this, "qiscusComment")
            qiscusComment?.set(this, qMessage)

            onProgress(100)
        }
    }

    @Test
    fun testOnProgressTest() {
        getViewHolder().run {
            val qiscusComment = extractFieldOnly(this, "qiscusComment")
            qiscusComment?.set(this, qMessage)

            onProgress(qMessage, 100)

            onProgress(
                QMessage().apply {
                    id = 10020283
                    uniqueId = "unique_123"
                }, 100
            )
        }
    }

    @Test
    fun setUpPlayButtonTest() {
        getViewHolder().run {
            val isLocal = extractFieldOnly(this, "isLocal")
            isLocal?.set(this, true)

            val playerState = extractFieldOnly(this, "playerState")
            playerState?.set(this, AudioHandler.STATE_PLAY)

            val setUpPlayButton = extractMethode(this, "setUpPlayButton")
            setUpPlayButton.call(this)
        }
    }

    @Test
    fun playAudioTest() {
        withListener = true
        getViewHolder().run {
            val duration = extractFieldOnly(this, "duration")
            duration?.set(this, 50)
            val playerState = extractFieldOnly(this, "playerState")
            playerState?.set(this, AudioHandler.STATE_STOP)

            val playAudio = extractMethode(this, "playAudio")
            playAudio.call(this, qMessage, "")
        }
    }

    @Test
    fun playAudioNullListenerTest() {
        withListener = false
        getViewHolder().run {
            val duration = extractFieldOnly(this, "duration")
            duration?.set(this, 50)
            val playerState = extractFieldOnly(this, "playerState")
            playerState?.set(this, AudioHandler.STATE_STOP)

            val playAudio = extractMethode(this, "playAudio")
            playAudio.call(this, qMessage, "")
        }
    }

    @Test
    fun playAudioPauseTest() {
        getViewHolder().run {
            val duration = extractFieldOnly(this, "duration")
            duration?.set(this, 50)
            val playerState = extractFieldOnly(this, "playerState")
            playerState?.set(this, AudioHandler.STATE_PLAY)

            val playAudio = extractMethode(this, "playAudio")
            playAudio.call(this, qMessage, "")
        }
    }

    @Test
    fun onDownloading() {
        getViewHolder().run {
            val qiscusComment = extractFieldOnly(this, "qiscusComment")
            qiscusComment?.set(this, qMessage.apply {
                progress = 100
            })

            onDownloading(qMessage, false)
            whenever(MultichannelConst.qiscusCore()!!.dataStore.getLocalPath(anyLong())).thenReturn(
                null
            )
            onDownloading(qMessage, false)
            onDownloading(qMessage, true)
        }
        whenever(MultichannelConst.qiscusCore()!!.dataStore.getLocalPath(anyLong())).thenReturn(
            File(
                "path"
            )
        )
    }

    override fun getLayout(): Int = R.layout.item_opponent_audio_mc

    override fun creteViewHolder(
        view: View,
        config: QiscusMultichannelWidgetConfig,
        color: QiscusMultichannelWidgetColor,
        viewType: Int
    ) = AudioVH(view, config, color, viewType, if (withListener) listener else null, audioHandler)
}