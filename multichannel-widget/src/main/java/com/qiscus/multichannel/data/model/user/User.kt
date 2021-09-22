package com.qiscus.multichannel.data.model.user

import java.io.Serializable

/**
 * Created on : 03/09/21
 * Author     : mmnuradityo
 * GitHub     : https://github.com/mmnuradityo
 */
data class User(
    var userId: String = "",
    var name: String = "",
    var avatar: String = "",
    val userProperties: Map<String, String>? = null
) : Serializable