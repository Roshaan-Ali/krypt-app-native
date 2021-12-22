package com.pyra.krpytapplication.repositories.implementations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.app.hakeemUser.network.ApiInput
import com.google.gson.Gson
import com.pyra.krpytapplication.utils.Coroutine
import com.pyra.krpytapplication.utils.MessageStatus
import com.pyra.krpytapplication.utils.toMessageString
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.chat.XMPPOperations
import com.pyra.krpytapplication.model.CommonResponseModel
import com.pyra.krpytapplication.repositories.interfaces.ApiResponseCallback
import com.pyra.krpytapplication.roomDb.AppDataBase
import com.pyra.krpytapplication.roomDb.ChatMessageSchemaFactory
import com.pyra.krpytapplication.roomDb.dao.BlockListDao
import com.pyra.krpytapplication.roomDb.dao.BurnMessageDao
import com.pyra.krpytapplication.roomDb.dao.ChatListDao
import com.pyra.krpytapplication.roomDb.dao.ChatMessagesDao
import com.pyra.krpytapplication.roomDb.entity.BurnMessageSchema
import com.pyra.krpytapplication.roomDb.entity.ChatMessagesSchema
import com.pyra.network.Api
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import java.util.*

class ChatMessagesRepository private constructor(appDataBase: AppDataBase?) {

    init {
        chatMessagesDao = appDataBase?.chatMessagesDao()
        chatListDao = appDataBase?.chatListDao()
        chatListDao = appDataBase?.chatListDao()
        blockListDao = appDataBase?.blockListDao()
        burnMessageDao = appDataBase?.burnMessageDao()
    }

    companion object {

        private var chatMessagesDao: ChatMessagesDao? = null
        private var chatListDao: ChatListDao? = null
        private var blockListDao: BlockListDao? = null
        private var burnMessageDao: BurnMessageDao? = null
        private var messagesRepository: ChatMessagesRepository? = null

        fun getInstance(appDataBase: AppDataBase?): ChatMessagesRepository {
            if (messagesRepository == null) {
                messagesRepository = ChatMessagesRepository(appDataBase)
            }
            return messagesRepository as ChatMessagesRepository
        }
    }

    fun insertMessage(entity: ChatMessagesSchema) {
        chatMessagesDao?.insertChatMessage(entity)
    }

    fun updateMessage(messageId: String, message: String) {
        chatMessagesDao?.updateChatMessage(messageId, message)
    }

    fun resetUnreadCount(roomId: String) {
        doAsync {
            chatListDao?.resetUnreadMessageCount(roomId.toUpperCase(Locale.ROOT))
        }
    }

    fun insertOrReplace(entity: ChatMessagesSchema) {
        chatMessagesDao?.insertOrReplaceChatMessage(entity)
    }

    fun getChatMessages(roomId: String): LiveData<PagedList<ChatMessagesSchema>>? {

        // val factory: DataSource.Factory<Int, ChatMessagesSchema>? =
        return chatMessagesDao?.getChatMessages(roomId.toUpperCase(Locale.ROOT))?.toLiveData(20, 1)

//        val loadConfig = PagedList.Config.Builder()
//            .setEnablePlaceholders(false)
//            .setPrefetchDistance(10)
//            .setPageSize(10).build()
//        return if (factory == null) {
//            null
//        } else {
//            LivePagedListBuilder<Int, ChatMessagesSchema>(factory, loadConfig).build()
//        }

    }

    fun getChatMessagesPrivate(roomId: String): LiveData<PagedList<ChatMessagesSchema>>? {

        val factory: DataSource.Factory<Int, ChatMessagesSchema>? =
            chatMessagesDao?.getChatMessagesPrivate(roomId.toUpperCase(Locale.ROOT))


        val loadConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(30)
            .setPageSize(50).build()
        return if (factory == null) {
            null
        } else {
            LivePagedListBuilder<Int, ChatMessagesSchema>(factory, loadConfig).build()
        }

    }

    fun sendMessage(messagesEntity: ChatMessagesSchema, isGroup: Boolean, groupType: String) {
        doAsync {
            insertOrReplace(messagesEntity)
            if (isGroup) {
                MyApp.xmppOperations.sendGroupMessage(
                    messagesEntity.roomId,
                    messagesEntity,
                    groupType
                )
            } else {
                MyApp.xmppOperations.sendMessage(messagesEntity.kryptId, messagesEntity)
            }
        }
    }

    fun deleteMessage(messagesEntity: ChatMessagesSchema) {
        MyApp.xmppOperations.sendMessage(messagesEntity.kryptId, messagesEntity)
    }

    fun addUserToRoster(kryptId: String) {
        doAsync {
            MyApp.xmppOperations.addUserToRoster(kryptId)
        }
    }

    suspend fun clearMessage(roomId: String) {

//        chatMessagesDao?.clearMessage(roomId.toUpperCase(Locale.ROOT))
        chatMessagesDao?.updateAsDeleteRoomMessage(roomId.toUpperCase(Locale.ROOT))
    }

    suspend fun saveMessage(selectedChatMessage: ArrayList<String>, isSave: Boolean) {
        chatMessagesDao?.saveMessage(selectedChatMessage as List<String>, !isSave)
    }

    suspend fun deleteMessage(selectedChatMessage: ArrayList<String>) {
//        chatMessagesDao?.deleteMessage(selectedChatMessage as List<String>)
        chatMessagesDao?.updateMessageAsDeleted(selectedChatMessage as List<String>)

    }

    suspend fun getIsAllSaved(selectedChatMessage: ArrayList<String>): Boolean {
        val count = chatMessagesDao?.getIsAllSaved(selectedChatMessage as List<String>)
        return count == 0
    }

    fun getSavedMessages(): LiveData<PagedList<ChatMessagesSchema>>? {

        val factory: DataSource.Factory<Int, ChatMessagesSchema>? =
            chatMessagesDao?.getSavedMessages()


        val loadConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(30)
            .setPageSize(50).build()
        return if (factory == null) {
            null
        } else {
            LivePagedListBuilder<Int, ChatMessagesSchema>(factory, loadConfig).build()
        }


//        return chatMessagesDao?.getSavedMessages()
    }

    fun sendTypingStatus(kryptId: String, typing: Boolean) {
        MyApp.xmppOperations.updateTypingStatus(kryptId, typing)
    }

    fun insertLocalMedia(chatMessagesEntity: ChatMessagesSchema): String {

        Coroutine.iOWorker {
            chatMessagesDao?.insertLocalMedia(chatMessagesEntity)
        }
        return chatMessagesEntity.messageId
    }

    fun updateSendImageMessage(
        messageId: String,
        imageUrl: String?,
        isGroup: Boolean,
        groupType: String
    ) {
        doAsync {
            chatMessagesDao?.updateSendMessage(messageId.toUpperCase(Locale.ROOT), imageUrl)
            val value: ChatMessagesSchema? =
                chatMessagesDao?.getChatDetails(messageId.toUpperCase(Locale.ROOT))

            value?.let {
                val createSenderImage = ChatMessageSchemaFactory.createSenderImageMesssage(value)
                if (isGroup) {
                    MyApp.xmppOperations.sendGroupMessage(
                        createSenderImage.roomId,
                        createSenderImage,
                        groupType
                    )
                } else {
                    MyApp.xmppOperations.sendMessage(createSenderImage.kryptId, createSenderImage)
                }

            }
        }
    }

    fun updateSendVideoMessage(
        messageId: String, imageUrl: String?, isGroup: Boolean,
        groupType: String
    ) {
        doAsync {
            chatMessagesDao?.updateSendMessage(messageId.toUpperCase(Locale.ROOT), imageUrl)
            val value: ChatMessagesSchema? =
                chatMessagesDao?.getChatDetails(messageId.toUpperCase(Locale.ROOT))

            value?.let {
                val createSenderVideo = ChatMessageSchemaFactory.createSenderVideoMesssage(value)
                if (isGroup) {
                    MyApp.xmppOperations.sendGroupMessage(
                        createSenderVideo.roomId,
                        createSenderVideo,
                        groupType
                    )
                } else {
                    MyApp.xmppOperations.sendMessage(createSenderVideo.kryptId, createSenderVideo)
                }

            }
        }
    }

    fun updateSendDocumentMessage(
        messageId: String,
        imageUrl: String?,
        isGroup: Boolean,
        groupType: String
    ) {
        doAsync {
            chatMessagesDao?.updateSendMessage(messageId.toUpperCase(Locale.ROOT), imageUrl)
            val value: ChatMessagesSchema? =
                chatMessagesDao?.getChatDetails(messageId.toUpperCase(Locale.ROOT))

            value?.let {
                val createSenderDocument =
                    ChatMessageSchemaFactory.createSenderDocumentMesssage(value)
                if (isGroup) {
                    MyApp.xmppOperations.sendGroupMessage(
                        createSenderDocument.roomId,
                        createSenderDocument,
                        groupType
                    )
                } else {
                    MyApp.xmppOperations.sendMessage(
                        createSenderDocument.kryptId,
                        createSenderDocument
                    )
                }

            }
        }
    }

    fun updateSendAudioMessage(
        messageId: String,
        imageUrl: String?,
        isGroup: Boolean,
        groupType: String
    ) {
        doAsync {
            chatMessagesDao?.updateSendMessage(messageId.toUpperCase(Locale.ROOT), imageUrl)
            val value: ChatMessagesSchema? =
                chatMessagesDao?.getChatDetails(messageId.toUpperCase(Locale.ROOT))

            value?.let {
                val createSenderAudio = ChatMessageSchemaFactory.createSenderAudioMesssage(value)
                if (isGroup) {
                    MyApp.xmppOperations.sendGroupMessage(
                        createSenderAudio.roomId,
                        createSenderAudio,
                        groupType
                    )
                } else {
                    MyApp.xmppOperations.sendMessage(createSenderAudio.kryptId, createSenderAudio)
                }

            }
        }
    }

    fun updateMessageStatus(messageId: String, messageStatus: String) {
        chatMessagesDao?.updateMessageStatus(messageId, messageStatus)
    }

    fun updateSeenStatus(messageId: String, kryptId: String) {
        Coroutine.iOWorker {
            MyApp.xmppOperations.updateSeenStatus(messageId, kryptId)
            chatMessagesDao?.updateMessageStatus(
                messageId,
                MessageStatus.READ.toMessageString().toUpperCase(Locale.ROOT)
            )
        }
    }

    suspend fun getUnreadMessages(roomId: String): List<ChatMessagesSchema>? {
        return chatMessagesDao?.getUnreadMessages(roomId)
    }

    fun updateThumbImage(thumbImage: String, messageId: String) {
        Coroutine.iOWorker {
            chatMessagesDao?.updateThumbImage(thumbImage, messageId.toUpperCase(Locale.ROOT))
        }
    }

    fun uploadCancelledByUser(messageId: String) {
        Coroutine.iOWorker {
            chatMessagesDao?.uploadCancelledByUser(messageId.toUpperCase(Locale.ROOT))
        }
    }

    fun uploadstartedByUser(messageId: String) {
        Coroutine.iOWorker {
            chatMessagesDao?.uploadStartedByUser(messageId.toUpperCase(Locale.ROOT))
        }
    }

    fun setMediaDownloded(messageId: String, downlodedFilePath: String) {
        Coroutine.iOWorker {
            chatMessagesDao?.setMediaDownloded(
                messageId.toUpperCase(Locale.ROOT),
                downlodedFilePath
            )
        }
    }

    fun downloadStartByUser(messageId: String) {
        Coroutine.iOWorker {
            chatMessagesDao?.downloadStartByUser(messageId.toUpperCase(Locale.ROOT))
        }

    }

    fun downloadCanceledByUser(messageId: String) {
        Coroutine.iOWorker {
            chatMessagesDao?.downloadCanceledByUser(messageId.toUpperCase(Locale.ROOT))
        }
    }

    suspend fun isUserCanceledUpload(messageId: String): Boolean? {
        return chatMessagesDao?.isUserCanceledUpload(messageId.toUpperCase(Locale.ROOT))
    }

    fun getIsUserBlocked(kryptId: String): LiveData<Int>? {
        return blockListDao?.getBlockedUser(kryptId.toUpperCase())
    }

    fun getIsForwardable(selectedChatMessage: ArrayList<String>): List<ChatMessagesSchema>? {
        return chatMessagesDao?.getChatMessage(selectedChatMessage)
    }

    fun getReplyMessage(messageId: String): ChatMessagesSchema? {
        return chatMessagesDao?.getreplyMessage(messageId.toUpperCase())
    }

    fun sendDeleteMessage(
        selectedChatMessage: ArrayList<String>,
        group: Boolean,
        kryptId: String,
        roomId: String
    ) {

        for (i in selectedChatMessage.indices) {
            if (group) {
                XMPPOperations.deleteGroupMessage(roomId, selectedChatMessage[i])
            } else {
                XMPPOperations.deleteMessage(selectedChatMessage[i], kryptId)
            }

        }

    }

    fun burnMessage(kryptId: String, messageId: String) {
        Coroutine.iOWorker {
            if (burnMessageDao?.isMessageAvailable(messageId.toUpperCase()) == 0) {

                val message = BurnMessageSchema()
                message.messageId = messageId
                message.kryptId = kryptId

                burnMessageDao?.insertMessage(message)
                chatMessagesDao?.burnMessage(kryptId.toUpperCase())
            }
        }
    }

    fun deleteMessage(messageId: String) {
        chatMessagesDao?.updateMessageAsDeleted(messageId)
    }

    fun getAllMessages(selectedChatMessage: ArrayList<String>): List<ChatMessagesSchema>? {
        return chatMessagesDao?.getAllMessages(selectedChatMessage as List<String>)
    }

    fun createGroupCall(apiParams: ApiInput): MutableLiveData<CommonResponseModel> {

        val apiResponse: MutableLiveData<CommonResponseModel> = MutableLiveData()

        Api.postMethod(apiParams, object : ApiResponseCallback {
            override fun setResponseSuccess(jsonObject: JSONObject) {
                val gson = Gson()
                val response: CommonResponseModel =
                    gson.fromJson(jsonObject.toString(), CommonResponseModel::class.java)
                apiResponse.value = response
            }

            override fun setErrorResponse(error: String) {
                val response = CommonResponseModel()
                response.error = "true"
                response.message = error
                apiResponse.value = response
            }
        })
        return apiResponse

    }

    fun setApiMessage(apiParams: ApiInput) {

        Api.postMethod(apiParams, object : ApiResponseCallback {
            override fun setResponseSuccess(jsonObject: JSONObject) {}

            override fun setErrorResponse(error: String) {}
        })
    }

    fun resetMessages(apiParams: ApiInput) {

        Api.postMethod(apiParams, object : ApiResponseCallback {
            override fun setResponseSuccess(jsonObject: JSONObject) {}

            override fun setErrorResponse(error: String) {}
        })
    }
}