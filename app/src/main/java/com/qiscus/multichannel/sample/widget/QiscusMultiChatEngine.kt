package com.qiscus.multichannel.sample.widget

import com.qiscus.sdk.chat.core.QiscusCore

/**
 * set chat core engine with multiple app id
 * */
class QiscusMultiChatEngine {

    companion object {
        val MULTICHANNEL_CORE = 0
        val ANOTHER_CHAT_CORE = 1
    }

    private val qiscusCores: MutableList<QiscusCore> = ArrayList()

    fun setCores() {
        qiscusCores.add(MULTICHANNEL_CORE, QiscusCore())
        qiscusCores.add(ANOTHER_CHAT_CORE, QiscusCore())
    }

    fun get(type: Int): QiscusCore {
        return qiscusCores[type]
    }

    fun allQiscusCore(): MutableList<QiscusCore> {
        return qiscusCores
    }

}