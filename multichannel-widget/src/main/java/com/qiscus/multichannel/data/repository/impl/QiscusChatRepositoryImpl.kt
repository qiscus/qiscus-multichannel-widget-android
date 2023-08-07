package com.qiscus.multichannel.data.repository.impl

import com.qiscus.multichannel.data.model.DataInitialChat
import com.qiscus.multichannel.data.model.response.ResponseInitiateChat
import com.qiscus.multichannel.data.repository.QiscusChatApi
import com.qiscus.multichannel.data.repository.QiscusChatRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class QiscusChatRepositoryImpl(val api: QiscusChatApi.Api) : QiscusChatRepository {

    override fun initiateChat(
        dataInitialChat: DataInitialChat,
        onSuccess: (ResponseInitiateChat) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        api.initiateChat(dataInitialChat).enqueue(object : Callback<ResponseInitiateChat?> {
            override fun onFailure(call: Call<ResponseInitiateChat?>, t: Throwable) {
                onError(t)
            }

            override fun onResponse(
                call: Call<ResponseInitiateChat?>,
                response: Response<ResponseInitiateChat?>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    }

                } else {
                    onError(Throwable("Error get data from api"))
                }
            }
        })
    }

    override fun checkSessional(
        appCode: String,
        onSuccess: (ResponseInitiateChat) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        api.sessionalCheck(appCode).enqueue(object : Callback<ResponseInitiateChat> {
            override fun onFailure(call: Call<ResponseInitiateChat>, t: Throwable) {
                onError(t)
            }

            override fun onResponse(
                call: Call<ResponseInitiateChat>,
                response: Response<ResponseInitiateChat>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        onSuccess(it)
                    }
                } else {
                    onError(Throwable("Error get data from api"))
                }
            }

        })
    }


}
