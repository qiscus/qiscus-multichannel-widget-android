package com.qiscus.qiscusmultichannel.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.data.model.UserProperties
import org.json.JSONObject

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

    fun setHasMigration(hasMigration: Boolean) {
        getEditor().putBoolean("hasMigration", hasMigration).apply()
    }

    fun getHasMigration() = getPref().getBoolean("hasMigration", false)


    fun setRoomId(roomId: Long) {
        getEditor().putLong("roomId", roomId).apply()
    }

    fun getRoomId() = getPref().getLong("roomId", 0)

    fun saveExtras(extras: String) {
        getEditor().putString("qm_extras", extras).apply()
    }

    fun getExtras(): JSONObject? {
        val param = getPref().getString("qm_extras", null)
        return if (param == null) null else JSONObject(param)
    }

    fun saveUserProps(userProps: List<UserProperties>) {
        val param = Gson().toJson(userProps)
        getEditor().putString("qm_props", param).apply()
    }

    fun getUserProps(): List<UserProperties> {
        val param = getPref().getString("qm_props", "")
        return Gson().fromJson(param, Array<UserProperties>::class.java).toList()
    }

    fun saveUserId(userId: String) {
        getEditor().putString("qm_user_id", userId).apply()
    }

    fun getUserId(): String {
        return getPref().getString("qm_user_id","") ?: ""
    }

    fun saveAvatar(avatar: String?) {
        getEditor().putString("qm_avatar", avatar).apply()
    }

    fun getAvatar(): String? {
        return getPref().getString("qm_avatar","") ?: null
    }

    fun clearPreferences() {
        getEditor().clear().apply()
    }

}