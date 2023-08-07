package com.qiscus.multichannel.ui.chat.viewholder

import android.text.SpannableString
import android.view.View
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.sdk.chat.core.data.local.QiscusDataStore
import com.qiscus.sdk.chat.core.data.model.QMessage
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File

@ExtendWith(BaseVHTest::class)
internal class FileVHTest : BaseVHTest<FileVH>(), BaseVHTest.ViewHolderForTest<FileVH> {

    private lateinit var dataStore: QiscusDataStore
    private val qMessage = getMessage(
        textMessage = "[file] https://www.file.com/link.pdf [/file]",
        type = "file_attachment"
    ).apply {
        uniqueId = "unique01"
        payload = "{" +
                "\"file_name\" : \"file.pdf\", " +
                "\"url\" : \"https://www.file.com/file.pdf\"" +
                "}"
    }

    @BeforeAll
    fun setUp() {
        setViewHolderForTest(this)
        setUpComponent()

        dataStore = mock()
        whenever(MultichannelConst.qiscusCore()!!.dataStore).thenReturn(dataStore)
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
    }

    @Test
    fun bindTest() {
        setViewType(CommentsAdapter.TYPE_MY_FILE)
        val file = File("file.pdf")
        whenever(dataStore.getLocalPath(qMessage.id)).thenReturn(file)
        getViewHolder().bind(qMessage.apply {
            status = QMessage.STATE_FAILED
        })
    }

    @Test
    fun bindFileNullTest() {
        setViewType(CommentsAdapter.TYPE_OPPONENT_FILE)
        whenever(dataStore.getLocalPath(qMessage.id)).thenReturn(null)
        getViewHolder().bind(qMessage.apply {
            status = QMessage.STATE_SENDING
        })
    }


    @Test
    fun bindFileJSONErrorTest() {
        setViewType(CommentsAdapter.TYPE_OPPONENT_FILE)
        whenever(dataStore.getLocalPath(qMessage.id)).thenReturn(null)
        getViewHolder().bind(getMessage(
            textMessage = "[file] https://www.file.com/link.pdf [/file]",
            type = "file_attachment"
        ).apply {
            uniqueId = "unique01"
            payload = "{" +
                    "\"file_name\" : \"file.pdf\"" +
                    "\"url\" : \"https://www.file.com/filepdf\"" +
                    "}"
            status = QMessage.STATE_SENDING
        })
    }

    @Test
    fun onProgressTest() {
        getViewHolder().run {
            qiscusComment = qMessage
            onProgress(qMessage, 0)
        }
    }
    @Test
    fun onProgressMessageNotSameTest() {
        getViewHolder().run {
            qiscusComment = qMessage
            onProgress(
                QMessage().apply {
                    uniqueId = "unique02"
                    id = 20134
                }, 0
            )
        }
    }

    @Test
    fun setUpDownloadIconTest() {
        getViewHolder().run {
            val setUpDownloadIcon = extractMethode(this, "setUpDownloadIcon")
            setUpDownloadIcon.call(this, QMessage().apply {
                status = QMessage.STATE_READ
            })
        }
    }

    @Test
    fun onDownloadingTest() {
        getViewHolder().run {
            qiscusComment = qMessage
            onDownloading(qiscusComment, true)
        }
    }

    @Test
    fun onDownloadingNotRunningTest() {
        getViewHolder().run {
            qiscusComment = qMessage
            onDownloading(qMessage, false)
        }
    }

    @Test
    fun onDownloadingMessageNotSameTest() {
        getViewHolder().run {
            qiscusComment = qMessage
            onDownloading(
                QMessage().apply {
                    uniqueId = "unique02"
                    id = 20134
                }, false
            )
        }
    }

    @Test
    fun onSpanResultTest() {
        getViewHolder().onSpanResult( SpannableString.valueOf("text"))
    }

    override fun getLayout(): Int = R.layout.item_opponent_file_mc

    override fun creteViewHolder(
        view: View,
        config: QiscusMultichannelWidgetConfig,
        color: QiscusMultichannelWidgetColor,
        viewType: Int
    ) = FileVH(view, config, color, viewType)
}