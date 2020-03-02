package com.qiscus.qiscusmultichannel.util

import android.content.Context
import android.content.SharedPreferences
import com.qiscus.qiscusmultichannel.MultichannelWidget

/**
 * Created on : 28/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
object QiscusChatLocal {

    private var sharedPreferences: SharedPreferences = MultichannelWidget.application
        .getSharedPreferences("qiscus_multichannel_chat", Context.MODE_PRIVATE)

    fun getPref(): SharedPreferences {
        return sharedPreferences
    }

    private fun getEditor(): SharedPreferences.Editor {
        return getPref().edit()
    }

    fun setRoomId(roomId: Long) {
        getEditor().putLong("roomId", roomId).apply()
    }

    fun getRoomId() = getPref().getLong("roomId", 0)

}