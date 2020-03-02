package com.qiscus.qiscusmultichannel.util

import android.graphics.*
import android.media.ExifInterface
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Environment
import com.qiscus.sdk.chat.core.custom.QiscusCore
import com.qiscus.sdk.chat.core.custom.data.local.QiscusCacheManager
import com.qiscus.sdk.chat.core.custom.util.QiscusFileUtil
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created on : 19/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
object QiscusImageUtil {

    fun getScaledBitmap(imageUri: Uri): Bitmap? {
        val filePath = QiscusFileUtil.getRealPathFromURI(imageUri)
        var scaledBitmap: Bitmap? = null

        val options = BitmapFactory.Options()

        //by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        //you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(filePath, options)

        var actualHeight = options.outHeight
        var actualWidth = options.outWidth

        if (actualWidth < 0 || actualHeight < 0) {
            val bitmap2 = BitmapFactory.decodeFile(filePath)
            actualWidth = bitmap2.getWidth()
            actualHeight = bitmap2.getHeight()
        }

        //max Height and width values of the compressed image is taken as 1440x900
        val maxHeight = QiscusCore.getChatConfig().getQiscusImageCompressionConfig().getMaxHeight()
        val maxWidth = QiscusCore.getChatConfig().getQiscusImageCompressionConfig().getMaxWidth()
        var imgRatio = (actualWidth / actualHeight).toFloat()
        val maxRatio = maxWidth / maxHeight

        //width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()

            }
        }

        //setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)

        //inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false

        //this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)

        try {
            //load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()

        }

        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }

        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f

        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)

        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bmp,
            middleX - bmp.getWidth() / 2,
            middleY - bmp.getHeight() / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )

        //check the rotation of the image and display it properly
        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath)

            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            val matrix = Matrix()
            if (orientation == 6) {
                matrix.postRotate(90f)
            } else if (orientation == 3) {
                matrix.postRotate(180f)
            } else if (orientation == 8) {
                matrix.postRotate(270f)
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap!!, 0, 0,
                scaledBitmap!!.getWidth(), scaledBitmap!!.getHeight(), matrix,
                true
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return scaledBitmap
    }

    fun compressImage(imageFile: File): File {

        var out: FileOutputStream? = null
        val filename = QiscusFileUtil.generateFilePath(imageFile.getName(), ".jpg")
        try {
            out = FileOutputStream(filename)

            //write the compressed bitmap at the destination specified by filename.
            QiscusImageUtil.getScaledBitmap(Uri.fromFile(imageFile))!!.compress(
                Bitmap.CompressFormat.JPEG,
                QiscusCore.getChatConfig().getQiscusImageCompressionConfig().getQuality(), out
            )

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            try {
                if (out != null) {
                    out!!.close()
                }
            } catch (ignored: IOException) {
                //Do nothing
            }

        }

        val compressedImage = File(filename)
        QiscusFileUtil.notifySystem(compressedImage)

        return compressedImage
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }

        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }

        return inSampleSize
    }

    fun isImage(file: File): Boolean {
        return QiscusFileUtil.isImage(file.getPath())
    }

    fun addImageToGallery(picture: File) {
        QiscusFileUtil.notifySystem(picture)
    }

    fun showImageFolderAppInGallery() {
        /*val nomedia = File(
            Environment.getExternalStorageDirectory().getPath(),
            QiscusFileUtil.IMAGE_PATH + File.separator +
                    QiscusCore.getApps().getString(com.qiscus.sdk.chat.core.custom.R.string.qiscus_nomedia)
        )
        if (nomedia.exists()) {
            nomedia.delete()
            //rescan media gallery for updating deleted .nomedia file
            QiscusFileUtil.notifySystem(nomedia)
        }*/
    }

    fun hideImageFolderAppInGallery() {
        /*val nomedia = File(
            Environment.getExternalStorageDirectory().getPath(),
            (QiscusFileUtil.IMAGE_PATH + File.separator +
                    QiscusCore.getApps().getString(com.qiscus.sdk.chat.core.custom.R))
        )

        if (!nomedia.getParentFile().exists()) {
            nomedia.getParentFile().mkdirs()
        }

        if (!nomedia.exists()) {
            try {
                if (nomedia.createNewFile()) {
                    QiscusFileUtil.notifySystem(nomedia)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }*/
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG-$timeStamp-"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        QiscusCacheManager.getInstance().cacheLastImagePath("file:" + image.getAbsolutePath())
        return image
    }

    fun getCircularBitmap(bm: Bitmap): Bitmap {
        val size = 192

        val bitmap = ThumbnailUtils.extractThumbnail(bm, size, size)

        val output =
            Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888)

        val canvas = Canvas(output)

        val color = -0x10000
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.getWidth(), bitmap.getHeight())
        val rectF = RectF(rect)

        paint.setAntiAlias(true)
        paint.setDither(true)
        paint.setFilterBitmap(true)
        canvas.drawARGB(0, 0, 0, 0)
        paint.setColor(color)
        canvas.drawOval(rectF, paint)

        paint.setColor(Color.BLUE)
        paint.setStyle(Paint.Style.STROKE)
        paint.setStrokeWidth(4.toFloat())
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    @JvmOverloads
    fun generateBlurryThumbnailUrl(
        imageUrl: String?,
        width: Int = 320,
        height: Int = 320,
        blur: Int = 300
    ): String? {
        if (imageUrl == null) {
            return null
        }

        var i = imageUrl!!.indexOf("upload/")
        if (i > 0) {
            i += 7
            var blurryImageUrl = imageUrl!!.substring(0, i)
            blurryImageUrl += "w_$width,h_$height,c_limit,e_blur:$blur/"
            var file = imageUrl!!.substring(i)
            i = file.lastIndexOf('.')
            if (i > 0) {
                file = file.substring(0, i)
            }
            return "$blurryImageUrl$file.png"
        }
        return imageUrl
    }
}