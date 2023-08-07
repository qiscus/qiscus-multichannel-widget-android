package com.qiscus.multichannel.util

import android.content.SharedPreferences
import com.qiscus.multichannel.QiscusMultichannelWidgetConfig
import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import com.qiscus.multichannel.basetest.MulchanWidget
import com.qiscus.multichannel.data.local.QiscusChatLocal
import com.qiscus.multichannel.data.model.user.User
import com.qiscus.multichannel.data.model.user.UserProperties
import com.qiscus.sdk.chat.core.data.model.QAccount
import com.qiscus.sdk.chat.core.data.model.QChatRoom
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.data.model.QParticipant
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock


@ExtendWith(InstrumentationBaseTest::class)
class QiscusChatRoomBuilderTest : InstrumentationBaseTest() {

    private var preferences: SharedPreferences? = null
    private var widgetConfig: QiscusMultichannelWidgetConfig? = null
    private var builder: QiscusChatRoomBuilder? = null
    private var widget: MultichanelChatWidget? = null

    private val listener = object : QiscusChatRoomBuilder.InitiateCallback {
        override fun onProgress() {
            // ignored
        }

        override fun onSuccess(
            qChatRoom: QChatRoom,
            qMessage: QMessage?,
            isAutomatic: Boolean
        ) {
            // ignored
        }

        override fun onError(throwable: Throwable) {
            // ignored
        }

    }

    @BeforeAll
    fun setup() {
        setUpComponent()
        MockitoAnnotations.openMocks(this)

        preferences = mock<SharedPreferences>()
        widgetConfig = mock<QiscusMultichannelWidgetConfig>()
        widget = mock<MultichanelChatWidget>()

        widgetConfig?.prepare(application!!)
        `when`(widget?.getConfig()).thenReturn(widgetConfig)
      /*  MulchanWidget().apply {
            generateMulchanWidget(application!!)
            login()
        }

        var appServer = extractField<String>(QiscusMultichannelWidget.instance, "appServer")
        appServer = "https://www.qiscus.com/"
        */

        builder = QiscusChatRoomBuilder(widget!!)
    }

    @BeforeEach
    fun before() {
        setActivity()
        clearInvocations(preferences)
        clearInvocations(widgetConfig)
        clearInvocations(widget)
    }

    @AfterAll
    fun teardown() {
        tearDownComponent()
        preferences = null
        widgetConfig = null
        widget = null
        builder = null
    }

    @Test
    fun buildTest() {
        builder?.run {
            setChannelId(0)
            setRoomTitle("title")
            setRoomSubtitle(QiscusMultichannelWidgetConfig.RoomSubtitle.ENABLE)
            setRoomSubtitle(QiscusMultichannelWidgetConfig.RoomSubtitle.EDITABLE, "subtitle")
            setAvatar(QiscusMultichannelWidgetConfig.Avatar.ENABLE)
            setShowSystemMessage(true)
            setSessional(true)
        }
    }

    @Test
    fun autoSendTest() {
        val message = QMessage.generateCustomMessage(
            100L, "text", "text", JSONObject()
        )

        builder?.run {
            automaticSendMessage(message.text)
            automaticSendMessage(message)
            manualSendMessage(message.text)
            startChat(context!!)
        }
    }

    private fun getUser() = User().apply {
        name = "name"
        userId = "userId"
        avatar = "avatar"
    }

    @Test
    fun startChatWithListenerTest() {
        builder?.also {
            it.showLoadingWhenInitiate(true)
            it.startChat(activity!!, listener)
        }

        val onSuccess = argumentCaptor<(User, MutableList<UserProperties>) -> Unit>()

        verify(widget)?.userCheck(onSuccess.capture())
        onSuccess.lastValue.invoke(getUser(), ArrayList())
    }

    @Test
    fun startChatWithListenerNoLoadTest() {
        builder?.also {
            it.showLoadingWhenInitiate(false)
            it.startChat(context!!, listener)
        }

        val onSuccess = argumentCaptor<(User, MutableList<UserProperties>) -> Unit>()
        val onAccount = argumentCaptor<(QAccount) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        val user = getUser()
        verify(widget)?.userCheck(onSuccess.capture())
        onSuccess.lastValue.invoke(user, ArrayList())

        verify(widget)?.loginMultiChannel(
            ArgumentMatchers.eq(user.name),
            ArgumentMatchers.eq(user.userId),
            ArgumentMatchers.eq(user.avatar),
            ArgumentMatchers.eq("{}"),
            ArgumentMatchers.eq(ArrayList()),
            onAccount.capture(),
            onError.capture()
        )
        onAccount.lastValue.invoke(QAccount())
    }

    @Test
    fun startChatWithListenerNoLoadErrorTest() {
        builder?.also {
            it.showLoadingWhenInitiate(false)
            it.startChat(context!!, listener)
        }

        val onSuccess = argumentCaptor<(User, MutableList<UserProperties>) -> Unit>()
        val onAccount = argumentCaptor<(QAccount) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        val user = getUser()
        verify(widget)?.userCheck(onSuccess.capture())
        onSuccess.lastValue.invoke(user, ArrayList())

        verify(widget)?.loginMultiChannel(
            ArgumentMatchers.eq(user.name),
            ArgumentMatchers.eq(user.userId),
            ArgumentMatchers.eq(user.avatar),
            ArgumentMatchers.eq("{}"),
            ArgumentMatchers.eq(ArrayList()),
            onAccount.capture(),
            onError.capture()
        )
        onError.lastValue.invoke(Throwable("msg"))
    }

    @Test
    fun startChatNoListenerNoLoadErrorTest() {
        builder?.also {
            it.showLoadingWhenInitiate(false)
            it.startChat(context!!, null)
        }

        val onSuccess = argumentCaptor<(User, MutableList<UserProperties>) -> Unit>()
        val onAccount = argumentCaptor<(QAccount) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        val user = getUser()
        verify(widget)?.userCheck(onSuccess.capture())
        onSuccess.lastValue.invoke(user, ArrayList())

        verify(widget)?.loginMultiChannel(
            ArgumentMatchers.eq(user.name),
            ArgumentMatchers.eq(user.userId),
            ArgumentMatchers.eq(user.avatar),
            ArgumentMatchers.eq("{}"),
            ArgumentMatchers.eq(ArrayList()),
            onAccount.capture(),
            onError.capture()
        )
        onError.lastValue.invoke(Throwable("msg"))
    }

    @Test
    fun loadChatRoomTest() {
        val loadChatroom = extractMethode(builder!!,  "loadChatRoom")
        loadChatroom.call(builder!!, context!!, true, listener)

        val roomId = QiscusChatLocal.getRoomId()
        val onSuccess = argumentCaptor<(QChatRoom) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        verify(widget)?.openChatRoomById(ArgumentMatchers.eq(roomId), onSuccess.capture(), onError.capture())
        onSuccess.lastValue.invoke(QChatRoom().apply {
            id = roomId
        })
    }

    @Test
    fun loadChatRoomDifferentIdTest() {
        val loadChatroom = extractMethode(builder!!,  "loadChatRoom")
        loadChatroom.call(builder!!, context!!, true, listener)

        val roomId = QiscusChatLocal.getRoomId()
        val onSuccess = argumentCaptor<(QChatRoom) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        verify(widget)?.openChatRoomById(ArgumentMatchers.eq(roomId), onSuccess.capture(), onError.capture())
        onSuccess.lastValue.invoke(QChatRoom().apply {
            id = 12345L
        })
    }

    @Test
    fun loadChatRoomNullTest() {
        val loadChatroom = extractMethode(builder!!, "loadChatRoom")
        loadChatroom.call(builder!!, activity!!, true, null)

        val roomId = QiscusChatLocal.getRoomId()
        val onSuccess = argumentCaptor<(QChatRoom) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        verify(widget)?.openChatRoomById(
            ArgumentMatchers.eq(roomId),
            onSuccess.capture(),
            onError.capture()
        )

        onSuccess.lastValue.invoke(QChatRoom().apply {
            id = roomId
            participants = arrayListOf<QParticipant>().apply {
                add(
                    QParticipant().apply {
                        extras = JSONObject()
                    }
                )
            }
        })
    }

    @Test
    fun loadChatRoomErrorTest() {
        val loadChatroom = extractMethode(builder!!,  "loadChatRoom")
        loadChatroom.call(builder!!, context!!, true, listener)

        val roomId = QiscusChatLocal.getRoomId()
        val onSuccess = argumentCaptor<(QChatRoom) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        verify(widget)?.openChatRoomById(ArgumentMatchers.eq(roomId), onSuccess.capture(), onError.capture())
        onError.lastValue.invoke(Throwable("msg"))
    }

    @Test
    fun loadChatRoomErrorNoListenerTest() {
        MulchanWidget().generateMulchanWidget(application!!)

        val loadChatroom = extractMethode(builder!!,  "loadChatRoom")
        loadChatroom.call(builder!!, context!!, true, null)

        val roomId = QiscusChatLocal.getRoomId()
        val onSuccess = argumentCaptor<(QChatRoom) -> Unit>()
        val onError = argumentCaptor<(Throwable) -> Unit>()

        verify(widget)?.openChatRoomById(ArgumentMatchers.eq(roomId), onSuccess.capture(), onError.capture())
        onError.lastValue.invoke(Throwable("msg"))
    }

    @Test
    fun getQMessageTest() {
        builder?.manualSendMessage("msg")
        val loadChatroom = extractMethode(builder!!,  "getQMessage")
        loadChatroom.call(builder!!, 222L)
    }

}