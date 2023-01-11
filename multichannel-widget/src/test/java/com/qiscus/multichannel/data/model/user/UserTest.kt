package com.qiscus.multichannel.data.model.user

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class UserTest {

    @Test
    fun setTest() {
        val userId = "userId"
        val name = "name"
        val avatar = "avatar"
        val userProperties = mutableMapOf<String, String>()
        userProperties["key"] = "value"

        val user = User(userId, name, avatar, userProperties)

        assertEquals(user.userId, userId)
        assertEquals(user.name, name)
        assertEquals(user.avatar, avatar)
        assertEquals(user.userProperties, userProperties)
    }

}