package com.pyra.krpytapplication.utils

import android.content.Context
import android.graphics.*
import android.graphics.Paint.FILTER_BITMAP_FLAG
import android.media.ExifInterface
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import getNewFileName
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.jvm.Throws


internal object ImageUtil {


    @Throws(IOException::class)
    fun compressImage(
        imageFile: File,
        reqWidth: Float,
        reqHeight: Float,
        compressFormat: Bitmap.CompressFormat,
        quality: Int,
        destinationPath: String
    ): File {

        var fileOutputStream: FileOutputStream? = null

        val file = File(destinationPath).parentFile
        if (!file!!.exists()) {
            file.mkdirs()
        }
        try {
            fileOutputStream = FileOutputStream(destinationPath)
            // write the compressed bitmap at the destination specified by destinationPath.
            decodeSampledBitmapFromFile(
                imageFile,
                reqWidth,
                reqHeight
            )!!.compress(
                compressFormat,
                quality,
                fileOutputStream
            )
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.flush()
                fileOutputStream.close()
            }
        }

        return File(destinationPath)
    }

    @Throws(IOException::class)
    fun decodeSampledBitmapFromFile(imageFile: File, reqWidth: Float, reqHeight: Float): Bitmap? {
        // First decode with inJustDecodeBounds=true to check dimensions

        var scaledBitmap: Bitmap? = null
        var bmp: Bitmap?

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        bmp = BitmapFactory.decodeFile(imageFile.absolutePath, options)

        var actualHeight = options.outHeight
        var actualWidth = options.outWidth

        var imgRatio = actualWidth.toFloat() / actualHeight.toFloat()
        val maxRatio = reqWidth / reqHeight

        if (actualHeight > reqHeight || actualWidth > reqWidth) {
            //If Height is greater
            when {
                imgRatio < maxRatio -> {
                    imgRatio = reqHeight / actualHeight
                    actualWidth = (imgRatio * actualWidth).toInt()
                    actualHeight = reqHeight.toInt()

                }  //If Width is greater
                imgRatio > maxRatio -> {
                    imgRatio = reqWidth / actualWidth
                    actualHeight = (imgRatio * actualHeight).toInt()
                    actualWidth = reqWidth.toInt()
                }
                else -> {
                    actualHeight = reqHeight.toInt()
                    actualWidth = reqWidth.toInt()
                }
            }
        }

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(
            options,
            actualWidth,
            actualHeight
        )
        options.inJustDecodeBounds = false
        options.inTempStorage = ByteArray(16 * 1024)

        try {
            bmp = BitmapFactory.decodeFile(imageFile.absolutePath, options)
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
            bmp!!, middleX - bmp.width / 2,
            middleY - bmp.height / 2, Paint(FILTER_BITMAP_FLAG)
        )
        bmp.recycle()
        val exif: ExifInterface
        try {
            exif = ExifInterface(imageFile.absolutePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            val matrix = Matrix()
            when (orientation) {
                6 -> matrix.postRotate(90f)
                3 -> matrix.postRotate(180f)
                8 -> matrix.postRotate(270f)
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap, 0, 0, scaledBitmap.width,
                scaledBitmap.height, matrix, true
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return scaledBitmap


    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            inSampleSize *= 2
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    //image compression
    fun getCompressedFile(
        context: Context?,
        file: File?,
        quality: Int
    ): File? {

        val newFileName =
            "IMG_" + "_" + System.currentTimeMillis().toString() + ".jpg"
        val destiniFile = File(context?.getExternalFilesDir(null)!!.absolutePath, "/$newFileName")

        if (!destiniFile.exists()) {
            destiniFile.createNewFile()
        }


        var compressedImage: File? = null
        return try {
            compressedImage = Compressor(context)
                .setQuality(quality)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .compressToFile(file!!, destiniFile)
            compressedImage
        } catch (e: IOException) {
            e.printStackTrace()
            file
        }
    }

    fun getThumbnail(context: Context, file: File, quality: Int, fileType: Int): File? {

        val newFileName = getNewFileName(MediaType.IMAGE.value, "")

        val newFile = File(context.getExternalFilesDir(null)!!.absolutePath, "/$newFileName")


        if (!newFile.exists()) {
            newFile.createNewFile()
        }

        var fileOutputStream = FileOutputStream(newFile.absolutePath)

        var bitmap: Bitmap? = null
        if (fileType == MediaType.IMAGE.value) {
            bitmap =
                ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(file.absolutePath), 20, 20)
        } else if (fileType == MediaType.VIDEO.value) {
            bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ThumbnailUtils.createVideoThumbnail(file, Size(20, 20), null)
            } else {
                ThumbnailUtils.createVideoThumbnail(
                    file.absolutePath,
                    MediaStore.Images.Thumbnails.MINI_KIND
                )
            }
        }

        bitmap?.let {
            bitmap.compress(Bitmap.CompressFormat.WEBP, quality, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            return newFile
        }

        return null
    }
}

enum class MediaType(val value: Int) {
    NONE(0),
    IMAGE(1),
    VIDEO(2),
    DOCUMENT(3),
    AUDIO(4)
}