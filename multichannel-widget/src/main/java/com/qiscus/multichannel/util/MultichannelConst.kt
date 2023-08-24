package com.qiscus.multichannel.util

import com.qiscus.sdk.chat.core.QiscusCore

internal object MultichannelConst {

    val TAKE_PICTURE_REQUEST = 3
    val IMAGE_GALLERY_REQUEST = 7

    val CAMERA_PERMISSION = arrayOf(
        "android.permission.CAMERA"
    )
    val CAMERA_PERMISSION_28 = arrayOf(
        "android.permission.CAMERA",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.READ_EXTERNAL_STORAGE"
    )
    val FILE_PERMISSION = arrayOf(
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.READ_EXTERNAL_STORAGE"
    )
    val RC_CAMERA_PERMISSION = 128
    val RC_FILE_PERMISSION = 130
    val ORIGIN = "android"
    val BASE_URL = "https://qismo.qiscus.com/"

    private var qiscusCore: QiscusCore? = null
    private var allQiscusCore: MutableList<QiscusCore> = ArrayList()

    fun setQiscusCore(qiscusCore: QiscusCore?) {
        MultichannelConst.qiscusCore = qiscusCore
    }

    fun qiscusCore(): QiscusCore? {
        return if (qiscusCore != null) {
            qiscusCore
        } else {
            try {
                throw Exception("QiscusCore null")
            } catch (e: Exception) {
               // ignore
            }
            null
        }
    }

    fun setAllQiscusCore(allQiscusCore: MutableList<QiscusCore>) {
        MultichannelConst.allQiscusCore = allQiscusCore
    }

    fun getAllQiscusCore(): MutableList<QiscusCore> {
        return allQiscusCore
    }
}