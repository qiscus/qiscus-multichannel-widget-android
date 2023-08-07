package com.qiscus.multichannel.data.repository

import com.qiscus.multichannel.data.model.DataInitialChat
import com.qiscus.multichannel.data.model.response.ResponseInitiateChat

interface QiscusChatRepository {
    fun initiateChat(
        dataInitialChat: DataInitialChat,
        onSuccess: (ResponseInitiateChat) -> Unit,
        onError: (Throwable) -> Unit
    )

    fun checkSessional(
        appCode: String,
        onSuccess: (ResponseInitiateChat) -> Unit,
        onError: (Throwable) -> Unit
    )
}