package com.pyra.krpytapplication.view.activity

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.*
import com.pyra.krpytapplication.databinding.ActivityEditImageBinding
import com.pyra.krpytapplication.domain.UploadStatus
import com.pyra.krpytapplication.roomDb.entity.ChatMessagesSchema
import com.pyra.krpytapplication.viewmodel.ChatMessagesViewModel
import com.pyra.krpytapplication.viewmodel.GalleryViewModel
import com.yalantis.ucrop.UCrop
import getNewFile
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class EditImageActivity : BaseActivity() {

    private lateinit var binding: ActivityEditImageBinding
    private var originalPath: String? = null
    var newFile: File? = null

    var isImageSetOnce = false

    private val galleryViewModel by viewModels<GalleryViewModel>()
    private val chatMessageViewModel by viewModels<ChatMessagesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_image)

        newFile = getNewFile()
        getIntentValues()
        setImageDrawer(originalPath)
        initListener()
        startCrop()
        observeValues()
    }

    private fun initListener() {

        binding.crop.setOnClickListener {
            startCrop()
        }

        binding.cancel.setOnClickListener {
            deleteCreatedFile()
        }

        binding.ok.setOnClickListener {

            binding.blockView.visibility = View.VISIBLE
            binding.progress.visibility = View.VISIBLE

            val bitmap: Bitmap? = getBitmapFromView(binding.drawerView, Color.WHITE)

            bitmap?.let {
                try {
                    FileOutputStream(newFile?.absolutePath).use { out ->
                        bitmap.compress(
                            Bitmap.CompressFormat.PNG,
                            100,
                            out
                        ) // bmp is your Bitmap instance
                    }
                    uploadFileToAbs()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

        }
    }

    private fun uploadFileToAbs() {

        binding.blockView.visibility = View.VISIBLE
        binding.progress.visibility = View.VISIBLE
        galleryViewModel.uploadImageFullyCompressed(newFile!!, MediaType.IMAGE.value)

    }

    private fun observeValues() {

        lifecycleScope.launch {
            galleryViewModel.awsUploadData.collect {
                when (it) {
                    UploadStatus.Loading -> {
                        binding.blockView.visibility = View.VISIBLE
                        binding.progress.visibility = View.VISIBLE
                    }
                    is UploadStatus.Success -> {
                        binding.blockView.visibility = View.GONE
                        binding.progress.visibility = View.GONE
                        it.awsUploadCompleted.fileUrl?.let { url ->

                            storeData(
                                it.awsUploadCompleted.file?.absolutePath,
                                url,
                                it.awsUploadCompleted.thumbUrl
                            )

                            finish()

                        }
                    }
                    is UploadStatus.Error -> {
                        binding.blockView.visibility = View.GONE
                        binding.progress.visibility = View.GONE
                        Toast.makeText(this@EditImageActivity, it.error, Toast.LENGTH_SHORT).show()
                    }
                    UploadStatus.Empty -> {
                        binding.blockView.visibility = View.GONE
                        binding.progress.visibility = View.GONE
                    }
                }


            }
        }

    }

    private fun storeData(absolutePath: String?, url: String, thumbUrl: String?) {

        absolutePath?.let {
            thumbUrl?.let {

                val messagesEntity = ChatMessagesSchema()
                messagesEntity.messageId = UUID.randomUUID().toString()
                messagesEntity.message = ""
                messagesEntity.messageType = MessageType.IMAGE.toMessageString()
                messagesEntity.messageStatus = ""
                messagesEntity.messageTime = ""
                messagesEntity.isSender = false
                messagesEntity.roomId = ""
                messagesEntity.isDeleted = false
                messagesEntity.isEdited = false
                messagesEntity.isUploaded = true
                messagesEntity.kryptId = ""
                messagesEntity.userImage = ""
                messagesEntity.userName = ""
                messagesEntity.mediaUrl = url
                messagesEntity.localMediaPath = absolutePath
                messagesEntity.mediaThumbUrl = thumbUrl

                chatMessageViewModel.addImageToLocal(messagesEntity)
            }
        }

    }

    fun getBitmapFromView(view: View, defaultColor: Int): Bitmap? {
        val bitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(defaultColor)
        view.draw(canvas)
        return bitmap
    }

    private fun setImageDrawer(path: String?) {

        path?.let {

            var exif: ExifInterface? = null
            try {
                exif = ExifInterface(it)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val orientation: Int = exif!!.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            val bmpFactoryOptions = BitmapFactory.Options()

            bmpFactoryOptions.inJustDecodeBounds = false
            val bmp = BitmapFactory
                .decodeStream(
                    File(it).inputStream(), null, bmpFactoryOptions
                )

            val alteredBitmap = Bitmap.createBitmap(
                bmp!!.width,
                bmp.height, bmp.config
            )

            val bmRotated: Bitmap? = rotateBitmap(alteredBitmap!!, orientation)

            binding.drawerView.setNewImage(bmRotated!!, bmp)
        }

    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap? {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> return bitmap
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return bitmap
        }
        return try {
            val bmRotated = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
            bitmap.recycle()
            bmRotated
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            null
        }
    }

    private fun startCrop() {
        originalPath?.let {

            newFile?.absolutePath?.let { newFile ->

                UCrop.of(Uri.fromFile(File(it)), Uri.fromFile(File(newFile)))
                    .start(this)

            }
        }
    }

    private fun getIntentValues() {

        originalPath = intent.getStringExtra("imageUrl")

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            isImageSetOnce = true
            val resultUri = UCrop.getOutput(data!!)
            setImageDrawer(newFile!!.absolutePath)

        } else if (resultCode == UCrop.RESULT_ERROR) {
            if (!isImageSetOnce) {
                deleteCreatedFile()
            }
        } else if (resultCode == UCrop.RESULT_CANCELLED) {
            if (!isImageSetOnce) {
                deleteCreatedFile()
            }

        }
    }

    private fun deleteCreatedFile() {

        newFile?.let { file ->
            file.delete()
            if (file.exists()) {
                file.canonicalFile.delete()
                if (file.exists()) {
                    applicationContext.deleteFile(file.name)
                }
            }
        }

        finish()

    }

    fun onBlockClicked(view: View) {}
}