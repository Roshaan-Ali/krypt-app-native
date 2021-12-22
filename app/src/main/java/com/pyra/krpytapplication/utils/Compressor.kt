package com.pyra.krpytapplication.utils

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.File.separator
import java.io.IOException
import kotlin.jvm.Throws


class Compressor(context: Context) {
    //max width and height values of the compressed image is taken as 612x816
    private var maxWidth = 1280f
    private var maxHeight = 720f
    private var compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    private var quality = 90
    private var destinationDirectoryPath: String? = null

    init {
        destinationDirectoryPath = context.cacheDir.path + separator + "images"
    }

    fun setMaxWidth(maxWidth: Float): Compressor {
        this.maxWidth = maxWidth
        return this
    }

    fun setMaxHeight(maxHeight: Float): Compressor {
        this.maxHeight = maxHeight
        return this
    }

    fun setCompressFormat(compressFormat: Bitmap.CompressFormat): Compressor {
        this.compressFormat = compressFormat
        return this
    }

    fun setQuality(quality: Int): Compressor {
        this.quality = quality
        return this
    }

    fun setDestinationDirectoryPath(destinationDirectoryPath: String): Compressor {
        this.destinationDirectoryPath = destinationDirectoryPath
        return this
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun compressToFile(imageFile: File, compressedFileName: File): File {
        return ImageUtil.compressImage(
            imageFile, maxWidth, maxHeight, compressFormat, quality,
            compressedFileName.absolutePath
        )
    }

    @Throws(IOException::class)
    fun compressToBitmap(imageFile: File): Bitmap {
        return ImageUtil.decodeSampledBitmapFromFile(
            imageFile,
            maxWidth,
            maxHeight
        )!!
    }


}