package com.qiscus.multichannel.data.model

import com.qiscus.multichannel.data.model.user.UserProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DataInitialChatTest {

    @Test
    fun setTest() {
        val appId = "appId"
        val userId = "userId"
        val name = "name"
        val avatar = "avatar"
        val nonce = "nonce"
        val origin = "origin"
        val extras = "extra"
        val userProp: List<UserProperties> = arrayListOf()
        val channelId = 0

        val data =
            DataInitialChat(appId, userId, name, avatar, nonce, origin, extras, userProp, channelId)

        assertEquals(data.appId, appId)
        assertEquals(data.userId, userId)
        assertEquals(data.name, name)
        assertEquals(data.avatar, avatar)
        assertEquals(data.nonce, nonce)
        assertEquals(data.origin, origin)
        assertEquals(data.extras, extras)
        assertEquals(data.userProp, userProp)
        assertEquals(data.channelId, channelId)
    }
}