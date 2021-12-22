package com.pyra.krpytapplication.chat

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.*
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.notification.NotificationUtils
import com.pyra.krpytapplication.repositories.implementations.ChatListRepository
import com.pyra.krpytapplication.repositories.implementations.ChatMessagesRepository
import com.pyra.krpytapplication.repositories.implementations.ProfileRepository
import com.pyra.krpytapplication.roomDb.ChatMessageSchemaFactory
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema
import com.pyra.krpytapplication.roomDb.entity.ChatMessagesSchema
import java.util.*

class ChatMessageReceiverWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    private val chatListRepository: ChatListRepository
    private val chatMessagesRepository: ChatMessagesRepository
    private val profileRepository: ProfileRepository

    val context: Context = appContext
    private val parameters: WorkerParameters = workerParams
    var app = MyApp.getInstance()

    init {
        chatListRepository = ChatListRepository.getInstance(app.getAppDatabase())
        chatMessagesRepository = ChatMessagesRepository.getInstance(app.getAppDatabase())
        profileRepository = ProfileRepository.getInstance(app.getAppDatabase())
    }


    private fun createContactIfNotexists(messagesEntity: ChatMessagesSchema) {
        try {
            val roomId = messagesEntity.roomId.toUpperCase(Locale.ROOT)
            if (chatListRepository.getChatCount(roomId) == 0) {
                val chatEntity = ChatListSchema()
                chatEntity.chatType = "PRIVATE"
                chatEntity.kryptId = messagesEntity.kryptId.split("@")[0]
                chatEntity.roomId = roomId
                chatEntity.showNotification = true
                chatListRepository.insertData(chatEntity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun doWork(): Result {


        if (SharedHelper(app).kryptKey == "") {
            return Result.success()
        }

        val message = parameters.inputData.getString("message").toString()
        val messageId = parameters.inputData.getString("messageId").toString()
        val messageTime = parameters.inputData.getString("messageTime").toString()
        val messageType = parameters.inputData.getString("messageType")?.toMessageType()
        val roomId = parameters.inputData.getString("roomId").toString()
        val from = parameters.inputData.getString("from").toString().bareUsername()
        val roomName = parameters.inputData.getString("roomName").toString()
        val roomImage = parameters.inputData.getString("roomImage").toString()

        val mediaThumbUrl = parameters.inputData.getString("mediaThumbUrl").toString()
        val mediaUrl = parameters.inputData.getString("mediaUrl").toString()
        val mediaLength = parameters.inputData.getString("mediaLength").toString()

        val mediaDocumentName = parameters.inputData.getString("mediaDocumentName").toString()
        val mediaDocumentType = parameters.inputData.getString("mediaDocumentType").toString()

        val groupType = parameters.inputData.getString("groupType").toString()
        var isReply = parameters.inputData.getString("isReply").toString() == "true"
        val replyedKryptId = parameters.inputData.getString("replyedKryptId").toString()
        val replyedMessageType = parameters.inputData.getString("replyedMessageType").toString()
        val replyedMessage = parameters.inputData.getString("replyedMessage").toString()

        LogUtil.d("Message ", message)
        var messagesEntity = ChatMessagesSchema()

        when (messageType) {
            MessageType.TEXT -> {
                messagesEntity = ChatMessageSchemaFactory.createReceiverTextMessage(
                    messageId,
                    messageTime,
                    roomId,
                    from,
                    roomName,
                    roomImage,
                    message,
                    isReply,
                    replyedKryptId,
                    replyedMessageType,
                    replyedMessage
                )


            }
            MessageType.IMAGE -> {

                messagesEntity = ChatMessageSchemaFactory.createReceiverImageMessage(
                    messageId,
                    messageTime,
                    roomId,
                    from,
                    roomName,
                    roomImage,
                    message,
                    mediaUrl,
                    mediaThumbUrl, isReply, replyedKryptId, replyedMessageType, replyedMessage
                )


            }
            MessageType.AUDIO -> {
                messagesEntity = ChatMessageSchemaFactory.createReceiverAudioMessage(
                    messageId,
                    messageTime,
                    roomId,
                    from,
                    roomName,
                    roomImage,
                    message,
                    mediaUrl,
                    mediaDocumentName,
                    mediaLength, isReply, replyedKryptId, replyedMessageType, replyedMessage
                )
            }
            MessageType.VIDEO -> {
                messagesEntity = ChatMessageSchemaFactory.createReceiverVideoMessage(
                    messageId,
                    messageTime,
                    roomId,
                    from,
                    roomName,
                    roomImage,
                    message,
                    mediaUrl,
                    mediaThumbUrl,
                    mediaLength, isReply, replyedKryptId, replyedMessageType, replyedMessage
                )

            }
            MessageType.DOCUMENT -> {
                messagesEntity = ChatMessageSchemaFactory.createReceiverDocumentMessage(
                    messageId,
                    messageTime,
                    roomId,
                    from,
                    roomName,
                    roomImage,
                    message,
                    mediaUrl,
                    mediaDocumentName,
                    mediaDocumentType, isReply, replyedKryptId, replyedMessageType, replyedMessage
                )

            }
            MessageType.CONTACT -> TODO()
            MessageType.LOCATION -> TODO()
        }


        if (groupType == "" || groupType == "null") {
            createContactIfNotexists(messagesEntity)
            if (profileRepository.isUserBlocked(
                    messagesEntity.kryptId.bareUsername().toUpperCase()
                ) == 1
            ) {
                return Result.success()
            }

        }


        chatMessagesRepository.insertMessage(messagesEntity)
        chatListRepository.updateLastMessage(messagesEntity)
        chatListRepository.updateMessageCount(messagesEntity)



        if (SharedHelper(MyApp.getInstance().baseContext).currentChatingUser.toUpperCase() == roomId.toUpperCase()) {
            return Result.success()
        }

        val name = chatListRepository.getChatRoomName(roomId)

        val title = if (name == "") {
            applicationContext.getString(
                R.string.message_from
            ) + " " + messagesEntity.kryptId.bareUsername()
        } else {
            applicationContext.getString(
                R.string.message_from
            ) + " " + name
        }

        Coroutine.iOWorker {
            val roomDetails = chatListRepository.getProfileData(roomId.toUpperCase())

            val isAddedToContact = roomDetails?.roomName != ""
            val notificationEnabled = roomDetails?.showNotification
            val name = if (roomDetails?.roomName != "") {
                roomDetails?.roomName
            } else {
                roomDetails.kryptId
            }

            notificationEnabled?.let {
                if(notificationEnabled){
                    NotificationUtils(MyApp.getInstance().baseContext).createMessageNotification(
                        title,
                        messagesEntity.message,
                        messagesEntity.roomId,
                        roomDetails?.kryptId,
                        isAddedToContact,
                        name
                    )
                }
            }

        }



        return Result.success()

    }

}