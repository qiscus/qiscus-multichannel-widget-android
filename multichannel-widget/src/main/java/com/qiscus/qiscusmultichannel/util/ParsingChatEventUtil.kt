package com.qiscus.qiscusmultichannel.util

import com.qiscus.qiscusmultichannel.MultichannelWidget
import com.qiscus.qiscusmultichannel.R
import com.qiscus.sdk.chat.core.data.model.QAccount
import org.json.JSONObject

/**
 * Created on : 2019-10-23
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */
class ParsingChatEventUtil {

    private object Holder {
        val local = ParsingChatEventUtil()
    }

    companion object {
        val instance: ParsingChatEventUtil by lazy { Holder.local }
    }

    fun parsingMessage(param: JSONObject, qiscusAccount: QAccount): String {
        val type = param.getString("type")
        var msg = ""
        val subjectUsername = param.getString("subject_username")
        val subjectEmail = param.getString("subject_email")

        msg += if (subjectEmail == qiscusAccount.id) getString(R.string.qiscus_you_mc) else subjectUsername
        return msg
    }

    private fun getString(str: Int): String {
        return MultichannelWidget.application.getString(str)
    }

    private fun getString(str: Int, param: String): String {
        return MultichannelWidget.application.getString(str, param)
    }

}