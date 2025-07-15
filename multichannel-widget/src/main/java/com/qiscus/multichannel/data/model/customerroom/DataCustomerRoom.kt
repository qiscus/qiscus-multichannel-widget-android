package com.qiscus.multichannel.data.model.customerroom

import com.google.gson.annotations.SerializedName

class DataCustomerRoom {
    @SerializedName("data")
    var data: Data? = null

    @SerializedName("status")
    var status: Int = 0
}