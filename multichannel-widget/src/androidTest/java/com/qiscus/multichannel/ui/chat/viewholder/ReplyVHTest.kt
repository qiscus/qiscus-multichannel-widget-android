package com.qiscus.multichannel.ui.chat.viewholder

import android.text.SpannableString
import android.view.View
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.sdk.chat.core.data.local.QiscusDataStore
import com.qiscus.sdk.chat.core.data.model.QAccount
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExtendWith(BaseVHTest::class)
internal class ReplyVHTest : BaseVHTest<ReplyVH>(), BaseVHTest.ViewHolderForTest<ReplyVH> {

    private fun getMessageReply(senderName: String, type: String, text: String? = null, caption: String = "") = getMessage(
        textMessage = "text message",
        type = "reply"
    ).apply {
        payload = "{" +
                "\"replied_comment_id\" : 102, " +
                "\"replied_comment_message\" : \"$text\", " +
                "\"replied_comment_sender_username\" : \"$senderName\", " +
                "\"replied_comment_sender_email\" : \"$senderName@mail.com\", " +
                "\"replied_comment_type\" : \"$type\", " +
                "\"replied_comment_payload\" :  { \"caption\" : \"$caption\" } " +
                "}"
    }

    private lateinit var qiscusAccount: QAccount

    @BeforeAll
    fun setUp() {
        setViewType(CommentsAdapter.TYPE_MY_REPLY)
        setViewHolderForTest(this)
        setUpComponent()

        qiscusAccount = mock()

        whenever(MultichannelConst.qiscusCore()!!.qiscusAccount).thenReturn(qiscusAccount)
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
    }

    @Test
    fun getChatFromMeTest() {
        setViewType(CommentsAdapter.TYPE_MY_REPLY)
        getViewHolder().getChatFrom()
    }

    @Test
    fun getChatFromOpponentTest() {
        setViewType(CommentsAdapter.TYPE_OPPONENT_REPLY)
        getViewHolder().getChatFrom()
    }

    @Test
    fun bindTest() {
        whenever(qiscusAccount.id).thenReturn("sender@mail.com")
        val qMessage = getMessageReply("sender", "text", "reply message")
        getViewHolder().bind(qMessage)
    }

    @Test
    fun bindNullTest() {
        whenever(qiscusAccount.id).thenReturn("sender@mail.com")
        val qMessage = getMessageReply("sender", "text")
        getViewHolder().bind(qMessage)
    }

    @Test
    fun bindIdNotSameTest() {
        setViewType(CommentsAdapter.TYPE_MY_REPLY)
        whenever(qiscusAccount.id).thenReturn("notSender@mail.com")
        val qMessage = getMessageReply("sender", "file_attachment", "[file] https://www.file.com/file.pdf [/file]")
        getViewHolder().bind(qMessage)
    }

    @Test
    fun bindIdOpponentTest() {
        setViewType(CommentsAdapter.TYPE_OPPONENT_REPLY)
        whenever(qiscusAccount.id).thenReturn("sender@mail.com")
        val qMessage = getMessageReply("sender", "file_attachment", "[file] https://www.file.com/file.pdf [/file]")
        getViewHolder().bind(qMessage)
    }

    @Test
    fun bindIdImageTest() {
        whenever(qiscusAccount.id).thenReturn("sender@mail.com")
        val qMessage = getMessageReply("sender", "file_attachment", "[file] https://www.file.com/file.png [/file]")
        runOnMainThread {
            getViewHolder().bind(qMessage)
        }
    }

    @Test
    fun bindIdVideoTest() {
        whenever(qiscusAccount.id).thenReturn("sender@mail.com")
        val qMessage = getMessageReply("sender", "file_attachment", "[file] https://www.file.com/file.mp4 [/file]", "caption")
        runOnMainThread {
            getViewHolder().bind(qMessage)
        }
    }

    @Test
    fun onSpanResultTest() {
        getViewHolder().onSpanResult(SpannableString.valueOf("ok"))
    }

    override fun getLayout(): Int = R.layout.item_opponent_reply_mc

    override fun creteViewHolder(
        view: View,
        config: QiscusMultichannelWidgetConfig,
        color: QiscusMultichannelWidgetColor,
        viewType: Int
    ) = ReplyVH(view, config, color, if (withListener) listener else null, viewType)
}