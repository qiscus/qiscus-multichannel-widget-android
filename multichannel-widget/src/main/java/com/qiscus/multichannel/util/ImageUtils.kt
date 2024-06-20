package com.qiscus.multichannel.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.drawable.DrawableCompat
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ImageUtils {

    companion object {

        fun getBitmapNotifFromURL(strURL: String): Bitmap? {
            return if (strURL.contains(".gif") || strURL.contains(".svg")) null else getBitmapFromURL(
                strURL
            )
        }

        fun getBitmapFromURL(strURL: String?): Bitmap? {
            var connection: HttpURLConnection? = null
            var resultBitmap: Bitmap? = null
            try {
                val url = URL(strURL)
                connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    resultBitmap = ImageUtils.getCircleCroppedBitmap(
                        BitmapFactory.decodeStream(connection.inputStream)
                    )
                }
            } catch (e: IOException) {
                // ignored
            } finally {
                connection?.disconnect()
            }
            return resultBitmap
        }

        private fun getCircleCroppedBitmap(bitmap: Bitmap?): Bitmap? {
            if (bitmap == null) return null
            val output = Bitmap.createBitmap(
                bitmap.width,
                bitmap.height, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(output)
            val color = -0xbdbdbe
            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)
            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
            canvas.drawCircle(
                (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
                (
                        bitmap.width / 2).toFloat(), paint
            )
            paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
            canvas.drawBitmap(bitmap, rect, rect, paint)
            return output
        }

        fun getBitmapFromVectorDrawable(drawable: Drawable?): Bitmap? {
            if (drawable == null) return null

            val wrappedDrawable = DrawableCompat.wrap(drawable).mutate()

            val bitmap = Bitmap.createBitmap(
                wrappedDrawable.intrinsicWidth,
                wrappedDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            wrappedDrawable.setBounds(0, 0, canvas.width, canvas.height)
            wrappedDrawable.draw(canvas)
            return bitmap
        }

    }
}
