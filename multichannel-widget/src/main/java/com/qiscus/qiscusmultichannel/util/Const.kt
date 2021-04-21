package com.qiscus.qiscusmultichannel.util

import com.qiscus.sdk.chat.core.QiscusCore

object Const {
    private var qiscusCore: QiscusCore? = null
    private var allQiscusCore: MutableList<QiscusCore> = ArrayList()
    fun setQiscusCore(qiscusCore: QiscusCore?) {
        Const.qiscusCore = qiscusCore
    }
    fun qiscusCore(): QiscusCore? {
        return if (qiscusCore != null) {
            qiscusCore
        } else {
            try {
                throw Exception("QiscusCore null")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            null
        }
    }

    fun setAllQiscusCore(allQiscusCores: MutableList<QiscusCore>){
       Const.allQiscusCore =  allQiscusCores
    }

    fun getAllQiscusCore(): MutableList<QiscusCore> {
       return allQiscusCore
    }
}