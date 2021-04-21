package com.qiscus.qiscusmultichannel.data.repository.response


import com.google.gson.annotations.SerializedName

data class ResponseInitiateChat(
    @SerializedName("data")
    val `data`: Data
) {
    data class Data(
        @SerializedName("identity_token")
        val identityToken: String? = "",
        @SerializedName("is_sessional")
        val isSessional: Boolean? = false,
        @SerializedName("room_id")
        val roomId: String? = "",
        @SerializedName("sdk_user")
        val sdkUser: Any
    )
}