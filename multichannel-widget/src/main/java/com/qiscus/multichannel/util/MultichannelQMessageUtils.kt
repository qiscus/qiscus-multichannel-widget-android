package com.qiscus.multichannel.util

class MultichannelQMessageUtils {
    companion object {

        fun getFileName(textMessage: String?) : String {
            if (textMessage == null) return ""
            var fileNameEndIndex: Int = textMessage.lastIndexOf("[/file]")
            if (fileNameEndIndex == -1) {
                fileNameEndIndex = textMessage.lastIndexOf("[/sticker]")
            }
            return if (fileNameEndIndex > -1) textMessage.substring(
                textMessage.lastIndexOf('/', fileNameEndIndex) + 1, fileNameEndIndex
            ) else ""
        }

    }
}