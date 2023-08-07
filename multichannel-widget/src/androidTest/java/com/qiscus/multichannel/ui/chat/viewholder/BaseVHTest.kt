package com.qiscus.multichannel.ui.chat.viewholder

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.qiscus.multichannel.QiscusMultichannelWidgetColor
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import com.qiscus.multichannel.ui.chat.CommentsAdapter
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.multichannel.util.ResourceManager
import com.qiscus.nirmana.Nirmana
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.data.model.QUser
import com.qiscus.sdk.chat.core.util.QiscusConst
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.*

@ExtendWith(InstrumentationBaseTest::class)
internal open class BaseVHTest<T: BaseViewHolder> : InstrumentationBaseTest() {

    private var viewType: Int = 0
    protected lateinit var config: QiscusMultichannelWidgetConfig
    protected lateinit var color: QiscusMultichannelWidgetColor
    protected lateinit var parentView: LinearLayout
    protected lateinit var view: View
    private lateinit var vhForTest: ViewHolderForTest<T>
    protected var withListener = true
    protected val listener = object : CommentsAdapter.ItemViewListener {
        override fun onSendComment(comment: QMessage) {
            // ignored
        }

        override fun onItemClick(view: View, position: Int) {
            // ignored
        }

        override fun onItemLongClick(view: View, position: Int) {
            // ignored
        }

        override fun onItemReplyClick(view: View, comment: QMessage) {
            // ignored
        }

        override fun stopAnotherAudio(comment: QMessage) {
            // ignored
        }
    }

    fun setViewHolderForTest(vhForTest: ViewHolderForTest<T>) {
        this.vhForTest = vhForTest
    }

    override fun setUpComponent() {
        super.setUpComponent()
        setComponent()
    }

    @BeforeEach
    open fun before() {
        setActivity()
        parentView = LinearLayout(activity!!)
        view = LayoutInflater.from(activity).inflate(vhForTest.getLayout(), parentView, false)
    }

    private fun setComponent() {
        MockitoAnnotations.openMocks(this)
        val core: QiscusCore = mock()

        whenever(core.apps).thenReturn(application!!)
        MultichannelConst.setQiscusCore(core)

        config = mock()
        config.prepare(context!!)
        config.setAvatar(QiscusMultichannelWidgetConfig.Avatar.DISABLE)
        color = QiscusMultichannelWidgetColor()
        ResourceManager.setUp(context!!, color)

        Nirmana.init(application!!)
        QiscusConst.setApps(application!!)
    }


    fun setViewType(viewType: Int){
        this.viewType = viewType
    }

    fun reloadViewHolder(viewType: Int): T {
        setViewType(viewType)
        return vhForTest.creteViewHolder(view, config, color, viewType)
    }

    fun getViewHolder(): T = vhForTest.creteViewHolder(view, config, color, viewType)

    fun getMessage(textMessage: String = "text", type: String = "text") = QMessage().apply {
        id = 100
        chatRoomId = 100
        timestamp = Date()
        isSelected = false
        status = QMessage.STATE_DELIVERED
        sender = QUser().apply {
            id = "user@mail.com"
            name = "userName"
            avatarUrl = "avatar_url.com"
        }
        text = textMessage
        rawType = type
        payload = "{ }"
    }

    interface ViewHolderForTest<T: BaseViewHolder> {

        fun getLayout(): Int

        fun creteViewHolder(
            view: View,
            config: QiscusMultichannelWidgetConfig,
            color: QiscusMultichannelWidgetColor,
            viewType: Int
        ) : T

    }
}