package com.pyra.krpytapplication.viewmodel

import android.app.Application
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.pyra.krpytapplication.utils.Coroutine
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.repositories.implementations.ForwardRepository
import com.pyra.krpytapplication.roomDb.ChatMessageSchemaFactory
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema
import com.pyra.krpytapplication.roomDb.entity.ChatMessagesSchema

class ForwardViewModel(application: Application) : AndroidViewModel(application) {

    val app: Application = application
    val repository = ForwardRepository.getInstance((application as MyApp).getAppDatabase())

    var chatList = ArrayList<ChatListSchema>()
    var notifyData: MutableLiveData<Void>? = MutableLiveData<Void>()
    var selectedList = ArrayList<String>()

    var messageList = ArrayList<ChatMessagesSchema>()
    var isUsersSelected = MutableLiveData<Boolean>()

    var messageForwarded: MutableLiveData<Void>? = MutableLiveData<Void>()
    fun getChatList() {

        repository.getChatList()?.observeForever(Observer {
            it?.let {
                chatList = it as ArrayList<ChatListSchema>
                notifyData?.value = null
            }

        })

    }

    fun getKryptId(position: Int): String? {
        return chatList[position].roomId
    }

    fun getUserImage(position: Int): String = chatList[position].roomImage

    fun isKryptContactSelected(position: Int): Boolean =
        selectedList.contains(chatList[position].roomId.toUpperCase())

    fun setSelected(position: Int) {

        if (selectedList.contains(chatList[position].roomId.toUpperCase())) {
            selectedList.remove(chatList[position].roomId.toUpperCase())
        } else {
            selectedList.add(chatList[position].roomId.toUpperCase())
        }

        isUsersSelected.value = selectedList.size != 0
        notifyData?.value = null
    }

    fun getUserName(position: Int): String? {

        return if (chatList[position].chatType == "PRIVATE") {
            if (chatList[position].roomName != "") {
                chatList[position].roomName
            } else {
                chatList[position].kryptId
            }
        } else {
            chatList[position].roomName
        }

    }

    fun getRawMessage(list: java.util.ArrayList<String>) {

        Coroutine.iOWorker {
            repository.getMessageList(list)?.let { list ->
                messageList = list as ArrayList<ChatMessagesSchema>
            }
        }

    }

    fun forwardmessage() {


        Coroutine.iOWorker {

            var selectedChatUsers: List<ChatListSchema>? =
                repository.getselectedChatData(selectedList)

            selectedChatUsers?.let {
                for (i in messageList.indices) {
                    for (j in selectedChatUsers.indices) {
                        var isGroup = selectedChatUsers[j].chatType != "PRIVATE"
                        var chatMessagesSchema = ChatMessageSchemaFactory.createForwardMessage(
                            messageList[i],
                            selectedChatUsers[j].kryptId,
                            selectedChatUsers[j].roomId,
                            selectedChatUsers[j].roomName,
                            selectedChatUsers[j].roomImage
                        )

                        repository.sendMessage(
                            chatMessagesSchema,
                            isGroup,
                            selectedChatUsers[j].groupType
                        )

                    }
                }
            }

            Coroutine.mainWorker {
                android.os.Handler(Looper.getMainLooper()).postDelayed({
                    messageForwarded?.value = null
                }, 500)

            }
        }
    }


}