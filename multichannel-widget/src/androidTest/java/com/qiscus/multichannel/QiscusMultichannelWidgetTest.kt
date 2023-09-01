package com.qiscus.multichannel

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.core.util.Pair
import com.google.firebase.messaging.RemoteMessage
import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import com.qiscus.multichannel.data.local.QiscusChatLocal
import com.qiscus.multichannel.data.model.response.ResponseInitiateChat
import com.qiscus.multichannel.data.repository.ChatroomRepository
import com.qiscus.multichannel.data.repository.QiscusChatRepository
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.multichannel.util.MultichannelNotificationListener
import com.qiscus.multichannel.util.anyObject
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.local.QiscusCacheManager
import com.qiscus.sdk.chat.core.data.local.QiscusDataStore
import com.qiscus.sdk.chat.core.data.model.*
import com.qiscus.sdk.chat.core.data.remote.QiscusApi
import com.qiscus.sdk.chat.core.util.QiscusFirebaseMessagingUtil
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import rx.Observable
import java.lang.reflect.Constructor
import java.util.*

@ExtendWith(InstrumentationBaseTest::class)
internal class QiscusMultichannelWidgetTest : InstrumentationBaseTest() {

    /*private var cache: QiscusCacheManager? = null
    private var widget: QiscusMultichannelWidget? = null
    private var qiscusCore: QiscusCore? = null
    private var api: QiscusApi? = null
    private var qiscusConfig: QiscusCoreChatConfig? = null
    private var messageUtil: QiscusFirebaseMessagingUtil? = null
    private var account: QAccount? = null
    private var dataStore: QiscusDataStore? = null
    private var config: QiscusMultichannelWidgetConfig? = QiscusMultichannelWidgetConfig()
    private var color: QiscusMultichannelWidgetColor? = QiscusMultichannelWidgetColor()
    private var onSuccess: ArgumentCaptor<NotificationListener>? = null
    private var onError = argumentCaptor<(Throwable) -> Unit>()

    @BeforeAll
    fun setUp() {
        setUpComponent()
        setToMock()
        setArgumentCaptor()
        setMockCondition()
        setupMultichannel()
    }

    private fun setToMock() {
        MockitoAnnotations.openMocks(this)
        qiscusCore = mock()
        api = mock()
        qiscusConfig = mock()
        messageUtil = mock()
        account = mock()
        dataStore = mock()
        cache = mock()
    }

    private fun setArgumentCaptor() {
        onSuccess = ArgumentCaptor.forClass(NotificationListener::class.java)
    }

    private fun setMockCondition() {
        `when`(account?.id).thenReturn("100")
        `when`(dataStore?.isContains(ArgumentMatchers.eq(message))!!).thenReturn(true)

        api?.let {
            `when`(it.jwtNonce).thenReturn(Observable.empty())
        }

        qiscusConfig?.let {
            `when`(it.isEnableLog).thenReturn(true)
            `when`(it.setEnableFcmPushNotification(ArgumentMatchers.eq(true))).thenReturn(it)
            `when`(it.setNotificationListener(onSuccess!!.capture())).thenReturn(it)
            `when`(it.enableDebugMode(ArgumentMatchers.eq(false))).thenReturn(it)
        }

        qiscusCore!!.let {
            `when`(it.apps).thenReturn(application!!)
            `when`(it.chatConfig).thenReturn(qiscusConfig!!)
            `when`(it.api).thenReturn(api!!)
            `when`(it.appId).thenReturn("appId")
            `when`(it.firebaseMessagingUtil).thenReturn(messageUtil!!)
            `when`(it.qiscusAccount).thenReturn(account!!)
            `when`(it.dataStore).thenReturn(dataStore!!)
            `when`(it.cacheManager).thenReturn(cache)
            `when`(it.cacheManager.lastChatActivity).thenReturn(Pair(true, 1L))
        }

    }

    @BeforeEach
    fun before() {
        MultichannelConst.setQiscusCore(qiscusCore)

        clearInvocations(qiscusCore)
        clearInvocations(api)
        clearInvocations(qiscusConfig)
        clearInvocations(messageUtil)
        clearInvocations(account)
        clearInvocations(dataStore)
        clearInvocations(cache)
    }

    @AfterAll
    fun tearDown() {
        tearDownComponent()
        qiscusCore = null
        widget = null
        config = null
        color = null
        cache = null
    }

    private fun setupMultichannel() {
        QiscusMultichannelWidget.setup(
            application!!, qiscusCore!!, "appId", "key"
        )
        setListener(qiscusCore!!)
        widget = QiscusMultichannelWidget.instance
    }

    @Test
    fun setupWithConfig() {
        widget = QiscusMultichannelWidget.setup(
            application!!, qiscusCore!!, "appId", config!!, "key"
        )
        setListener(qiscusCore!!)
    }

    @Test
    fun testSetupWithColor() {
        widget = QiscusMultichannelWidget.setup(
            application!!, qiscusCore!!, "appId", config!!, color!!, "key"
        )
        setListener(qiscusCore!!)
    }

    @Test
    fun setCoreConfig() {
        val core = prepareForConfig()
        val setConfiguration = extractMethode(widget!!, "setCoreConfig", 2)
        setConfiguration.call(widget!!, core)

        setListener(core)
    }

    @Test
    fun setCoreConfigPnNoContext() {
        val core = prepareForConfig()
        val setConfiguration = extractMethode(widget!!, "setCoreConfig", 2)
        setConfiguration.call(widget!!, core)

        setListener(core, false)
    }

    @Test
    fun setCoreConfigPnNoMessage() {
        val core = prepareForConfig()
        val setConfiguration = extractMethode(widget!!, "setCoreConfig", 2)
        setConfiguration.call(widget!!, core)

        setListener(core, isWithMsg =  false)
    }

    @Test
    fun setCoreConfigPnNoContextAndMessage() {
        val core = prepareForConfig()
        val setConfiguration = extractMethode(widget!!, "setCoreConfig", 2)
        setConfiguration.call(widget!!, core)

        setListener(core, isWithContext = false, isWithMsg = false)
    }

    @Test
    fun setCoreConfigWithNotifListener() {
        val core = prepareForConfig()
        config?.setEnableNotification(true)
        config?.setNotificationListener(object : MultichannelNotificationListener {
            override fun handleMultichannelListener(context: Context?, qiscusComment: QMessage?) {
                // ignored
            }

        })

        val setConfiguration = extractMethode(widget!!, "setCoreConfig", 2)
        setConfiguration.call(widget!!, core)

        setListener(core)

    }

    @Test
    fun setCoreConfigDisableNotif() {
        val core = prepareForConfig()
        config?.setEnableNotification(false)
        config?.setNotificationListener(null)

        val setConfiguration = extractMethode(widget!!, "setCoreConfig", 2)
        setConfiguration.call(widget!!, core)

        setListener(core)
    }

    private fun prepareForConfig(): QiscusCore {
        val core: QiscusCore = mock()
        val chatConfig: QiscusCoreChatConfig = mock()
        `when`(core.chatConfig).thenReturn(chatConfig)
        `when`(core.chatConfig.setEnableFcmPushNotification(ArgumentMatchers.eq(true))).thenReturn(
            chatConfig
        )
        `when`(core.chatConfig.setNotificationListener(onSuccess?.capture())).thenReturn(chatConfig)
        `when`(core.dataStore).thenReturn(dataStore)

        MultichannelConst.setQiscusCore(core)
        return core
    }

    private val message = QMessage().apply {
        id = 100
        sender = QUser().apply {
            id = "100"
        }
    }

    //    private fun setListener() {
    private fun setListener(core: QiscusCore, isWithContext: Boolean = true, isWithMsg: Boolean = true) {
        onSuccess?.let {
            verify(core.chatConfig)?.notificationListener = it.capture()

            val msg =  if (isWithMsg) message else null
            val ctx = if (isWithContext) context else null
            it.value.onHandlePushNotification(ctx, msg)
        }
    }

    @Test
    fun getComponent() {
        assertNotNull(
            widget?.getComponent()
        )
    }

    @Test
    fun getConfig() {
        assertNotNull(
            widget?.getConfig()
        )
    }

    @Test
    fun getColor() {
        assertNotNull(
            widget?.getColor()
        )
    }

    @Test
    fun loginMultiChannelSuccessAll() {
        val widgetComponent: QWidgetComponent = mock()
        val chatroomRepository: ChatroomRepository = mock()
        val qiscusChatRepository: QiscusChatRepository = mock()
        val core: QiscusCore = qiscusCoreMock()
        MultichannelConst.setQiscusCore(core)

        val qiscusWidget = createWidgetInstance(widgetComponent, core, chatroomRepository, qiscusChatRepository)

        /*qiscusWidget?.loginMultiChannel(
            "name", "userId", "avatar", "{}", null, ArrayList(), { }, { }
        )

        loginMultiChannelNonce(chatroomRepository, true)
        loginMultiChannelInitiateChat(
            qiscusChatRepository,
            ResponseInitiateChat.Data(
                isSessional = true,
                customerRoom = ResponseInitiateChat.Data.CustomerRoom(roomId = "100"),
                identityToken = "identityToken"
            ),
            true
        )
        loginMultiChannelUserWithIdentityToken(core, true)*/
    }

    @Test
    fun loginMultiChannelErrorNonce() {
        val widgetComponent: QWidgetComponent = mock()
        val chatroomRepository: ChatroomRepository = mock()
        val qiscusChatRepository: QiscusChatRepository = mock()
        val core: QiscusCore = qiscusCoreMock()
        MultichannelConst.setQiscusCore(core)

        val qiscusWidget = createWidgetInstance(widgetComponent, core, chatroomRepository, qiscusChatRepository)

        /*qiscusWidget?.loginMultiChannel(
            "name", "userId", "avatar", "{}",null, ArrayList(), { }, { }
        )

        loginMultiChannelNonce(chatroomRepository, false)*/
    }

    @Test
    fun loginMultiChannelErrorInitiateChat() {
        val widgetComponent: QWidgetComponent = mock()
        val chatroomRepository: ChatroomRepository = mock()
        val qiscusChatRepository: QiscusChatRepository = mock()
        val core: QiscusCore = qiscusCoreMock()
        MultichannelConst.setQiscusCore(core)

        val qiscusWidget = createWidgetInstance(widgetComponent, core, chatroomRepository, qiscusChatRepository)

        /*qiscusWidget?.loginMultiChannel(
            "name", "userId", "avatar", "{}",null, ArrayList(), { }, { }
        )

        loginMultiChannelNonce(chatroomRepository, true)
        loginMultiChannelInitiateChat(
            qiscusChatRepository,
            ResponseInitiateChat.Data(
                isSessional = true,
                customerRoom = ResponseInitiateChat.Data.CustomerRoom(roomId = "100"),
                identityToken = "identityToken"
            ),
            false
        )*/
    }

    @Test
    fun loginMultiChannelErrorIdentityToken() {
        val widgetComponent: QWidgetComponent = mock()
        val chatroomRepository: ChatroomRepository = mock()
        val qiscusChatRepository: QiscusChatRepository = mock()
        val core: QiscusCore = qiscusCoreMock()
        MultichannelConst.setQiscusCore(core)

        val qiscusWidget = createWidgetInstance(widgetComponent, core, chatroomRepository, qiscusChatRepository)

        /*qiscusWidget?.loginMultiChannel(
            "name", "userId", "avatar", "{}",null, ArrayList(), { }, { }
        )

        loginMultiChannelNonce(chatroomRepository, true)
        loginMultiChannelInitiateChat(
            qiscusChatRepository,
            ResponseInitiateChat.Data(
                isSessional = true,
                customerRoom = ResponseInitiateChat.Data.CustomerRoom(roomId = "100"),
                identityToken = "identityToken"
            ),
            true
        )
        loginMultiChannelUserWithIdentityToken(core, false)*/
    }

    @Test
    fun loginMultiChannelNullCustomerRoomSessionalIdentityToken() {
        val widgetComponent: QWidgetComponent = mock()
        val chatroomRepository: ChatroomRepository = mock()
        val qiscusChatRepository: QiscusChatRepository = mock()
        val core: QiscusCore = qiscusCoreMock()
        MultichannelConst.setQiscusCore(core)

        val qiscusWidget = createWidgetInstance(widgetComponent, core, chatroomRepository, qiscusChatRepository)

        /*qiscusWidget?.loginMultiChannel(
            "name", "userId", "avatar", "{}", null, ArrayList(), { }, { }
        )

        loginMultiChannelNonce(chatroomRepository, true)
        loginMultiChannelInitiateChat(
            qiscusChatRepository,
            ResponseInitiateChat.Data(
                isSessional = null,
                customerRoom = null,
                identityToken = null
            ),
            true
        )*/
    }

    @Test
    fun loginMultiChannelNullRoomId() {
        val widgetComponent: QWidgetComponent = mock()
        val chatroomRepository: ChatroomRepository = mock()
        val qiscusChatRepository: QiscusChatRepository = mock()
        val core: QiscusCore = qiscusCoreMock()
        MultichannelConst.setQiscusCore(core)

        val qiscusWidget = createWidgetInstance(widgetComponent, core, chatroomRepository, qiscusChatRepository)

        /*qiscusWidget?.loginMultiChannel(
            "name", "userId", "avatar", "{}",null, ArrayList(), { }, { }
        )

        loginMultiChannelNonce(chatroomRepository, true)
        loginMultiChannelInitiateChat(
            qiscusChatRepository,
            ResponseInitiateChat.Data(
                isSessional = true,
                customerRoom = ResponseInitiateChat.Data.CustomerRoom(roomId = null),
                identityToken = "identityToken"
            ),
            true
        )
        loginMultiChannelUserWithIdentityToken(core, true)*/
    }

    @Test
    fun loginMultiChannelNullQiscusCore() {
        val widgetComponent: QWidgetComponent = mock()
        val chatroomRepository: ChatroomRepository = mock()
        val qiscusChatRepository: QiscusChatRepository = mock()
        val core: QiscusCore = qiscusCoreMock()
        MultichannelConst.setQiscusCore(null)

        val qiscusWidget = createWidgetInstance(widgetComponent, core, chatroomRepository, qiscusChatRepository)

        /*qiscusWidget?.loginMultiChannel(
            "name", "userId", "avatar", "{}", null, ArrayList(), { }, { }
        )

        loginMultiChannelNonce(chatroomRepository, true)
        loginMultiChannelInitiateChat(
            qiscusChatRepository,
            ResponseInitiateChat.Data(
                isSessional = true,
                customerRoom = ResponseInitiateChat.Data.CustomerRoom(roomId = "100"),
                identityToken = "identityToken"
            ),
            true
        )*/
    }

    private fun createWidgetInstance(
        widgetComponent: QWidgetComponent,
        core: QiscusCore,
        chatroomRepository: ChatroomRepository,
        qiscusChatRepository: QiscusChatRepository
    ): QiscusMultichannelWidget? {
        widgetComponent.also {
            `when`(it.getChatroomRepository()).thenReturn(chatroomRepository)
            `when`(it.getQiscusChatRepository()).thenReturn(qiscusChatRepository)
        }

        val constructor: Constructor<QiscusMultichannelWidget> = extractConstructor(
            QiscusMultichannelWidget::class.java,
            Application::class.java,
            QiscusCore::class.java,
            String::class.java,
            QWidgetComponent::class.java,
            QiscusMultichannelWidgetConfig::class.java,
            QiscusMultichannelWidgetColor::class.java,
            String::class.java
        )

        return constructor.newInstance(
            application!!, core, "appId", widgetComponent, config!!, color!!, "localKey"
        )
    }

    private fun loginMultiChannelNonce(chatroomRepository: ChatroomRepository, isSuccess: Boolean) {
        val onSuccessNonce = argumentCaptor<(QiscusNonce) -> Unit>()

        /*verify(chatroomRepository).getJwtNonce(
            ArgumentMatchers.eq("userId"), ArgumentMatchers.eq("avatar"), ArgumentMatchers.eq("{}"),
            ArgumentMatchers.eq(ArrayList()), onSuccessNonce.capture(), onError.capture()
        )*/

        if (isSuccess) onSuccessNonce.lastValue.invoke(QiscusNonce(Date(), "nonce"))
        else onError.lastValue.invoke(Throwable("msg"))
    }

    private fun loginMultiChannelInitiateChat(
        qiscusChatRepository: QiscusChatRepository,
        data: ResponseInitiateChat.Data,
        isSuccess: Boolean
    ) {
        val onSuccessResponseInitiateChat = argumentCaptor<(ResponseInitiateChat) -> Unit>()

        verify(qiscusChatRepository).initiateChat(
            anyObject(), onSuccessResponseInitiateChat.capture(), onError.capture()
        )

        if (isSuccess) onSuccessResponseInitiateChat.lastValue.invoke(ResponseInitiateChat(data, 200))
        else onError.lastValue.invoke(Throwable("msg"))
    }

    private fun loginMultiChannelUserWithIdentityToken(core: QiscusCore, isSuccess: Boolean) {
        val listener = argumentCaptor<QiscusCore.SetUserListener>()
        verify(core).setUserWithIdentityToken(anyObject(), listener.capture())

        if (isSuccess) listener.lastValue.onSuccess(QAccount())
        else listener.lastValue.onError(Throwable("msg"))
    }

    @Test
    fun getNonce() {
        widget?.getNonce({ }, { })
    }

    @Test
    fun getAppId() {
        assertNotNull(
            widget?.getAppId()
        )
    }

    @Test
    fun registerDeviceToken() {
        widget?.registerDeviceToken(
            qiscusCore!!, "token"
        )
    }

    @Test
    fun firebaseMessagingUtil() {
        widget?.firebaseMessagingUtil(
            RemoteMessage(Bundle())
        )
    }

    @Test
    fun updateUserTest() {
        updateUser(true)
    }

    @Test
    fun updateUserfailedTest() {
        updateUser(false)
    }

    private fun updateUser(isSuccess: Boolean) {
        val core: QiscusCore = qiscusCoreMock()

        val qWidget = QiscusMultichannelWidget.setup(
            application!!, core, "appId", "key"
        )
        setListener(qiscusCore!!)

        MultichannelConst.setQiscusCore(core)

        val json = JSONObject()
        qWidget.updateUser(
            "username", "avatar", json, { }, { }
        )

        val onSuccessUser: ArgumentCaptor<QiscusCore.SetUserListener> =
            ArgumentCaptor.forClass(QiscusCore.SetUserListener::class.java)
        verify(core).updateUser(
            ArgumentMatchers.eq("username"), ArgumentMatchers.eq("avatar"),
            ArgumentMatchers.eq(json), onSuccessUser.capture()
        )

        if (isSuccess) onSuccessUser.value.onSuccess(QAccount())
        else onSuccessUser.value.onError(Throwable("msg"))

        MultichannelConst.setQiscusCore(qiscusCore)
    }

    private fun qiscusCoreMock(): QiscusCore {
        val core: QiscusCore = mock()
        `when`(core.apps).thenReturn(application!!)
        `when`(core.chatConfig).thenReturn(qiscusConfig)
        `when`(core.dataStore).thenReturn(dataStore)
        `when`(core.cacheManager).thenReturn(cache)
        `when`(core.qiscusAccount).thenReturn(account)
        return core
    }

    @Test
    fun hasSetupUser() {
        widget?.hasSetupUser()
    }

    @Test
    fun getQiscusAccount() {
        widget?.getQiscusAccount()
    }

    @Test
    fun testSetUser() {
        widget?.setUser("userId", "userName", "avatar",  userProperties = HashMap())
    }

    @Test
    fun isLoggedIn() {
        widget?.isLoggedIn()
    }

    @Test
    fun isLoggedInNull() {
        val core: QiscusCore = mock()
        MultichannelConst.setQiscusCore(core)
        `when`(core.qiscusAccount).thenReturn(null)
        widget?.isLoggedIn()
        MultichannelConst.setQiscusCore(qiscusCore)
    }

    @Test
    fun setUserCheck() {
        widget?.setUser("userId", "userName", "avatar",null)

        val userProperties = mapOf(
            "city" to "jogja",
            "job" to "developer"
        )
        widget?.setUser("userId", "userName", "avatar", userProperties)

        widget?.userCheck { _, _ ->
            run { }
        }
    }

    @Test
    fun initiateChat() {
        widget?.initiateChat()
    }

    /*@Test
    fun openChatRoomById() {
        `when`(api?.getChatRoomInfo(
            ArgumentMatchers.eq(QiscusChatLocal.getRoomId())
        )).thenReturn(Observable.just(QChatRoom()))

        widget?.openChatRoom(context!!)
    }*/

    @Test
    fun openChatRoomByIdError() {
        `when`(
            api?.getChatRoomInfo(
                ArgumentMatchers.eq(QiscusChatLocal.getRoomId())
            )
        ).thenReturn(Observable.error(Throwable("msg")))

        widget?.openChatRoom(context!!)
    }

    @Test
    fun testOpenChatRoomById() {
    }

    @Test
    fun testOpenChatRoomById1() {
    }

    @Test
    fun openChatRoom() {
    }

    @Test
    fun testOpenChatRoom() {
    }

    @Test
    fun isMultichannelMessage() {
        val b = Bundle().apply {
            putString(
                "payload",
                "{ \"room_options\" : { \"app_code\" : \"${qiscusCore!!.appId}\"} }"
            )
        }
        val remote = RemoteMessage(b)

        val list = mutableListOf<QiscusCore>()
        list.add(qiscusCore!!)

        widget?.isMultichannelMessage(
            remote, list
        )
    }

    @Test
    fun isMultichannelMessageAppIdNotSame() {
        val b = Bundle().apply {
            putString(
                "payload",
                "{ \"room_options\" : { \"app_code\" : \"AppCodeNotSame\"} }"
            )
        }
        val remote = RemoteMessage(b)

        val list = mutableListOf<QiscusCore>()
        list.add(qiscusCore!!)

        widget?.isMultichannelMessage(
            remote, list
        )
    }

    @Test
    fun isMultichannelMessageError() {
        val b = Bundle().apply {
            putString(
                "payload",
                "{ \"room_options\" : \"not json\" }"
            )
        }
        val remote = RemoteMessage(b)

        val list = mutableListOf<QiscusCore>()
        list.add(qiscusCore!!)

        widget?.isMultichannelMessage(
            remote, list
        )
    }

    @Test
    fun isMultichannelMessageNull() {
        val b = Bundle()
        val remote = RemoteMessage(b)

        val list = mutableListOf<QiscusCore>()
        list.add(qiscusCore!!)

        widget?.isMultichannelMessage(
            remote, list
        )
    }

    @Test
    fun clearUser() {
        widget?.clearUser()
    }*/

}