package com.pyra.krpytapplication.repositories.implementations

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.roomDb.AppDataBase
import com.pyra.krpytapplication.roomDb.dao.ChatListDao
import com.pyra.krpytapplication.roomDb.dao.ChatMessagesDao
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema
import com.pyra.krpytapplication.roomDb.entity.ChatMessagesSchema
import org.jetbrains.anko.doAsync
import java.util.ArrayList

class ForwardRepository private constructor(appDataBase: AppDataBase?) {



    companion object {

        var repository: ForwardRepository? = null
        private var chatListDao: ChatListDao? = null
        private var chatMessagesDao: ChatMessagesDao? = null
        fun getInstance(appDataBase: AppDataBase?): ForwardRepository {
            if (repository == null) {
                repository = ForwardRepository(appDataBase)
            }
            return repository as ForwardRepository
        }
    }

    init {
        chatListDao = appDataBase?.chatListDao()
        chatMessagesDao = appDataBase?.chatMessagesDao()

    }

    fun getChatList(): LiveData<List<ChatListSchema>>? {
        return chatListDao?.getChatList()
    }

    fun getMessageList(list: ArrayList<String>): List<ChatMessagesSchema>? {
        return chatMessagesDao?.getChatMessage(list)
    }

    fun getselectedChatData(selectedList: ArrayList<String>): List<ChatListSchema>? {
        return chatListDao?.getselectedChatData(selectedList)
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