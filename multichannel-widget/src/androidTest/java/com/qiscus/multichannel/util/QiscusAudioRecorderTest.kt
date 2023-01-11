package com.qiscus.multichannel.util

import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstrumentationBaseTest::class)
internal class QiscusAudioRecorderTest : InstrumentationBaseTest() {

    private var recorder: QiscusAudioRecorder? = null

    @BeforeEach
    fun setUp() {
        setUpComponent()
        recorder = QiscusAudioRecorder()
    }

    @AfterEach
    fun tearDown() {
        tearDownComponent()
        recorder = null
    }

    @Test
    fun runRecordingTest() {
       runOnMainThread {
           recorder?.let {
               it.cancelRecording()
               try {
                   it.startRecording(context!!)
               } catch (e: Exception) {
                   // ignore
               }
               if (it.isRecording()) {
                   it.cancelRecording()
                   try {
                       it.startRecording(context!!)
                   } catch (e: Exception) {
                       // ignore
                   }
                   it.stopRecording()
               }
           }
       }
    }

}