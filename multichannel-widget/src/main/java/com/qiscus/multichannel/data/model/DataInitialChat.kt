package com.qiscus.multichannel.data.model

import com.google.gson.annotations.SerializedName
import com.qiscus.multichannel.data.model.user.UserProperties

/**
 * Created on : 06/02/20
 * Author     : arioki
 * Name       : Yoga Setiawan
 * GitHub     : https://github.com/arioki
 */
data class DataInitialChat(
    @field:SerializedName("app_id")
    val appId: String?,

    @field:SerializedName("user_id")
    val userId: String?,

    @field:SerializedName("email")
    val email: String?,

    @field:SerializedName("name")
    val name: String?,

    @field:SerializedName("avatar")
    val avatar: String? = "",

    @field:SerializedName("nonce")
    val nonce: String?,

    @field:SerializedName("origin")
    val origin: String? = "",

    @field:SerializedName("extras")
    val extras: String? = null,

    @field:SerializedName("user_properties")
    val userProp: List<UserProperties>?,

    @field:SerializedName("channel_id")
    val channelId: Int?,

    @field:SerializedName("session_id")
    val sessionId: String?
)