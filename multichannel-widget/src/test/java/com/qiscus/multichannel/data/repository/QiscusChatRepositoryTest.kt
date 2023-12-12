package com.qiscus.multichannel.data.repository

import com.qiscus.multichannel.data.model.DataInitialChat
import com.qiscus.multichannel.data.model.response.ResponseInitiateChat
import com.qiscus.multichannel.data.model.user.UserProperties
import com.qiscus.multichannel.data.repository.impl.QiscusChatRepositoryImpl
import okhttp3.Request
import okio.Timeout
import org.junit.jupiter.api.*
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class QiscusChatRepositoryTest {

    private var api: QiscusChatApi.Api? = null
    private var repository: QiscusChatRepository? = null

    @BeforeAll
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        api = mock()
        repository = QiscusChatRepositoryImpl(api!!)
    }

    @AfterAll
    fun tearDown() {
        api = null
        repository = null
    }

    @Test
    fun initiateChatTest() {
        val appId = "app_id"
        val userId = "user_id"
        val name = "name"
        val avatar = "avatar"
        val nonce = "nonce"
        val origin = "origin"
        val extras = "extras"
        val userProp: List<UserProperties> = arrayListOf()
        val channelId = 0
        val data =
            DataInitialChat(appId, userId, name, "", avatar, nonce, origin, extras, userProp, channelId, "")

        val call = creteCallBack(
            ResponseInitiateChat(
                ResponseInitiateChat.Data(), 200
            ), true, isExecute = true
        )

        `when`(api?.initiateChat(data)).thenReturn(call)
        repository?.initiateChat(data, {}, {})
    }

    @Test
    fun initiateChatNotExecuteTest() {
        val appId = "app_id"
        val userId = "user_id"
        val name = "name"
        val avatar = "avatar"
        val nonce = "nonce"
        val origin = "origin"
        val extras = "extras"
        val userProp: List<UserProperties> = arrayListOf()
        val channelId = 0
        val data =
            DataInitialChat(appId, userId, name, "", avatar, nonce, origin, extras, userProp, channelId, "")

        val call = creteCallBack(
            ResponseInitiateChat(
                ResponseInitiateChat.Data(), 200
            ), true, isExecute = false
        )

        `when`(api?.initiateChat(data)).thenReturn(call)
        repository?.initiateChat(data, {}, {})
    }

    @Test
    fun initiateChatNullTest() {
        val appId = "app_id"
        val userId = "user_id"
        val name = "name"
        val avatar = "avatar"
        val nonce = "nonce"
        val origin = "origin"
        val extras = "extras"
        val userProp: List<UserProperties> = arrayListOf()
        val channelId = 0
        val data =
            DataInitialChat(appId, userId, name, "", avatar, nonce, origin, extras, userProp, channelId, "")

        val call = creteCallBack(
            ResponseInitiateChat(
                ResponseInitiateChat.Data(), 200
            ), false, isExecute = true
        )

        `when`(api?.initiateChat(data)).thenReturn(call)
        repository?.initiateChat(data, {}, {})
    }

    @Test
    fun initiateChatErrorTest() {
        val appId = "app_id"
        val userId = "user_id"
        val name = "name"
        val avatar = "avatar"
        val nonce = "nonce"
        val origin = "origin"
        val extras = "extras"
        val userProp: List<UserProperties> = arrayListOf()
        val channelId = 0
        val data =
            DataInitialChat(appId, userId, name, "", avatar, nonce, origin, extras, userProp, channelId, "")

        val call = creteCallBack(
            ResponseInitiateChat(
                ResponseInitiateChat.Data(), 200
            ), false, isExecute = false
        )

        `when`(api?.initiateChat(data)).thenReturn(call)
        repository?.initiateChat(data, {}, {})
    }

    private fun <T> creteCallBack(data: T, isSuccess: Boolean, isExecute: Boolean) = object : Call<T> {
        override fun clone(): Call<T> {
            return this
        }

        override fun execute(): Response<T> {
            return if (isSuccess && isExecute) Response.success(data)
            else if (!isSuccess && isExecute) Response.success(null)
            else Response.error(500, mock())
        }

        override fun enqueue(callback: Callback<T>) {
            if (isSuccess || isExecute) callback.onResponse(this, execute())
            else callback.onFailure(this, Throwable("msg"))
        }

        override fun isExecuted(): Boolean {
            return true
        }

        override fun cancel() {
        }

        override fun isCanceled(): Boolean {
            return isSuccess
        }

        override fun request(): Request {
            return mock()
        }

        override fun timeout(): Timeout {
            TODO("Not yet implemented")
        }

    }

    @Test
    fun checkSessionalTest() {

        val call = creteCallBack(
            ResponseInitiateChat(
                ResponseInitiateChat.Data(), 200
            ), true, isExecute = true
        )

        `when`(api?.sessionalCheck("")).thenReturn(call)
        repository?.checkSessional("", {}, {})
    }

    @Test
    fun checkSessionalNotExecuteTest() {

        val call = creteCallBack(
            ResponseInitiateChat(
                ResponseInitiateChat.Data(), 200
            ), true, isExecute = false
        )

        `when`(api?.sessionalCheck("")).thenReturn(call)
        repository?.checkSessional("", {}, {})
    }

    @Test
    fun checkSessionalNullTest() {

        val call = creteCallBack(
            ResponseInitiateChat(
                ResponseInitiateChat.Data(), 200
            ), false, isExecute = true
        )

        `when`(api?.sessionalCheck("")).thenReturn(call)
        repository?.checkSessional("", {}, {})
    }

    @Test
    fun checkSessionalErrorTest() {

        val call = creteCallBack(
            ResponseInitiateChat(
                ResponseInitiateChat.Data(), 200
            ), false, isExecute = false
        )

        `when`(api?.sessionalCheck("")).thenReturn(call)
        repository?.checkSessional("", {}, {})
    }
}