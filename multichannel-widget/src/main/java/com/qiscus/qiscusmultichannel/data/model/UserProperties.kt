package com.qiscus.qiscusmultichannel.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created on : 12/03/20
 * Author     : Taufik Budi S
 * Github     : https://github.com/tfkbudi
 */

data class UserProperties (
    @SerializedName("key")
    var key: String = "",
    @SerializedName("value")
    var value: String = ""
): Serializable