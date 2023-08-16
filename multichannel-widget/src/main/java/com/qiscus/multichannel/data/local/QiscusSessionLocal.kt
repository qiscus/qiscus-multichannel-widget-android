package com.qiscus.multichannel.data.local

import android.content.Context
import android.content.SharedPreferences
import com.qiscus.multichannel.util.MultichannelConst

/**
 * Created on : 28/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
object QiscusSessionLocal {

    private var sharedPreferences: SharedPreferences = MultichannelConst.qiscusCore()!!.apps
        .getSharedPreferences("qiscus_multichannel_session", Context.MODE_PRIVATE)

    fun getPref(): SharedPreferences {
        return sharedPreferences
    }

    private fun getEditor(): SharedPreferences.Editor {
        return getPref().edit()
    }

    internal fun save(userId: String?, isSecure: Boolean, sessionId: String?) {
        val isSameUser = userId != null && userId == getUserId()

        getEditor()
            .putBoolean("qm_isInitiate", true)
            .putString(
                "qm_session_id",
                if (isSecure || !isSameUser) sessionId
                else if (isSameUser) getSessionId()
                else ""
            )
            .putBoolean("qm_is_secure", isSecure)
            .putString("qm_user_id", userId)
            .apply()
    }

    fun getSessionId(userId: String?): String? {
        return if (userId != null && userId == getUserId()) {
            return getSessionId()
        } else {
            null
        }
    }

    fun getUserId(): String? {
        return getPref().getString("qm_user_id","")
    }

    private fun getSessionId(): String? = getPref().getString("qm_session_id", "")

    fun isSecure(): Boolean = getPref().getBoolean("qm_is_secure", false)

    internal fun isInitiate(): Boolean = getPref().getBoolean("qm_isInitiate", false)

    fun removeInitiate() {
        getEditor().putBoolean("qm_isInitiate", false).apply()
    }

    fun clearPreferences() {
        getEditor().clear().apply()
    }

}