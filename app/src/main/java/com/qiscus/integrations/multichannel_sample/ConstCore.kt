package com.qiscus.integrations.multichannel_sample

import com.qiscus.sdk.chat.core.QiscusCore

object ConstCore {
    private var qiscusCore1: QiscusCore? = null
    private var qiscusCore2: QiscusCore? = null
    fun setCore(){
        qiscusCore1 = QiscusCore()
        qiscusCore2 = QiscusCore()
    }

    fun qiscusCore1(): QiscusCore {
       return this.qiscusCore1!!
    }

    fun qiscusCore2(): QiscusCore {
        return this.qiscusCore2!!
    }

    fun allQiscusCore() : MutableList<QiscusCore> {
        var qiscusCores: MutableList<QiscusCore> = ArrayList()
        if (qiscusCore1 != null) {
            qiscusCores.add(ConstCore.qiscusCore1())
        }

        if (qiscusCore2 != null) {
            qiscusCores.add(ConstCore.qiscusCore2())
        }

        return qiscusCores
    }
}