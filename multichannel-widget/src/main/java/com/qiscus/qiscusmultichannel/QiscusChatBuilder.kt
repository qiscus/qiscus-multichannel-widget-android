package com.qiscus.qiscusmultichannel

/**
 * Created on : 05/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class QiscusChatBuilder {

    private var roomId: Long? = null
    private var userId: String = ""
    private var name: String = ""

    fun setRoomId(roomId: Long) = apply { this.roomId = roomId }

    fun getRoomId() = roomId

    fun setUserId(userId: String) = apply { this.userId = userId }

    fun getUserId() = userId

    fun setName(name: String) = apply { this.name = name }

    fun build() {

    }
}