package com.qiscus.multichannel.data.repository

import com.qiscus.multichannel.data.model.DataInitialChat
import com.qiscus.multichannel.data.repository.response.ResponseInitiateChat
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

object QiscusChatApi {
    fun create(isEnableLog: Boolean): Api {
        val client = OkHttpClient.Builder()
        if (isEnableLog) {
            client.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }

        val retrofit: Retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://qismo.qiscus.com/")
            .client(client.build())
            .build()
        return retrofit.create(Api::class.java)
    }

    interface Api {
        @Headers("Content-Type: application/json")
        @POST("api/v1/qiscus/initiate_chat")
        fun getNonce(@Body dataInitialChat: DataInitialChat): Call<ResponseInitiateChat>

        @POST("api/v1/qiscus/initiate_chat")
        fun initiateChat(@Body dataInitialChat: DataInitialChat): Call<ResponseInitiateChat>

        @GET("{appCode}/get_session")
        fun sessionalCheck(@Path("appCode") appCode: String?): Call<ResponseInitiateChat>
    }
}

