package com.qiscus.multichannel.data.repository

import com.qiscus.multichannel.data.model.DataInitialChat
import com.qiscus.multichannel.data.model.customerroom.DataCustomerRoom
import com.qiscus.multichannel.data.model.response.ResponseInitiateChat
import com.qiscus.multichannel.util.MultichannelConst
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import rx.Observable

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
            .baseUrl(MultichannelConst.BASE_URL)
            .client(client.build())
            .build()
        return retrofit.create(Api::class.java)
    }

    interface Api {
        @Headers("Content-Type: application/json")
        @POST("api/v2/qiscus/initiate_chat")
        fun initiateChat(@Body dataInitialChat: DataInitialChat): Call<ResponseInitiateChat>

        @GET("{appCode}/get_session")
        fun sessionalCheck(@Path("appCode") appCode: String?): Call<ResponseInitiateChat>

        @GET("api/v2/customer_rooms/{room_id}")
        fun getCustomerRoomById(@Path("room_id") roomId: Long): Observable<DataCustomerRoom>

        /*@GET("api/v1/qiscus/room/{room_id}/user_info")
        fun getUserInfo(@Path("room_id") roomId: Long?): Observable<UserInfo?>?*/
    }
}

