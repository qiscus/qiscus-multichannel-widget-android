package com.qiscus.multichannel.util

import android.view.View
import android.widget.ImageButton
import com.qiscus.multichannel.R
import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import com.qiscus.nirmana.Nirmana
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.data.model.QUser
import com.qiscus.sdk.chat.core.util.QiscusConst
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(InstrumentationBaseTest::class)
internal class PreviewDialogUtilTest : InstrumentationBaseTest() {

    @BeforeAll
    fun setUp() {
        setUpComponent()
        QiscusConst.setApps(application)
        Nirmana.init(application)
    }

    @BeforeEach
    fun before() {
        setActivity()
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
    }

    @Test
    fun runTextTest() {
        runOnMainThread {
            val previewDialog = PreviewDialogUtil()
            previewDialog.dialogViewImage(
                activity!!, getQMessage("message ok", "text")
            )

            val view = extractField<View>(previewDialog, "mDialog")
            val btn = view?.findViewById<ImageButton>(R.id.ibDialogView)
            btn?.performClick()
        }
    }

    @Test
    fun runImageTest() {
        runOnMainThread {
            val previewDialog = PreviewDialogUtil()
            previewDialog.dialogViewImage(
                activity!!, getQMessage("[file]  https://www.sample.com/image.jpg [/file]", "file_attachment")
            )

            val view = extractField<View>(previewDialog, "mDialog")
            val btn = view?.findViewById<ImageButton>(R.id.ibDialogView)
            btn?.performClick()
        }
    }

    @Test
    fun runVideoTest() {
        runOnMainThread {
            val previewDialog = PreviewDialogUtil()
            previewDialog.dialogViewImage(
                activity!!, getQMessage("[file]  https://www.sample.com/image.mp4 [/file]", "file_attachment")
            )

            val view = extractField<View>(previewDialog, "mDialog")
            val btn = view?.findViewById<ImageButton>(R.id.ibDialogView)
            btn?.performClick()
        }
    }

    private fun getQMessage(message: String, type: String) = QMessage().apply {
        id = 0L
        sender = QUser().apply {
            id = "user@email.com"
            name = "name"
            avatarUrl = "avatar"
            extras = JSONObject()
        }
        timestamp = Date()
        rawType = type
        payload = "{ \"caption\" : \"caption text ok\" }"
        text = message
    }

}