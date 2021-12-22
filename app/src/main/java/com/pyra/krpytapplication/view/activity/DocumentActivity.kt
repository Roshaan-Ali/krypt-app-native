package com.pyra.krpytapplication.view.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.*
import com.pyra.krpytapplication.domain.UploadStatus
import com.pyra.krpytapplication.roomDb.entity.ChatMessagesSchema
import com.pyra.krpytapplication.viewmodel.ChatMessagesViewModel
import com.pyra.krpytapplication.viewmodel.GalleryViewModel
import getTxtFile
import kotlinx.android.synthetic.main.activity_document.*
import kotlinx.android.synthetic.main.activity_document.content
import kotlinx.android.synthetic.main.activity_document.doctitle
import kotlinx.android.synthetic.main.activity_view_document.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.*
import java.util.*

class DocumentActivity : BaseActivity() {

    var isNewDoc = true
    var path = ""
    var previousFileName = ""
    private val galleryViewModel by viewModels<GalleryViewModel>()
    private val chatMessageViewModel by viewModels<ChatMessagesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document)

        getIntentValues()

        if (!isNewDoc)
            readDocument()

        initListener()
        observeValues()
    }

    private fun getIntentValues() {

        intent.extras?.let {
            isNewDoc = it.getBoolean("isNewDoc")
            path = it.getString("path", "")
        }

    }

    private fun initListener() {

        save.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    Constants.Permission.READ_WRITE_STORAGE_PERM_LIST,
                    Constants.Permission.READ_WRITE_STORAGE_PERMISSIONS
                )
            } else {
                saveFile()
            }

        }
    }

    private fun readDocument() {

        if (path == "") {
            return
        }

        val file = File(path)
        //Read text from file
        val text = java.lang.StringBuilder()

        try {
            val br = BufferedReader(FileReader(file))
            var line: String?
            while (br.readLine().also { line = it } != null) {
                text.append(line)
                text.append('\n')
            }
            br.close()
        } catch (e: IOException) {
            //You'll need to add proper error handling here
        }
        content.setText(text)
        doctitle.setText(file.name)
        previousFileName = file.name
    }

    private fun saveFile() {

        if (doctitle.text.toString() != "")
            if (isNewDoc) {
                saveNewFile()
            } else {
                updateFile()
            }
    }

    private fun saveNewFile() {

        val contentTxt = content.text.trim()
        val file: File? = getTxtFile(doctitle.text.toString())

        if (file == null) {
            Snackbar.make(parentView, "File already exist", Snackbar.LENGTH_LONG).show()
            return
        }

        path = file.absolutePath
        blockView.visibility = View.VISIBLE
        progress.visibility = View.VISIBLE

        Coroutine.iOWorker {
            val writer = FileWriter(file, true)

            writer.append(contentTxt)
            writer.flush()
            writer.close()

            galleryViewModel.uploadDocument(file)
        }
    }

    private fun updateFile() {
        val contentTxt = content.text.trim()
        val file: File? = getTxtFile(doctitle.text.toString())

        blockView.visibility = View.VISIBLE
        progress.visibility = View.VISIBLE

        if (file == null) {
            //file already exists just update the content
            val newFile = File(getExternalFilesDir(null)!!.absolutePath, "/${doctitle.text}")
            path = newFile.absolutePath
            Coroutine.iOWorker {
                val writer = FileWriter(newFile, false)

                writer.append(contentTxt)
                writer.flush()
                writer.close()

                galleryViewModel.uploadDocument(newFile)
            }
        } else {
            //file does not exists creating new file and deleting previous one
            val previousFile =
                File(getExternalFilesDir(null)!!.absolutePath, "/${previousFileName}")
            previousFile.delete()

            path = file.absolutePath

            Coroutine.iOWorker {
                val writer = FileWriter(file, true)

                writer.append(contentTxt)
                writer.flush()
                writer.close()

                galleryViewModel.uploadDocument(file)
            }
        }
    }

    fun onBackButtonPressed(view: View) {
        onBackPressed()
    }

    fun onBlockClicked(view: View) {}

    private fun storeData(absolutePath: String?, url: String) {

        absolutePath?.let {

            val messagesEntity = ChatMessagesSchema()
            messagesEntity.messageId = UUID.randomUUID().toString()
            messagesEntity.message = ""
            messagesEntity.messageType = MessageType.DOCUMENT.toMessageString()
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
            messagesEntity.mediaDocumentName = File(absolutePath).name
            messagesEntity.mediaDocumentType = File(absolutePath).getDocumentType()

            chatMessageViewModel.addImageToLocal(messagesEntity)
        }

        Intent().apply {
            setResult(Activity.RESULT_OK, this)
            finish()
        }
    }

    private fun observeValues() {

        lifecycleScope.launch {
            galleryViewModel.awsUploadData.collect {
                when (it) {
                    UploadStatus.Loading -> {
                        blockView.visibility = View.VISIBLE
                        progress.visibility = View.VISIBLE
                    }
                    is UploadStatus.Success -> {
                        blockView.visibility = View.GONE
                        progress.visibility = View.GONE

                        it.awsUploadCompleted.message?.let { url ->

                            storeData(
                                path,
                                url
                            )

                        }
                    }
                    is UploadStatus.Error -> {
                        blockView.visibility = View.GONE
                        progress.visibility = View.GONE
                        Toast.makeText(this@DocumentActivity, it.error, Toast.LENGTH_SHORT).show()
                    }
                    UploadStatus.Empty -> {
                        blockView.visibility = View.GONE
                        progress.visibility = View.GONE
                    }
                }

            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constants.Permission.READ_WRITE_STORAGE_PERMISSIONS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                saveFile()
            } else {

                val message = if (isNewDoc) {
                    "Need permission to create document"
                } else {
                    "Need permission to view document"
                }
                Snackbar.make(parentView, message, Snackbar.LENGTH_LONG).show()
            }
        }

    }

}