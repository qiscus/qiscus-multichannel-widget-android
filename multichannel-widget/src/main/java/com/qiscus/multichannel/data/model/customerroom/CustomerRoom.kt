package com.qiscus.multichannel.data.model.customerroom

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.util.Objects

class CustomerRoom protected constructor(`in`: Parcel) : Parcelable {
    @SerializedName("room_id")
    var roomId: String? = `in`.readString()

    @SerializedName("room_badge")
    var roomBadge: String? = `in`.readString()

    @SerializedName("contact_id")
    private var contactId: Int?

    @SerializedName("last_comment_timestamp")
    var lastCommentTimestamp: String?

    @SerializedName("source")
    var source: String?

    @SerializedName("is_handled_by_bot")
    var isHandledByBot: Boolean

    @SerializedName("is_waiting")
    var isIsWaiting: Boolean
        private set

    @SerializedName("user_avatar_url")
    var userAvatarUrl: String? = ""

    @SerializedName("last_comment_sender")
    var lastCommentSender: String?

    @SerializedName("is_resolved")
    var isResolved: Boolean

    @SerializedName("user_id")
    var userId: String?

    @SerializedName("last_customer_timestamp")
    var lastCustomerTimestamp: String?

    @SerializedName("name")
    var name: String? = ""

    @SerializedName("last_comment_sender_type")
    var lastCommentSenderType: String?

    @SerializedName("id")
    var id: Int = 0

    @SerializedName("last_comment_text")
    var lastCommentText: String?

    @SerializedName("channel_id")
    var channelId: Int

    @SerializedName("room_type")
    var roomType: String?
    var channelName: String?
    var expiredRoomTime: Int = 0
    var time: String?
    var timeStamp: Long = 0
    var unreadCount: Int = 0

    init {
        contactId = `in`.readInt()
        lastCommentTimestamp = `in`.readString()
        source = `in`.readString()
        isHandledByBot = `in`.readByte().toInt() != 0
        isIsWaiting = `in`.readByte().toInt() != 0
        userAvatarUrl = `in`.readString()
        lastCommentSender = `in`.readString()
        isResolved = `in`.readByte().toInt() != 0
        userId = `in`.readString()
        lastCustomerTimestamp = `in`.readString()
        name = `in`.readString()
        lastCommentSenderType = `in`.readString()
        id = `in`.readInt()
        lastCommentText = `in`.readString()
        channelId = `in`.readInt()
        roomType = `in`.readString()
        channelName = `in`.readString()
        expiredRoomTime = `in`.readInt()
        time = `in`.readString()
        timeStamp = `in`.readLong()
        unreadCount = `in`.readInt()
    }

    fun getContactId(): Int {
        return if (contactId != null) contactId!! else 0
    }

    fun setContactId(contactId: Int?) {
        this.contactId = contactId
    }

    fun setIsWaiting(isWaiting: Boolean) {
        this.isIsWaiting = isWaiting
    }

    override fun describeContents(): Int {
        return id
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(roomId)
        dest.writeString(roomBadge)
        dest.writeInt(contactId!!)
        dest.writeString(lastCommentTimestamp)
        dest.writeString(source)
        dest.writeByte((if (isHandledByBot) 1 else 0).toByte())
        dest.writeByte((if (isIsWaiting) 1 else 0).toByte())
        dest.writeString(userAvatarUrl)
        dest.writeString(lastCommentSender)
        dest.writeByte((if (isResolved) 1 else 0).toByte())
        dest.writeString(userId)
        dest.writeString(lastCustomerTimestamp)
        dest.writeString(name)
        dest.writeString(lastCommentSenderType)
        dest.writeInt(id)
        dest.writeString(lastCommentText)
        dest.writeInt(channelId)
        dest.writeString(roomType)
        dest.writeString(channelName)
        dest.writeInt(expiredRoomTime)
        dest.writeString(time)
        dest.writeLong(timeStamp)
        dest.writeInt(unreadCount)
    }

    override fun equals(anyObject: Any?): Boolean {
        if (this === anyObject) return true
        if (anyObject !is CustomerRoom) return false
        return roomId == anyObject.roomId
    }

    override fun hashCode(): Int {
        return Objects.hash(roomId)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CustomerRoom?> = object : Parcelable.Creator<CustomerRoom?> {
            override fun createFromParcel(`in`: Parcel): CustomerRoom {
                return CustomerRoom(`in`)
            }

            override fun newArray(size: Int): Array<CustomerRoom?> {
                return arrayOfNulls(size)
            }
        }
    }
}