/**
 * Created by huseinmuhdhor on 28/4/2021
 */

package com.qiscus.multichannel.util

import android.webkit.MimeTypeMap
import com.qiscus.sdk.chat.core.util.QiscusFileUtil
import java.io.File

class WidgetFileUtil {
    companion object {
        fun isVideo(file: File): Boolean? {
            val extension = QiscusFileUtil.getExtension(file)
            val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

            return type?.contains("video")
        }
    }
}