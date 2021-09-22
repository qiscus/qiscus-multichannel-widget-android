package com.qiscus.multichannel

import com.qiscus.multichannel.data.repository.QiscusChatApi
import com.qiscus.multichannel.data.repository.QiscusChatRepository
import com.qiscus.multichannel.data.repository.impl.ChatroomRepositoryImpl

/**
 * Created on : 05/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class QiscusMultichannelWidgetComponent(isEnableLog: Boolean) {

    var chatroomRepository = ChatroomRepositoryImpl()
    var qiscusChatRepository = QiscusChatRepository(QiscusChatApi.create(isEnableLog))

}