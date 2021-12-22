package com.pyra.krpytapplication.repositories.implementations

import androidx.lifecycle.LiveData
import com.pyra.krpytapplication.utils.MessageType
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.roomDb.AppDataBase
import com.pyra.krpytapplication.roomDb.dao.ChatListDao
import com.pyra.krpytapplication.roomDb.dao.ChatMessagesDao
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema
import com.pyra.krpytapplication.roomDb.entity.ChatMessagesSchema
import org.jetbrains.anko.doAsync
import java.util.*

class VaultFragRepository private constructor(appDataBase: AppDataBase?) {


    init {
        chatMessagesDao = appDataBase?.chatMessagesDao()
        chatListDao = appDataBase?.chatListDao()
    }

    companion object {

        private var chatMessagesDao: ChatMessagesDao? = null
        private var chatListDao: ChatListDao? = null
        private var respository: VaultFragRepository? = null

        fun getInstance(appDataBase: AppDataBase?): VaultFragRepository {
            if (respository == null) {
                respository = VaultFragRepository(appDataBase)
            }
            return respository as VaultFragRepository
        }
    }

    suspend fun getDownloadedImageList(): List<ChatMessagesSchema>? {

        return chatMessagesDao?.getDownlodedList(
            MessageType.IMAGE.toString().toUpperCase(Locale.ROOT)
        )
    }

    suspend fun getDownloadedVideoList(): List<ChatMessagesSchema>? {
        return chatMessagesDao?.getDownlodedList(
            MessageType.VIDEO.toString().toUpperCase(Locale.ROOT)
        )
    }

    suspend fun getDownloadedDocumentList(): List<ChatMessagesSchema>? {
        return chatMessagesDao?.getDownlodedList(
            MessageType.DOCUMENT.toString().toUpperCase(Locale.ROOT)
        )
    }


    suspend fun deleteSelectedItems(selectedImageList: ArrayList<String>) {
        chatMessagesDao?.deleteMessage(selectedImageList)
        chatMessagesDao?.updateMessageAsDeleted(selectedImageList)
    }


    fun getImageList(): LiveData<List<ChatMessagesSchema>>? {
        return chatMessagesDao?.getDownlodedMediaList(
            MessageType.IMAGE.toString().toUpperCase(Locale.ROOT)
        )
    }

    fun getVideoList(): LiveData<List<ChatMessagesSchema>>? {
        return chatMessagesDao?.getDownlodedMediaList(
            MessageType.VIDEO.toString().toUpperCase(Locale.ROOT)
        )
    }

    fun getDocumentList(): LiveData<List<ChatMessagesSchema>>? {
        return chatMessagesDao?.getDownlodedMediaList(
            MessageType.DOCUMENT.toString().toUpperCase(Locale.ROOT)
        )
    }

    suspend fun getChatDetails(roomId : String): ChatListSchema? {
        return chatListDao?.getProfileData(roomId.toUpperCase())
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

    fun insertOrReplace(entity: ChatMessagesSchema) {
        chatMessagesDao?.insertOrReplaceChatMessage(entity)
    }


}