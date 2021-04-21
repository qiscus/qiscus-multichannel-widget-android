package com.qiscus.qiscusmultichannel.util

import android.media.MediaRecorder
import android.os.Environment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created on : 02/03/19
 * Author     : arioki
 * Name       : Yoga Setiawan
 * GitHub     : https://github.com/arioki
 */

class QiscusAudioRecorder {
    private var recorder: MediaRecorder? = null
    private var fileName: String? = null
    private var recording = false

    @Throws(IOException::class)

    fun startRecording() {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(Date())
        val audioFileName = "AUDIO_" + timeStamp + "_"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        storageDir.mkdirs()
        var file = storageDir.absolutePath
        file += File.separator + audioFileName + ".m4a"
        startRecording(file)
    }

    @Throws(IOException::class)
    private fun startRecording(fileName: String) {
        if (!recording) {
            this.fileName = fileName
            recording = true
            recorder = MediaRecorder()
            recorder?.let { recorder ->
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                recorder.setOutputFile(fileName)
                recorder.prepare()
                recorder.start()
            }
        }
    }

    fun stopRecording(): File {
        cancelRecording()
        return File(fileName)
    }

    fun cancelRecording() {
        if (recording) {
            recording = false
            try {
                recorder?.let { recorder ->
                    recorder.stop()
                    recorder.release()
                }
                recorder = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isRecording(): Boolean {
        return recording
    }
}

