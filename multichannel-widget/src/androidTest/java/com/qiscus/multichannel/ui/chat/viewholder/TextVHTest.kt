package com.qiscus.multichannel.ui.chat.viewholder

import android.view.View
import android.widget.TextView
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.R
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(BaseVHTest::class)
internal class TextVHTest : BaseVHTest<TextVH>(), BaseVHTest.ViewHolderForTest<TextVH> {

    override fun getLayout() = R.layout.item_my_text_comment_mc

    @BeforeAll
    fun setUp() {
        setViewHolderForTest(this)
        setViewType(CommentsAdapter.TYPE_MY_TEXT)
        setUpComponent()
    }

    override fun creteViewHolder(
        view: View,
        config: QiscusMultichannelWidgetConfig,
        color: QiscusMultichannelWidgetColor,
        viewType: Int
    ) = TextVH(view, config, color, viewType)

    @AfterAll
    fun tearDown() {
        tearDownComponent()
    }

    @Test
    fun getChatFromMeTest() {
        reloadViewHolder(CommentsAdapter.TYPE_MY_TEXT)
        getViewHolder().getChatFrom()
    }

    @Test
    fun bindMeLinkWithAttTest() {
        getViewHolder().bind(getMessage("https://www.link.com/link@link"))
    }

    // opponent view type

    @Test
    fun getChatFromOpponentTest() {
        reloadViewHolder(CommentsAdapter.TYPE_OPPONENT_TEXT)
        getViewHolder().getChatFrom()
    }


    @Test
    fun bindTest() {
        getViewHolder().bind(getMessage())
    }

    @Test
    fun bindLinkTest() {
        getViewHolder().bind(getMessage("https://www.link.com"))
        extractField<TextView>(getViewHolder(), "message")!!.performClick()
    }

    /*private fun getListener() = object : TextVH.ClickSpan.OnClickListener {
        override fun onClick() {
            // ignored
        }
    }

    @Test
    fun clickSpanTest() {
        val clickSpan = TextVH.ClickSpan(getListener())
        clickSpan.onClick(View(context))
    }

    @Test
    fun clickifyStartMinusOneTest() {
        val clickify = extractMethode(getViewHolder(), "clickify")
        clickify.call(getViewHolder(), -1, 1)
    }

    @Test
    fun handleClickTest() {
        val text = "not_link"

        val handleClick = extractMethode(getViewHolder(), "handleClick")
        val listener = handleClick.call(
            getViewHolder(), text, 0, text.length - 1
        ) as TextVH.ClickSpan.OnClickListener

        listener.onClick()
    }

    @Test
    fun handleClickLinkTest() {
        val text = "https://www.link.com"

        val handleClick = extractMethode(getViewHolder(), "handleClick")
        val listener = handleClick.call(
            getViewHolder(), text, 0, text.length - 1
        ) as TextVH.ClickSpan.OnClickListener

        listener.onClick()
    }*/

}