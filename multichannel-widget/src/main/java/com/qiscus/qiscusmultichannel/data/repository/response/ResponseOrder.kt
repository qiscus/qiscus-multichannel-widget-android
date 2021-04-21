package com.qiscus.qiscusmultichannel.data.repository.response


import com.google.gson.annotations.SerializedName

data class ResponseOrder(
    @SerializedName("chat")
    val chat: Chat,
    @SerializedName("order")
    val order: Order
) {
    data class Chat(
        @SerializedName("room_avatar_url")
        val roomAvatarUrl: String,
        @SerializedName("room_channel_id")
        val roomChannelId: String,
        @SerializedName("room_id")
        val roomId: String,
        @SerializedName("room_name")
        val roomName: String,
        @SerializedName("room_options")
        val roomOptions: String,
        @SerializedName("room_type")
        val roomType: String
    )

    data class Order(
        @SerializedName("id")
        val id: String
    )
}