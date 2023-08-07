package com.qiscus.multichannel

import com.qiscus.multichannel.data.repository.ChatroomRepository
import com.qiscus.multichannel.data.repository.QiscusChatApi
import com.qiscus.multichannel.data.repository.QiscusChatRepository
import com.qiscus.multichannel.data.repository.impl.QiscusChatRepositoryImpl
import com.qiscus.multichannel.data.repository.impl.ChatroomRepositoryImpl

/**
 * Created on : 05/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */

 class QiscusMultichannelWidgetComponent : QWidgetComponent {

    private var chatroomRepository: ChatroomRepository? = null
    private var qiscusChatRepository: QiscusChatRepository? = null

    internal fun create(isEnableLog: Boolean): QiscusMultichannelWidgetComponent {
        chatroomRepository = ChatroomRepositoryImpl()
        qiscusChatRepository = QiscusChatRepositoryImpl(QiscusChatApi.create(isEnableLog))
        return this
    }

    override fun getChatroomRepository(): ChatroomRepository = chatroomRepository!!

    override fun getQiscusChatRepository(): QiscusChatRepository = qiscusChatRepository!!
}

interface QWidgetComponent {
    fun getChatroomRepository(): ChatroomRepository

    fun getQiscusChatRepository(): QiscusChatRepository
}