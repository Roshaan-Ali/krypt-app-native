package com.pyra.krpytapplication.viewmodel

import android.app.Application
import android.content.Context
import android.media.*
import android.net.Uri
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.app.hakeemUser.network.ApiInput
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.*
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.model.CommonResponseModel
import com.pyra.krpytapplication.repositories.implementations.AmazonRepository
import com.pyra.krpytapplication.repositories.implementations.ChatListRepository
import com.pyra.krpytapplication.repositories.implementations.ChatMessagesRepository
import com.pyra.krpytapplication.repositories.implementations.ProfileRepository
import com.pyra.krpytapplication.roomDb.ChatMessageSchemaFactory
import com.pyra.krpytapplication.roomDb.entity.ChatMessagesSchema
import com.pyra.network.UrlHelper
import getApiParams
import isUserOnline
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.ArrayList

class ChatMessagesViewModel(application: Application) : AndroidViewModel(application) {

    private var observer: Observer<PagedList<ChatMessagesSchema>>
    private var savedMessageObserver: Observer<PagedList<ChatMessagesSchema>>
    var chatMessagePagedList: PagedList<ChatMessagesSchema>? = null
    private lateinit var kryptId: String
    private lateinit var roomId: String
    lateinit var roomName: String
    private lateinit var roomImage: String
    private var groupType: String = ""
    private var isGroup: Boolean = false
    private val chatListRepository: ChatListRepository
    private val chatMessagesRepository: ChatMessagesRepository
    private var profileRepository: ProfileRepository
    val app: Application = application
    var chatMessages: List<ChatMessagesSchema> = ArrayList()
    var update: MutableLiveData<Void> = MutableLiveData()
    var presenceInfo: MutableLiveData<String> = MutableLiveData()
    var notifySelection: MutableLiveData<Void> = MutableLiveData()
    var messageReceiver: MutableLiveData<Void> = MutableLiveData()
    var onlineStatus: MutableLiveData<String> = MutableLiveData()

    var savedMessages = ArrayList<ChatMessagesSchema>()

    var selectedChatMessage: ArrayList<String> = ArrayList()
    var isMultiSelectedEnabled = false

    var deleteForEveryOne = false

    var amazonRepository: AmazonRepository = AmazonRepository.getInstance()
    var errorMessage: MutableLiveData<String> = MutableLiveData()
    var messageId = ""

    var uploadingFile: File? = null
    var isAllSavedMessage = false
    var isAllMessageForwadable = false

    var mediaPlayer: MediaPlayer? = null
    var audioCurrentPosition = 0
    var isMediaPlaying = false
    var playingMessageId = ""

    var isUserBlocked = MutableLiveData<Boolean>()

    var reply = MutableLiveData<ChatMessagesSchema>()
    var replerName = MutableLiveData<String>()

    var messageIdToDelete = 0

    init {
        chatListRepository = ChatListRepository.getInstance((app as MyApp).getAppDatabase())
        chatMessagesRepository = ChatMessagesRepository.getInstance((app).getAppDatabase())
        profileRepository = ProfileRepository.getInstance((app).getAppDatabase())
        observer = Observer {

            //            val list = it as List<ChatMessagesSchema>
            viewModelScope.launch {
                chatMessages = it as List<ChatMessagesSchema>
                chatMessagePagedList = it
                update.postValue(null)
            }

        }


        savedMessageObserver = Observer {

            //            val list = it as List<ChatMessagesSchema>

            viewModelScope.launch {
                chatMessages = it as List<ChatMessagesSchema>
                chatMessagePagedList = it
                update.postValue(null)
            }
        }

    }

    fun isSenderBubble(position: Int): Boolean {
        return chatMessages[position].isSender
    }

    fun sendMessage(
        content: String,
        messageType: MessageType
    ) {
        if (content.isEmpty()) return
        var messagesEntity = ChatMessagesSchema()
        when (messageType) {
            MessageType.TEXT -> {
                messagesEntity = ChatMessageSchemaFactory.createSenderTextMessage(
                    roomId,
                    kryptId,
                    roomName,
                    roomImage,
                    content,
                    reply.value,
                    SharedHelper(app).kryptKey, isGroup
                )

                if (!isGroup) {

                    val jsonObject = JSONObject()
                    jsonObject.put("fromUserName", SharedHelper(app).kryptKey.toLowerCase())
                    jsonObject.put("toUserName", kryptId.toLowerCase())

                    chatMessagesRepository.setApiMessage(
                        getApiParams(
                            app,
                            jsonObject,
                            UrlHelper.SENDMESSAGE
                        )
                    )
                }
            }
            MessageType.IMAGE -> TODO()
            MessageType.AUDIO -> TODO()
            MessageType.VIDEO -> TODO()
            MessageType.DOCUMENT -> TODO()
            MessageType.CONTACT -> TODO()
            MessageType.LOCATION -> TODO()
        }
        chatMessagesRepository.sendMessage(messagesEntity, isGroup, groupType)
        chatListRepository.updateLastMessage(messagesEntity)
        reply.value = ChatMessagesSchema()
        update.postValue(null)
    }

    fun getIsGroupChat(): Boolean {
        return isGroup
    }

    fun getChatMessages() {
        if (groupType == "PRIVATE") {
            chatMessagesRepository.getChatMessagesPrivate(roomId)?.observeForever(observer)
        } else {
            chatMessagesRepository.getChatMessages(roomId)?.observeForever(observer)
        }

    }

    fun unSubscribeMessageReceiver() {
        if (::roomId.isInitialized) {
            if (groupType == "PRIVATE") {
                chatMessagesRepository.getChatMessagesPrivate(roomId)?.removeObserver(observer)
            } else {
                chatMessagesRepository.getChatMessages(roomId)?.removeObserver(observer)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        unSubscribeMessageReceiver()
    }

    fun getPresence() {
        chatListRepository.getPresence(kryptId).observeForever { presence ->
            if (presence.isUserOnline()) {
                presenceInfo.postValue(app.getString(R.string.online))
            } else {
                chatListRepository.getLastActivity(kryptId).observeForever { lastSeen ->
                    presenceInfo.postValue(app.getString(R.string.last_seen_at) + lastSeen)
                }
            }
        }
    }

    fun resetUnreadCount() {
        chatMessagesRepository.resetUnreadCount(roomId)
    }

    fun getSavedMessages() {
        chatMessagesRepository.getSavedMessages()?.observeForever(observer)
    }

    fun getChatMessagesCount(): Int {
        return chatMessages.size
    }

    fun getChatMessageType(at: Int): MessageType {

        return try {
            if (chatMessages.isNotEmpty())
                chatMessages[at].messageType.toMessageType()
            else {
                MessageType.NONE
            }
        } catch (e: IndexOutOfBoundsException) {
            MessageType.NONE
        }
    }

    fun getChatMessageStatus(at: Int): MessageStatus {
        return chatMessages[at].messageStatus.toMessageStatus()
    }

    fun getTextChatMessage(at: Int): String {
        return chatMessages[at].message
    }

    fun getChatMessageTime(at: Int): String {
        return chatMessages[at].messageTime.longDateToDisplayTimeString()
    }

    fun isSender(at: Int): Boolean? {
        return try {
            chatMessages[at].isSender
        } catch (e: java.lang.IndexOutOfBoundsException) {
            null
        }

    }

    fun setRoomId(roomId: String) {

//        this.roomId = getRoomId(app.applicationContext, kryptId)
        this.roomId = roomId

        Coroutine.iOWorker {
            chatListRepository.getProfileData(roomId).let {
                this.roomImage = it?.roomImage ?: ""
                this.roomName = it?.roomName ?: ""
                this.isGroup = it?.chatType != "PRIVATE"
                this.kryptId = it?.kryptId ?: ""
                this.groupType = it?.groupType ?: ""

                getIsUserBlocked()
                getOnlineStatus(kryptId)
            }

            Coroutine.mainWorker {
                if (!isGroup) {
                    chatMessagesRepository.addUserToRoster(kryptId)
                    getPresence()
                }
                getChatMessages()
                notifySelection.value = null
            }

        }

    }

    private fun getIsUserBlocked() {

        if (kryptId == "") {
            isUserBlocked.value = false
            return
        }
        Coroutine.mainWorker {
            chatMessagesRepository.getIsUserBlocked(kryptId)?.observeForever {
                isUserBlocked.value = it != 0
            }
        }
    }

    fun saveCurrentChatUser() {
        SharedHelper(app.baseContext).currentChatingUser = roomId
    }

    fun removeCurrentChatUser() {
        SharedHelper(app.baseContext).currentChatingUser = ""
    }

    fun updateSeenStatus() {
        Coroutine.iOWorker {
            if (!isGroup) {
                val unreadMessages =
                    chatMessagesRepository.getUnreadMessages(roomId.toUpperCase(Locale.ROOT))
                if (unreadMessages != null) {
                    for (message in unreadMessages) {
                        chatMessagesRepository.updateSeenStatus(
                            message.messageId.toUpperCase(Locale.ROOT),
                            message.kryptId
                        )
                    }
                }
            }
        }

    }

    fun clearChat() {
        Coroutine.iOWorker {
            chatMessagesRepository.clearMessage(this.roomId)
            chatListRepository.clearMessage(this.roomId)
            Coroutine.mainWorker {
                reply.value = ChatMessagesSchema()
                unselectAll()
            }
        }
    }

    fun selectededPosition(position: Int) {
        makeSelectionUnselection(position)
        messageIdToDelete = position
    }

    fun singleClick(position: Int) {
        if (isMultiSelectedEnabled) {
            makeSelectionUnselection(position)
        }
    }

    fun makeSelectionUnselection(position: Int) {
        if (selectedChatMessage.contains(chatMessages[position].messageId.toUpperCase(Locale.ROOT))) {
            selectedChatMessage.remove(chatMessages[position].messageId.toUpperCase(Locale.ROOT))

        } else {
            selectedChatMessage.add(chatMessages[position].messageId.toUpperCase(Locale.ROOT))
        }

        isMultiSelectedEnabled = selectedChatMessage.size != 0
        getIsAllSelected()
        getIsForwardable()
        getIsDeleteForEveryOne()
    }

    private fun getIsDeleteForEveryOne() {

        Coroutine.iOWorker {
            var list = chatMessagesRepository.getAllMessages(selectedChatMessage)

            list?.let {
                deleteForEveryOne = true
                for (i in list.indices) {
                    if (list[i].isSender) {
                        deleteForEveryOne = true
                    } else {
                        deleteForEveryOne = false
                        break
                    }
                }
            }
        }

    }

    private fun getIsForwardable() {
        Coroutine.iOWorker {
            var list = chatMessagesRepository.getIsForwardable(selectedChatMessage)
            list?.let {
                isAllMessageForwadable = true
                for (i in list.indices) {
                    if (list[i].messageType != MessageType.TEXT.toMessageString()) {
                        if (list[i].isSender && !list[i].isUploaded) {
                            isAllMessageForwadable = false
                            break
                        } else if (!list[i].isSender && list[i].localMediaPath == "") {
                            isAllMessageForwadable = false
                            break
                        }
                    }
                }

                Coroutine.mainWorker {
                    notifySelection.value = null
                }
            }
        }
    }

    fun isSelected(position: Int): Boolean {
        return selectedChatMessage.contains(chatMessages[position].messageId.toUpperCase(Locale.ROOT))
    }

    fun getIsAllSelected() {
        Coroutine.iOWorker {
            isAllSavedMessage = chatMessagesRepository.getIsAllSaved(selectedChatMessage)
            Coroutine.mainWorker {
                notifySelection.value = null
            }
        }
    }

    fun onSaveClicked() {

        Coroutine.iOWorker {
            chatMessagesRepository.saveMessage(selectedChatMessage, isAllSavedMessage)
            Coroutine.mainWorker {
                unselectAll()
            }
        }

    }

    fun onDeleteClicked() {

        Coroutine.iOWorker {
            chatMessagesRepository.deleteMessage(selectedChatMessage)
            Coroutine.mainWorker {
                unselectAll()
            }
        }
    }

    fun unselectAll() {
        selectedChatMessage = ArrayList()
        isMultiSelectedEnabled = false
        notifySelection.value = null
    }

    fun getSenderName(position: Int): String? {

        return if (chatMessages[position].isSender) {
            app.getString(R.string.you)
        } else {

            if (chatMessages[position].userName == null || chatMessages[position].userName == "") {
                chatMessages[position].kryptId.bareUsername()
            } else {
                chatMessages[position].userName
            }
        }
    }

    fun getReceiverName(position: Int): String? {

        return if (chatMessages[position].isSender) {
            if (chatMessages[position].userName == "") {
                chatMessages[position].kryptId.bareUsername()
            } else {
                chatMessages[position].userName
            }
        } else {
            app.getString(R.string.you)
        }
    }

    fun getSenderProfilePic(position: Int): String {
        return chatMessages[position].userImage.toString()
    }

    fun sendTypingStatus(typing: Boolean) {
        if (!isGroup)
            chatMessagesRepository.sendTypingStatus(kryptId, typing)
    }

    fun uploadToLocal(file: File) {

        uploadingFile = file

        var chatMessagesEntity = ChatMessagesSchema()
        if (uploadingFile?.getFileType() == MediaType.IMAGE.value) {
            chatMessagesEntity = ChatMessageSchemaFactory.createLocalImageMesssage(
                roomId,
                kryptId,
                roomName,
                roomImage,
                app.getString(R.string.send_a_image),
                "",
                uploadingFile!!.absolutePath,
                reply.value,
                SharedHelper(app).kryptKey, isGroup
            )
        } else if (uploadingFile?.getFileType() == MediaType.VIDEO.value) {

            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(app.baseContext, Uri.fromFile(file))
            val time =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) ?: "0"
            val seconds = time.toLong() / 1000
            retriever.release()

            chatMessagesEntity = ChatMessageSchemaFactory.createLocalVideoMesssage(
                roomId,
                kryptId,
                roomName,
                roomImage,
                app.getString(R.string.send_a_video),
                "",
                uploadingFile!!.absolutePath,
                seconds.toString(), reply.value,
                SharedHelper(app).kryptKey, isGroup
            )
        }

        messageId = chatMessagesRepository.insertLocalMedia(chatMessagesEntity)
        chatListRepository.updateLastMessage(chatMessagesEntity)
        reply.value = ChatMessagesSchema()
        uploadImageFullyCompressed(file, file.getFileType())
    }

    fun uploadImageFullyCompressed(
        file: File,
        fileType: Int
    ) {

        val compressedFile = ImageUtil.getThumbnail(app.baseContext, file, 5, fileType)

        compressedFile?.let {
            amazonRepository.uploadToAWS(app.baseContext, compressedFile).observeForever {
                it.error?.let { error ->
                    if (!error) {
                        it.message?.let { url -> insertThumbImage(url) }
                    } else {
                        it.message?.let { msg ->
                            errorMessage.value = msg
                            chatMessagesRepository.uploadCancelledByUser(messageId)
                        }

                    }
                }
            }
        }

    }

    fun insertThumbImage(thumbImage: String) {
        app.baseContext.saveToGlideCache(thumbImage)
        chatMessagesRepository.updateThumbImage(thumbImage, messageId)

        if (uploadingFile!!.getFileType() == MediaType.IMAGE.value)
            uploadMedia(uploadingFile!!)
        else if (uploadingFile!!.getFileType() == MediaType.VIDEO.value)
            compressVideo(uploadingFile!!)
    }

    private fun compressVideo(file: File) {

        val newFileName =
            "VID_" + "_" + System.currentTimeMillis().toString() + ".mp4"

        val newFile =
            File(app.baseContext.getExternalFilesDir(null)!!.absolutePath, "/$newFileName")
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

                        Coroutine.iOWorker {

                            var isCancelled = chatMessagesRepository.isUserCanceledUpload(messageId)
                            isCancelled?.let {

                                if (!isCancelled) {
                                    Coroutine.mainWorker {
                                        uploadMedia(newFile)
                                    }
                                }
                            }
                        }

                    }

                    override fun onFailure() {
                        chatMessagesRepository.uploadCancelledByUser(messageId)
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

                uploadMedia(newFile)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    fun uploadMedia(
        file: File
    ) {
        amazonRepository.uploadToAWS(app.baseContext, file).observeForever {
            it.error?.let { error ->
                if (!error) {
                    it.message?.let { url ->
                        if (file.getFileType() == MediaType.IMAGE.value) {
                            updateSendImageMessage(url)
                        } else if (file.getFileType() == MediaType.VIDEO.value) {
                            updateSendVideoMessage(url)
                        }

                    }
                } else {
                    it.message?.let { msg ->
                        errorMessage.value = msg
                        chatMessagesRepository.uploadCancelledByUser(messageId)
                    }

                }
            }
        }
    }

    fun updateSendImageMessage(imageUrl: String) {

        if (messageId != "") {
            chatMessagesRepository.updateSendImageMessage(
                messageId.toUpperCase(Locale.ROOT),
                imageUrl, isGroup, groupType
            )
        }
    }

    fun updateSendVideoMessage(imageUrl: String) {

        if (messageId != "") {
            chatMessagesRepository.updateSendVideoMessage(
                messageId.toUpperCase(Locale.ROOT),
                imageUrl, isGroup, groupType
            )
        }
    }

    fun isUploaded(position: Int): Boolean {
        return chatMessages[position].isUploaded
    }

    fun getSenderImage(position: Int): String {
        return if (chatMessages[position].isUploaded) {
            chatMessages[position].mediaUrl
        } else {
            chatMessages[position].localMediaPath
        }
    }

    fun getThumbNail(position: Int): String {
        return chatMessages[position].mediaThumbUrl
    }

    fun getReciverImageUrl(position: Int): String {
        return if (isFileExist(position))
            chatMessages[position].localMediaPath
        else
            chatMessages[position].mediaThumbUrl
    }

    fun getReciverThumbUrl(position: Int): String? {
        return chatMessages[position].mediaThumbUrl
    }

    fun uploadCancelledByUser(position: Int) {
        chatMessagesRepository.uploadCancelledByUser(chatMessages[position].messageId)
        amazonRepository.stopUpload()

    }

    fun isUploadCancelledByUser(position: Int): Boolean {
        return chatMessages[position].isMediaCancelledByUser
    }

    fun startUpload(position: Int) {

        messageId = chatMessages[position].messageId
        chatMessagesRepository.uploadstartedByUser(messageId)
        uploadingFile = File(chatMessages[position].localMediaPath)

        uploadingFile?.let {
            if (chatMessages[position].mediaThumbUrl == "") {
                uploadImageFullyCompressed(it, it.getFileType())
            } else {
                if (it.getFileType() == MediaType.VIDEO.value) {
                    compressVideo(it)
                } else {
                    uploadMedia(it)
                }

            }
        }
    }

    fun isMediaDownload(position: Int): Boolean {
        return if (chatMessages[position].localMediaPath == "") {
            false
        } else {
            val file = File(chatMessages[position].localMediaPath)
            file.exists()
        }
    }

    fun isDownloadCancelByUser(position: Int): Boolean {
        return chatMessages[position].isMediaCancelledByUser
    }

    fun downloadMedia(position: Int) {

        messageId = chatMessages[position].messageId
        amazonRepository.downloadFile(app.baseContext, chatMessages[position].mediaUrl)
            ?.observeForever {
                it.error?.let { error ->
                    if (error) {
                        it.message?.let { msg ->
                            errorMessage.value = msg
                            chatMessagesRepository.downloadCanceledByUser(messageId)
                        }
                    } else {
                        it.file?.let { file ->
                            setFileDownloded(file)
                        }
                    }
                }
            }
        chatMessagesRepository.downloadStartByUser(messageId)
    }

    private fun setFileDownloded(file: File) {
        chatMessagesRepository.setMediaDownloded(messageId, file.absolutePath)
    }

    fun downloadCanceledByUser(position: Int) {
        chatMessagesRepository.downloadCanceledByUser(chatMessages[position].messageId)
        amazonRepository.cancelDownload()

    }

    fun getSenderVideo(position: Int): String? {
        return if (chatMessages[position].isUploaded) {
            chatMessages[position].localMediaPath
        } else {
            chatMessages[position].mediaUrl
        }
    }

    fun getReciverVideo(position: Int): String? {
        return if (chatMessages[position].localMediaPath == "") {
            chatMessages[position].mediaThumbUrl
        } else {
            if (File(chatMessages[position].localMediaPath).exists()) {
                chatMessages[position].localMediaPath
            } else {
                chatMessages[position].mediaThumbUrl
            }
        }
    }

    fun getLocalFilePath(position: Int): String {
        return chatMessages[position].localMediaPath
    }

    fun getLocalFile(position: Int): File? {
        return if (File(chatMessages[position].localMediaPath).exists()) {
            File(chatMessages[position].localMediaPath)
        } else null
    }

    fun uploadDocument(file: File) {
        uploadingFile = file

        val fileType = file.getDocumentType()
        val filename = file.name

        val chatMessagesEntity = ChatMessageSchemaFactory.createLocalDocumentMesssage(
            roomId,
            kryptId,
            roomName,
            roomImage,
            app.getString(R.string.send_a_file),
            uploadingFile!!.absolutePath,
            fileType,
            filename,
            reply.value,
            SharedHelper(app).kryptKey, isGroup
        )

        messageId = chatMessagesRepository.insertLocalMedia(chatMessagesEntity)
        chatListRepository.updateLastMessage(chatMessagesEntity)
        reply.value = ChatMessagesSchema()

        uploadToAws(file)
    }

    fun uploadAudio(file: File) {
        uploadingFile = file
        val filename = file.name

        LogUtil.e("ChatMessagesView", "File: ${file.absolutePath}")

        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(app.baseContext, Uri.fromFile(file))

            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val seconds = time.toLong() / 1000
            retriever.release()

            val chatMessagesEntity = ChatMessageSchemaFactory.createLocalAudioMesssage(
                roomId,
                kryptId,
                roomName,
                roomImage,
                app.getString(R.string.send_a_audio),
                uploadingFile!!.absolutePath,
                seconds.toString(),
                filename,
                reply.value,
                SharedHelper(app).kryptKey, isGroup
            )

            messageId = chatMessagesRepository.insertLocalMedia(chatMessagesEntity)
            chatListRepository.updateLastMessage(chatMessagesEntity)
            reply.value = ChatMessagesSchema()

            uploadAudioToAws(file)
        } catch (e: RuntimeException) {
            LogUtil.e("ChatMessagesView", "uploadAudio: ${e.message}")
        }
    }

    private fun uploadAudioToAws(file: File) {

        amazonRepository.uploadToAWS(app.baseContext, file).observeForever {
            it.error?.let { error ->
                if (!error) {
                    it.message?.let { url ->
                        chatMessagesRepository.updateSendAudioMessage(
                            messageId,
                            url,
                            isGroup,
                            groupType
                        )
                    }
                } else {
                    it.message?.let { msg ->
                        errorMessage.value = msg
                        chatMessagesRepository.uploadCancelledByUser(messageId)
                    }

                }
            }
        }

    }

    private fun uploadToAws(file: File) {

        amazonRepository.uploadToAWS(app.baseContext, file).observeForever {
            it.error?.let { error ->
                if (!error) {
                    it.message?.let { url ->
                        chatMessagesRepository.updateSendDocumentMessage(
                            messageId,
                            url,
                            isGroup,
                            groupType
                        )
                    }
                } else {
                    it.message?.let { msg ->
                        errorMessage.value = msg
                        chatMessagesRepository.uploadCancelledByUser(messageId)
                    }

                }
            }
        }

    }

    fun getDocumentName(position: Int): String {
        return chatMessages[position].mediaDocumentName
    }

    fun startUploadDocument(position: Int) {

        messageId = chatMessages[position].messageId
        chatMessagesRepository.uploadstartedByUser(messageId)
        uploadingFile = File(chatMessages[position].localMediaPath)

        uploadingFile?.let {
            uploadToAws(it)
        }
    }

    fun startUploadAudio(position: Int) {

        messageId = chatMessages[position].messageId
        chatMessagesRepository.uploadstartedByUser(messageId)
        uploadingFile = File(chatMessages[position].localMediaPath)

        uploadingFile?.let {
            uploadAudioToAws(it)
        }
    }

    fun downloadDocument(position: Int) {

        messageId = chatMessages[position].messageId
        amazonRepository.downloadDocumentFile(
            app.baseContext,
            chatMessages[position].mediaUrl,
            chatMessages[position].mediaDocumentName
        )
            ?.observeForever {
                it.error?.let { error ->
                    if (error) {
                        it.message?.let { msg ->
                            errorMessage.value = msg
                            chatMessagesRepository.downloadCanceledByUser(messageId)
                        }
                    } else {
                        it.file?.let { file ->
                            setFileDownloded(file)
                        }
                    }
                }
            }
        chatMessagesRepository.downloadStartByUser(messageId)
    }

    fun downloadAudio(position: Int) {

        messageId = chatMessages[position].messageId
        amazonRepository.downloadAudioFile(
            app.baseContext,
            chatMessages[position].mediaUrl,
            chatMessages[position].mediaDocumentName
        )
            ?.observeForever {
                it.error?.let { error ->
                    if (error) {
                        it.message?.let { msg ->
                            errorMessage.value = msg
                            chatMessagesRepository.downloadCanceledByUser(messageId)
                        }
                    } else {
                        it.file?.let { file ->
                            setFileDownloded(file)
                        }
                    }
                }
            }
        chatMessagesRepository.downloadStartByUser(messageId)
    }

    fun getSenderFile(position: Int): File? {

        File(chatMessages[position].localMediaPath).let {
            if (it.exists()) {
                return it
            }
        }
        return null
    }

    fun getReciverFile(position: Int): File? {

        return if (chatMessages[position].mediaUrl != "") {
            File(chatMessages[position].mediaUrl)
        } else
            null
    }

    fun isFileExist(position: Int): Boolean {
        return if (chatMessages[position].localMediaPath != "") {
            File(chatMessages[position].localMediaPath).exists()
        } else
            false
    }

    fun playAudio(position: Int) {

        if (chatMessages[position].localMediaPath != "")
            if (playingMessageId == "") {
                startNewPlayer(position)
            } else {
                if (playingMessageId == chatMessages[position].messageId) {
                    mediaPlayer?.seekTo(audioCurrentPosition)
                    mediaPlayer?.start()
                    isMediaPlaying = true
                } else {
                    startNewPlayer(position)
                }
            }

        notifySelection.value = null
    }

    private fun startNewPlayer(position: Int) {

        try {

            LogUtil.d(
                "ChatMessagesView",
                "Audio file path: ${chatMessages[position].localMediaPath}"
            )

            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    it.release()
                }
            }

            playingMessageId = chatMessages[position].messageId
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .build()
            )
            mediaPlayer?.setDataSource(chatMessages[position].localMediaPath)

            val sharedPref = SharedHelper(app)

            if (sharedPref.isMorphVoiceEnabled) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    mediaPlayer?.playbackParams = PlaybackParams().apply {
                        if (sharedPref.morphVoiceFrequency != "") {
                            pitch = sharedPref.morphVoiceFrequency.toFloat()
                        }
                    }
                }
            }

            mediaPlayer?.prepare()
            mediaPlayer?.start()
            isMediaPlaying = true

            mediaPlayer?.setOnCompletionListener {
                //pauseAudio()
                playingMessageId = ""
                audioCurrentPosition = 0
                isMediaPlaying = false
            }
        } catch (e: Exception) {
            LogUtil.e("ChatMessagesViewModel", "Error: ${e.message}")
        }
    }

    fun pauseAudio() {

        isMediaPlaying = false
        mediaPlayer?.let {
            audioCurrentPosition = it.currentPosition
            it.pause()
        }
        notifySelection.value = null
    }

    fun getIsAudioPlaying(position: Int): Boolean {
        return playingMessageId == chatMessages[position].messageId && isMediaPlaying
    }

    fun getDateDisplay(position: Int, itemCount: Int): String {

        try {
            if (chatMessages.isNotEmpty()) {

                if (chatMessages.size > position)
                    if (position == itemCount - 1) {
                        return chatMessages[position].messageTime.getDisplayTime()
                    } else {

                        chatMessages[position].messageTime.getDisplayTime().let {
                            return if (chatMessages[position + 1].messageTime.getDisplayTime() == it) {
                                ""
                            } else {
                                it
                            }
                        }

                    }

                return ""
            } else {
                return ""
            }

        } catch (e: IndexOutOfBoundsException) {
            return ""
        }

    }

    fun unsaveSelected() {
        Coroutine.iOWorker {
            chatMessagesRepository.saveMessage(selectedChatMessage, true)
            Coroutine.mainWorker {
                unselectAll()
            }
        }
    }

    fun getProfileImage(): String? {
        return roomImage
    }

    fun setReply(it: Int) {
        Coroutine.iOWorker {
            val message = chatMessagesRepository.getReplyMessage(chatMessages[it].messageId)
            Coroutine.mainWorker { message?.let { reply.value = it } }
        }
    }

    fun getName(it: ChatMessagesSchema?) {
        if (it?.isSender!!) {
            replerName.value = "You"
        } else {
            Coroutine.iOWorker {
                var chatData =
                    chatListRepository.getRoomNameByKryptId(it.kryptId.toString().toUpperCase())
                if (chatData?.toUpperCase() == "" || chatData == null) {
                    Coroutine.mainWorker { replerName.value = it.kryptId.bareUsername() }
                } else {
                    Coroutine.mainWorker { replerName.value = chatData.toString() }
                }
            }
        }
    }

    fun getMessage(it: ChatMessagesSchema?): CharSequence? {
        when {
            it?.messageType.toString().equals(MessageType.TEXT.toString(), ignoreCase = true) -> {
                return it?.message
            }
            it?.messageType.toString().equals(MessageType.IMAGE.toString(), ignoreCase = true) -> {
                return app.getString(R.string.send_a_image)
            }
            it?.messageType.toString().equals(MessageType.VIDEO.toString(), ignoreCase = true) -> {
                return app.getString(R.string.send_a_video)
            }
            it?.messageType.toString().equals(MessageType.AUDIO.toString(), ignoreCase = true) -> {
                return app.getString(R.string.send_a_audio)
            }
            it?.messageType.toString()
                .equals(MessageType.DOCUMENT.toString(), ignoreCase = true) -> {
                return app.getString(R.string.send_a_file)
            }
            else -> {
                return it?.message
            }
        }
    }

    fun getReplySendName(position: Int): String? {

        if (chatMessages[position].replyedKryptId.toUpperCase() == SharedHelper(app).kryptKey.toUpperCase()) {
            return "You"
        }
        return if (chatMessages[position].replyerName == null || chatMessages[position].replyerName == "null" || chatMessages[position].replyerName == "") {
            chatMessages[position].replyedKryptId.bareUsername()
        } else {
            chatMessages[position].replyerName
        }

    }

    fun getReplyMessage(position: Int): String? {

        when (chatMessages[position].replyedMessageType.toLowerCase()) {
            MessageType.TEXT.toString()
                .toLowerCase() -> {
                return chatMessages[position].replyedMessage
            }
            MessageType.IMAGE.toString()
                .toLowerCase() -> {
                return app.getString(R.string.send_a_image)
            }

            MessageType.VIDEO.toString()
                .toLowerCase() -> {
                return app.getString(R.string.send_a_video)
            }

            MessageType.DOCUMENT.toString()
                .toLowerCase() -> {
                return app.getString(R.string.send_a_file)
            }

            MessageType.AUDIO.toString()
                .toLowerCase() -> {
                return app.getString(R.string.send_a_audio)
            }

        }

        return ""
    }

    fun getReplyUrl(position: Int): String? {
        return chatMessages[position].replyedMessage
    }


    fun getIsReply(position: Int): Boolean {
        return chatMessages[position].isReply
    }


    fun isImageOrVideoMessage(position: Int): Boolean {

        return when (chatMessages[position].replyedMessageType.toLowerCase()) {
            MessageType.IMAGE.toString()
                .toLowerCase() -> {
                true
            }
            MessageType.VIDEO.toString()
                .toLowerCase() -> {
                true
            }
            else -> {
                false
            }
        }
    }

    fun deleteForEveryOne() {

        Coroutine.iOWorker {
            chatMessagesRepository.deleteMessage(selectedChatMessage)
            chatMessagesRepository.sendDeleteMessage(selectedChatMessage, isGroup, kryptId, roomId)

            Coroutine.mainWorker {
                unselectAll()
            }

        }

    }

    fun createGroupCall(
        context: Context,
        callType: String,
        selectedIds: ArrayList<String>,
        channelName: String,
        roomId: String
    ): MutableLiveData<CommonResponseModel> {

        val array = JSONArray()

        for (i in selectedIds.indices) {

            var objects = JSONObject()
            objects.put("name", selectedIds[i])

            array.put(objects)
        }

        val jsonObject = JSONObject()
        jsonObject.put("fromUserName", SharedHelper(context).kryptKey)
        jsonObject.put("toUserName", array.toString())
        jsonObject.put("callType", callType)
        jsonObject.put("channelName", channelName)
        jsonObject.put("groupId", roomId)


        return chatMessagesRepository.createGroupCall(
            getApiParams(
                app.baseContext!!,
                jsonObject,
                UrlHelper.CREATEGROUPCALL
            )
        )

    }

    fun getOnlineStatus(kryptCode: String) {

        Coroutine.mainWorker {
            chatListRepository.getOnlineStatus(kryptCode)?.observeForever {

                if (it.isOnline == 1) {
                    onlineStatus.value = app.getString(R.string.online)
                } else {
                    if (it.lastSeen == "") {
                        onlineStatus.value = ""
                    } else {
                        onlineStatus.value = "Last Seen At ${
                            getFormatedDate(
                                it.lastSeen,
                                "yyyy-MM-dd'T'HH:mm:ss",
                                "hh:mm a"
                            )
                        }"
                    }
                }
            }
        }

        val apiInputs = ApiInput()
        apiInputs.context = app.applicationContext
        apiInputs.jsonObject = JSONObject().apply {
            put("userName", kryptCode)
        }
        apiInputs.url = UrlHelper.GETUSERDETAILS


        Coroutine.mainWorker {
            profileRepository.getUserDetails(apiInputs).observeForever {
                if (it.error == "false" && it.data.isNotEmpty()) {
                    updateStatus(kryptCode, it.data[0].isOnline, it.data[0].lastLoginTime!!)
                }
            }

        }

    }

    fun updateStatus(kryptCode: String, i: Int, lastSeen: String) {
        Coroutine.iOWorker {
            chatListRepository.updateStatus(kryptCode.toUpperCase(), i, lastSeen)
        }
    }


    fun addImageToLocal(messagesEntity: ChatMessagesSchema) {
        Coroutine.iOWorker {
            chatMessagesRepository.insertMessage(messagesEntity)
        }
    }

}