package com.qiscus.multichannel.data.model.user

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class UserPropertiesTest {

    @Test
    fun setTest() {
        val userProperties = UserProperties("key", "value")
        assertEquals(userProperties.key, "key")
        assertEquals(userProperties.value, "value")
    }
}