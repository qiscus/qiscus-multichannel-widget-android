package com.qiscus.multichannel.data.repository.impl

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.qiscus.multichannel.data.repository.ChatroomRepository
import com.qiscus.multichannel.util.MultichannelConst
import com.qiscus.multichannel.basetest.InstrumentationBaseTest
import com.qiscus.sdk.chat.core.QiscusCore
import com.qiscus.sdk.chat.core.data.local.QiscusDataStore
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.data.model.QiscusNonce
import com.qiscus.sdk.chat.core.data.remote.QiscusApi
import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import rx.Observable
import java.util.*

@RunWith(AndroidJUnit4ClassRunner::class)
@ExtendWith(InstrumentationBaseTest::class)
internal class ChatroomRepositoryImplTest : InstrumentationBaseTest() {

    private var pusherApi: QiscusPusherApi? = null
    private var dataStore: QiscusDataStore? = null
    private var api: QiscusApi? = null
    private var qiscusCore: QiscusCore? = null
    private var repository: ChatroomRepository? = null

    @BeforeAll
    fun setUp() {
        setUpComponent()
        MockitoAnnotations.openMocks(this)
        qiscusCore = Mockito.mock(QiscusCore::class.java)
        api = Mockito.mock(QiscusApi::class.java)
        dataStore = Mockito.mock(QiscusDataStore::class.java)
        pusherApi = Mockito.mock(QiscusPusherApi::class.java)

        Mockito.`when`(qiscusCore?.api).thenReturn(api)
        MultichannelConst.setQiscusCore(qiscusCore)
        Mockito.`when`(MultichannelConst.qiscusCore()?.dataStore).thenReturn(dataStore)
        Mockito.`when`(MultichannelConst.qiscusCore()?.pusherApi).thenReturn(pusherApi)
        Mockito.`when`(MultichannelConst.qiscusCore()?.apps).thenReturn(application)

        repository = ChatroomRepositoryImpl()
    }

    @AfterAll
    fun tearDown() {
        dataStore = null
        api = null
        qiscusCore = null
        repository = null
    }

    private fun creteMessage(dataId: Long, type: String, message: String): QMessage {
        return QMessage().apply {
            id = dataId
            chatRoomId = dataId
            rawType = type
            text = message
        }
    }

    @Test
    fun sendMessageTest() {
        val roomId = 0L
        val message = creteMessage(roomId, "text", "message")

        Mockito.`when`(api?.sendMessage(ArgumentMatchers.eq(message))).thenReturn(
            Observable.just(message)
        )

        repository?.sendMessage(roomId, message, {}, {})
    }

    @Test
    fun sendMessageNotTypeTextTest() {
        val roomId = 0L
        val message = creteMessage(roomId, "custom", "message")

        Mockito.`when`(api?.sendMessage(ArgumentMatchers.eq(message))).thenReturn(
            Observable.just(message)
        )

        repository?.sendMessage(roomId, message, {}, {})
    }

    @Test
    fun sendMessageEmptyMessageTest() {
        val roomId = 0L
        val message = creteMessage(roomId, "text", "")

        Mockito.`when`(api?.sendMessage(ArgumentMatchers.eq(message))).thenReturn(
            Observable.just(message)
        )

        repository?.sendMessage(roomId, message, {}, {})
    }

    @Test
    fun sendMessageDifferentRoomIdTest() {
        val roomId = 0L
        val message = creteMessage(roomId, "text", "text")

        Mockito.`when`(api?.sendMessage(ArgumentMatchers.eq(message))).thenReturn(
            Observable.just(creteMessage(100L, "text", "text"))
        )

        repository?.sendMessage(roomId, message, {}, {})
    }

    @Test
    fun sendMessageDifferentRoomIdErrorTest() {
        val roomId = 0L
        val message = creteMessage(100L, "text", "text")

        Mockito.`when`(api?.sendMessage(ArgumentMatchers.eq(message))).thenReturn(
            Observable.just(message)
        )

        repository?.sendMessage(roomId, message, {}, {})
    }

    @Test
    fun sendMessageErrorTest() {
        val roomId = 0L
        val message = creteMessage(roomId, "text", "")

        Mockito.`when`(api?.sendMessage(ArgumentMatchers.eq(message))).thenReturn(
            Observable.error(Throwable("msg"))
        )

        repository?.sendMessage(roomId, message, {}, {})
    }

    @Test
    fun sendMessageErrorDifferentRoomIdTest() {
        val roomId = 0L
        val message = creteMessage(100L, "text", "")

        Mockito.`when`(api?.sendMessage(ArgumentMatchers.eq(message))).thenReturn(
            Observable.error(Throwable("msg"))
        )

        repository?.sendMessage(roomId, message, {}, {})
    }

    @Test
    fun publishCustomEventTest() {
        repository?.publishCustomEvent(0L, JSONObject())
    }

    @Test
    fun subscribeCustomEventTest() {
        repository?.subscribeCustomEvent(0L)
    }

    @Test
    fun loginMultichannelTest() {

        Mockito.`when`(api?.jwtNonce).thenReturn(
            Observable.just(QiscusNonce(Date(), "nonce"))
        )

        repository?.getJwtNonce("userId", "avatar", "extras", arrayListOf(), {}, {})
    }

    @Test
    fun loginMultichannelErrorTest() {

        Mockito.`when`(api?.jwtNonce).thenReturn(
            Observable.error(Throwable("msg"))
        )

        repository?.getJwtNonce("userId", "avatar", "extras", arrayListOf(), {}, {})
    }
}