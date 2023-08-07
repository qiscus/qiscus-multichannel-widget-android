package com.qiscus.multichannel.data.model.response


import com.google.gson.annotations.SerializedName

data class ResponseInitiateChat(

    @field:SerializedName("data")
    val data: Data,

    @field:SerializedName("status")
    val status: Int
) {
    data class Data(

        @field:SerializedName("customer_room")
        val customerRoom: CustomerRoom? = null,

        @field:SerializedName("is_sessional")
        val isSessional: Boolean? = null,

        @field:SerializedName("sdk_user")
        val sdkUser: Any? = null,

        @field:SerializedName("identity_token")
        val identityToken: String? = null
    ) {

        data class CustomerRoom(

            @field:SerializedName("room_id")
            val roomId: String? = null,

            @field:SerializedName("session_id")
            val sessionId: String? = null,

            @field:SerializedName("room_badge")
            val roomBadge: String? = null,

            @field:SerializedName("extras")
            val extras: Extras? = null,

            @field:SerializedName("last_comment_timestamp")
            val lastCommentTimestamp: Any? = null,

            @field:SerializedName("source")
            val source: String? = null,

            @field:SerializedName("is_handled_by_bot")
            val isHandledByBot: Boolean? = null,

            @field:SerializedName("is_waiting")
            val isWaiting: Any? = null,

            @field:SerializedName("user_avatar_url")
            val userAvatarUrl: String? = null,

            @field:SerializedName("last_comment_sender")
            val lastCommentSender: Any? = null,

            @field:SerializedName("is_resolved")
            val isResolved: Boolean? = null,

            @field:SerializedName("user_id")
            val userId: String? = null,

            @field:SerializedName("last_customer_timestamp")
            val lastCustomerTimestamp: Any? = null,

            @field:SerializedName("name")
            val name: String? = null,

            @field:SerializedName("last_comment_sender_type")
            val lastCommentSenderType: Any? = null,

            @field:SerializedName("id")
            val id: Int? = null,

            @field:SerializedName("last_comment_text")
            val lastCommentText: Any? = null,

            @field:SerializedName("channel_id")
            val channelId: Int? = null
        ) {
            data class Extras(

                @field:SerializedName("notes")
                val notes: Any? = null,

                @field:SerializedName("user_properties")
                val userProperties: List<Any?>? = null,

                @field:SerializedName("timezone_offset")
                val timezoneOffset: Any? = null,

                @field:SerializedName("additional_extras")
                val additionalExtras: AdditionalExtras? = null
            ) {
                data class AdditionalExtras(

                    @field:SerializedName("timezone_offset")
                    val timezoneOffset: Int? = null
                )

            }
        }
    }
}