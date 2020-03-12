package com.qiscus.qiscusmultichannel.data.model

import com.google.gson.annotations.SerializedName

/**
 * Created on : 06/02/20
 * Author     : arioki
 * Name       : Yoga Setiawan
 * GitHub     : https://github.com/arioki
 */
data class DataInitialChat(
    @SerializedName("app_id") val appId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("avatar") val avatar: String? = "",
    @SerializedName("nonce") val nonce: String,
    @SerializedName("origin") val origin: String? = "",
    @SerializedName("extras") val extras: String? = null,
    @SerializedName("user_properties") val userProp: List<UserProperties>
)