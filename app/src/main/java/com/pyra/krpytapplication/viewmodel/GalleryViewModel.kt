package com.pyra.krpytapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.pyra.krpytapplication.utils.*
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.domain.UploadStatus
import com.pyra.krpytapplication.repositories.implementations.AmazonRepository
import com.pyra.krpytapplication.repositories.implementations.VaultFragRepository
import com.pyra.krpytapplication.roomDb.ChatMessageSchemaFactory
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema
import com.pyra.krpytapplication.roomDb.entity.ChatMessagesSchema
import getRoomId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.math.RoundingMode
import java.text.DecimalFormat

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    var app = application
    var amazonRepository: AmazonRepository = AmazonRepository.getInstance()
    var repository = VaultFragRepository.getInstance((application as MyApp).getAppDatabase())


    var imageList = MutableLiveData<List<ChatMessagesSchema>>()
    var videoList = MutableLiveData<List<ChatMessagesSchema>>()
    var documentList = MutableLiveData<List<ChatMessagesSchema>>()


    var kryptCode = ""
    var roomId = ""
    var isGroup = false


    private val _awsUploadData = MutableStateFlow<UploadStatus>(UploadStatus.Empty)
    val awsUploadData: StateFlow<UploadStatus>
        get() = _awsUploadData


    fun uploadImageFullyCompressed(
        file: File,
        fileType: Int
    ) {
        val compressedFile = ImageUtil.getThumbnail(getApplication(), file, 1, fileType)

        compressedFile?.let {

            viewModelScope.launch {
                _awsUploadData.value = UploadStatus.Loading
                amazonRepository.uploadToAWS(getApplication(), compressedFile).observeForever {
                    it.error?.let { error ->
                        viewModelScope.launch {
                            if (!error) {
                                if (it.completed) {
                                    if (file.getFileType() == MediaType.VIDEO.value) {
                                        compressVideo(file, it.message!!)
                                    } else if (file.getFileType() == MediaType.DOCUMENT.value) {

                                    } else {
                                        uploadNormalImage(file, it.message!!)
                                    }

                                }
                            } else {
                                _awsUploadData.value = UploadStatus.Error(it.message!!)
                            }
                        }
                    }
                }
            }

        }

    }


    fun uploadDocument(
        file: File
    ) {

        file.let {

            viewModelScope.launch {
                _awsUploadData.value = UploadStatus.Loading
                amazonRepository.uploadToAWS(getApplication(), it).observeForever {
                    it.error?.let { error ->
                        viewModelScope.launch {
                            if (!error) {
                                if (it.completed) {
                                    _awsUploadData.value = UploadStatus.Success(it)
                                }
                            } else {
                                _awsUploadData.value = UploadStatus.Error(it.message!!)
                            }
                        }
                    }
                }
            }

        }

    }


    private fun uploadNormalImage(file: File, thumbUrl: String) {
        viewModelScope.launch {
            amazonRepository.uploadToAWS(getApplication(), file).observeForever {
                it.error?.let { error ->
                    viewModelScope.launch {
                        if (!error) {
                            if (it.completed) {
                                _awsUploadData.value = UploadStatus.Success(
                                    it.copy(
                                        thumbUrl = thumbUrl,
                                        fileUrl = it.message,
                                        file = file
                                    )
                                )
                            }
                        } else {
                            _awsUploadData.value = UploadStatus.Error(it.message!!)
                        }
                    }
                }
            }
        }


    }


    private fun compressVideo(file: File, thumbUrl: String) {

        val newFileName =
            "VID_" + "_" + System.currentTimeMillis().toString() + ".mp4"

        val newFile =
            File(getApplication<MyApp>().getExternalFilesDir(null)!!.absolutePath, "/$newFileName")
        if (!newFile.exists()) {
            newFile.createNewFile()
        }

        if (file.isCompresableFileSize()) {
            VideoCompressor.start(
                file.absolutePath,
                newFile.absolutePath,
                object : CompressionListener {
                    override fun onStart() {

                    }

                    override fun onSuccess() {

                        uploadNormalImage(file, thumbUrl)

                    }

                    override fun onFailure() {

                    }

                    override fun onProgress(percent: Float) {
                        println("Compress video percentage $percent")
                    }

                    override fun onCancelled() {

                    }

                })
        } else {
            try {

                val outputStream = FileOutputStream(newFile.absoluteFile)
                val inputStream = FileInputStream(file.absoluteFile)

                // Transfer bytes from in to out
                val buf = ByteArray(1024)
                var len = 0
                while (inputStream.read(buf).also { len = it } > 0) {
                    outputStream.write(buf, 0, len)
                }

                outputStream.flush()
                inputStream.close()
                outputStream.close()

                uploadNormalImage(file, thumbUrl)


            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }


    fun getImageList() {
        repository.getImageList()?.observeForever {
            imageList.value = it
        }
    }

    fun getVideoList() {
        repository.getVideoList()?.observeForever {
            videoList.value = it
        }
    }

    fun getDocumentList() {
        repository.getDocumentList()?.observeForever {
            documentList.value = it
        }
    }

    fun getImageCount(videoValue: Boolean): Int? {

        return if (videoValue) {
            videoList.value?.size
        } else {
            imageList.value?.size
        }
    }

    fun getThumpImage(position: Int, videoValue: Boolean): String? {
        return if (videoValue) {
            videoList.value?.get(position)?.localMediaPath
        } else {
            imageList.value?.get(position)?.localMediaPath
        }

    }


    fun getMediaUrl(position: Int, videoValue: Boolean): String? {
        return if (videoValue) {
            videoList.value?.get(position)?.mediaUrl
        } else {
            imageList.value?.get(position)?.mediaUrl
        }
    }


    fun getMediaThumpUrl(position: Int, videoValue: Boolean): String? {
        return if (videoValue) {
            videoList.value?.get(position)?.mediaThumbUrl
        } else {
            imageList.value?.get(position)?.mediaThumbUrl
        }
    }


    fun getDocumentListSize(): Int? {
        return documentList.value?.size
    }


    fun getDocFileName(position: Int): String? {
        return documentList.value?.get(position)?.mediaDocumentName
    }

    fun getDocFileType(position: Int): String? {
        return documentList.value?.get(position)?.mediaDocumentType
    }

    fun getDate(position: Int): String? {
        return documentList.value?.get(position)?.messageTime?.longDateToDisplayTimeString()
    }

    fun getDocFileSize(position: Int): String? {

        documentList.value?.get(position)?.localMediaPath?.let {
            val file = File(it)
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.CEILING
            return df.format(file.length() / 1024000f).toString() + " mb"
        }

        return null
    }


    fun getFile(position: Int): File? {
        documentList.value?.get(position)?.localMediaPath?.let {
            return File(it)
        }
        return null
    }

    fun getFilePath(position: Int): String? {
        return documentList.value?.get(position)?.localMediaPath
    }

    fun getMediaUrl(position: Int): String? {
        return documentList.value?.get(position)?.localMediaPath
    }

    fun sendImage(position: Int, isVideo: Boolean) {

        Coroutine.iOWorker {


            var selectedChatUsers: ChatListSchema? = null

            if (isGroup) {
                selectedChatUsers = repository.getChatDetails(roomId)
            } else {
                selectedChatUsers = repository.getChatDetails(getRoomId(app, kryptCode))
            }

            selectedChatUsers?.let {


                if (isVideo) {

                    val message: ChatMessagesSchema? = videoList.value?.get(position)

                    message?.let {
                        val chatMessagesSchema = ChatMessageSchemaFactory.createForwardMessage(
                            message, selectedChatUsers.kryptId, selectedChatUsers.roomId,
                            selectedChatUsers.roomName, selectedChatUsers.roomImage
                        )

                        repository.sendMessage(
                            chatMessagesSchema,
                            isGroup,
                            selectedChatUsers.groupType
                        )

                    }


                } else {


                    val message: ChatMessagesSchema? = imageList.value?.get(position)

                    message?.let {
                        val chatMessagesSchema = ChatMessageSchemaFactory.createForwardMessage(
                            message, selectedChatUsers.kryptId, selectedChatUsers.roomId,
                            selectedChatUsers.roomName, selectedChatUsers.roomImage
                        )

                        repository.sendMessage(
                            chatMessagesSchema,
                            isGroup,
                            selectedChatUsers.groupType
                        )

                    }


                }

            }

        }


    }


    fun sendDocument(position: Int) {

        Coroutine.iOWorker {


            var selectedChatUsers: ChatListSchema? = null

            if (isGroup) {
                selectedChatUsers = repository.getChatDetails(roomId)
            } else {
                selectedChatUsers = repository.getChatDetails(getRoomId(app, kryptCode))
            }

            selectedChatUsers?.let {

                val message: ChatMessagesSchema? = documentList.value?.get(position)

                message?.let {
                    val chatMessagesSchema = ChatMessageSchemaFactory.createForwardMessage(
                        message, selectedChatUsers.kryptId, selectedChatUsers.roomId,
                        selectedChatUsers.roomName, selectedChatUsers.roomImage
                    )

                    repository.sendMessage(
                        chatMessagesSchema,
                        isGroup,
                        selectedChatUsers.groupType
                    )

                }


            }

        }


    }

    fun getImageUrl(it: Int): String? {
        return imageList.value?.get(it)?.mediaUrl
    }

}