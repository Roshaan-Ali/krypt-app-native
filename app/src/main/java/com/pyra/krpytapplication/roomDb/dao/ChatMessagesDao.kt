package com.pyra.krpytapplication.roomDb.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.pyra.krpytapplication.roomDb.entity.ChatMessagesSchema
import java.util.*

@Dao
interface ChatMessagesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceChatMessage(chatMessages: ChatMessagesSchema?)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertChatMessage(chatMessages: ChatMessagesSchema?)

    @Query("UPDATE ChatMessages  SET message = :message WHERE messageId =:messageId")
    fun updateChatMessage(messageId: String, message: String)

    @Query(
        "SELECT ChatMessages.messageId,ChatMessages.roomId,ChatMessages.kryptId,ChatMessages.message,ChatMessages.messageType,ChatMessages.messageStatus,ChatMessages.messageTime,ChatMessages.mediaUrl,ChatMessages.mediaDocumentName,ChatMessages.mediaDocumentType,ChatMessages.mediaThumbUrl,ChatMessages.mediaCaption,ChatMessages.mediaLength,ChatMessages.localMediaPath,ChatMessages.latitude,ChatMessages.longitude,ChatMessages.locationName,ChatMessages.linkPreviewIcon,ChatMessages.linkPreviewDescription,ChatMessages.isEdited,ChatMessages.isSender,ChatMessages.isDeleted,ChatMessages.isSaved,ChatMessages.isUploaded,ChatMessages.isMediaCancelledByUser,ChatMessages.contactName,ChatMessages.contactImage,ChatMessages.contactNumber,ChatMessages.isRegisteredContact,ChatMessages.userImage,ChatMessages.isReply,ChatMessages.replyedKryptId,ChatMessages.replyedMessageType,ChatMessages.replyedMessage,accepteCall,rejectedCall,missedCall ,(SELECT ChatList.roomName FROM ChatList WHERE UPPER(ChatList.kryptId) == UPPER(ChatMessages.kryptId)) AS userName,(SELECT ChatList.roomName FROM ChatList WHERE UPPER(ChatList.kryptId) == UPPER(ChatMessages.replyedKryptId)) AS replyerName FROM ChatMessages  WHERE UPPER(ChatMessages.roomId) =:roomId AND isDeleted = 0 ORDER BY messageTime DESC"
    )
    fun getChatMessages(roomId: String): DataSource.Factory<Int, ChatMessagesSchema>

    @Query(
        "SELECT ChatMessages.messageId,ChatMessages.roomId,ChatMessages.kryptId,ChatMessages.message,ChatMessages.messageType,ChatMessages.messageStatus,ChatMessages.messageTime,ChatMessages.mediaUrl,ChatMessages.mediaDocumentName,ChatMessages.mediaDocumentType,ChatMessages.mediaThumbUrl,ChatMessages.mediaCaption,ChatMessages.mediaLength,ChatMessages.localMediaPath,ChatMessages.latitude,ChatMessages.longitude,ChatMessages.locationName,ChatMessages.linkPreviewIcon,ChatMessages.linkPreviewDescription,ChatMessages.isEdited,ChatMessages.isSender,ChatMessages.isDeleted,ChatMessages.isSaved,ChatMessages.isUploaded,ChatMessages.isMediaCancelledByUser,ChatMessages.contactName,ChatMessages.contactImage,ChatMessages.contactNumber,ChatMessages.isRegisteredContact,ChatMessages.isReply,ChatMessages.replyedKryptId,ChatMessages.replyedMessageType,ChatMessages.replyedMessage,accepteCall,rejectedCall,missedCall, (SELECT ChatParticipation.userName FROM ChatParticipation WHERE UPPER(ChatMessages.kryptId) == UPPER(ChatParticipation.kryptId) AND UPPER(ChatMessages.roomId) == UPPER(ChatParticipation.roomId)) AS userName,  (SELECT ChatParticipation.userName FROM ChatParticipation WHERE UPPER(ChatMessages.replyedKryptId) == UPPER(ChatParticipation.kryptId) AND UPPER(ChatMessages.roomId) == UPPER(ChatParticipation.roomId)) AS replyerName, (SELECT ChatParticipation.userImage FROM ChatParticipation WHERE UPPER(ChatMessages.kryptId) == UPPER(ChatParticipation.kryptId) AND UPPER(ChatMessages.roomId) == UPPER(ChatParticipation.roomId)) AS userImage FROM ChatMessages WHERE UPPER(ChatMessages.roomId) =:roomId AND isDeleted = 0 ORDER BY messageTime DESC"
    )
    fun getChatMessagesPrivate(roomId: String): DataSource.Factory<Int, ChatMessagesSchema>

    @Query("SELECT * FROM ChatMessages WHERE isSaved = 1")
    fun getSavedMessages(): DataSource.Factory<Int, ChatMessagesSchema>


    @Query("UPDATE ChatMessages SET isSaved = :save WHERE UPPER(messageId) in (:list)")
    suspend fun saveMessage(list: List<String>, save: Boolean)


    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLocalMedia(chatMessages: ChatMessagesSchema?): Long

    @Query("UPDATE ChatMessages SET mediaUrl = :imageUrl ,isUploaded = 1 WHERE UPPER(messageId) = :messageId")
    fun updateSendMessage(messageId: String, imageUrl: String?)

    @Query("UPDATE ChatMessages SET messageStatus = :messageStatus WHERE UPPER(messageId) = :messageId")
    fun updateMessageStatus(messageId: String, messageStatus: String)

    @Query("SELECT * FROM ChatMessages WHERE UPPER(roomId) =:roomId AND messageStatus = 'sent' AND isSender = 0  AND isDeleted = 0 ORDER BY messageTime DESC")
    suspend fun getUnreadMessages(roomId: String): List<ChatMessagesSchema>

    @Query("SELECT * FROM ChatMessages WHERE UPPER(messageId) = :messageId AND isDeleted = 0")
    fun getChatDetails(messageId: String): ChatMessagesSchema

    @Query("UPDATE ChatMessages SET mediaThumbUrl = :thumbImage WHERE UPPER(messageId) = :messageId")
    fun updateThumbImage(thumbImage: String, messageId: String)

    @Query("UPDATE ChatMessages SET isMediaCancelledByUser = 1 WHERE UPPER(messageId) = :messageId")
    fun uploadCancelledByUser(messageId: String)

    @Query("UPDATE ChatMessages SET isMediaCancelledByUser = 0 WHERE UPPER(messageId) = :messageId")
    fun uploadStartedByUser(messageId: String)

    @Query("UPDATE ChatMessages SET localMediaPath = :downlodedFilePath , isMediaCancelledByUser = 1 WHERE UPPER(messageId) = :messageId")
    fun setMediaDownloded(messageId: String, downlodedFilePath: String)

    @Query("UPDATE ChatMessages SET isMediaCancelledByUser = 0 WHERE UPPER(messageId) = :messageId")
    fun downloadStartByUser(messageId: String)

    @Query("UPDATE ChatMessages SET isMediaCancelledByUser = 1 WHERE UPPER(messageId) = :messageId")
    fun downloadCanceledByUser(messageId: String)

    @Query("SELECT isMediaCancelledByUser FROM ChatMessages WHERE UPPER(messageId) = :messageId")
    suspend fun isUserCanceledUpload(messageId: String): Boolean

    @Query("SELECT DISTINCT localMediaPath,messageId,roomId,kryptId,message,messageType,messageStatus,messageTime,mediaUrl,mediaDocumentName,mediaDocumentType,mediaThumbUrl,mediaCaption,mediaLength,latitude,longitude,locationName,linkPreviewIcon,linkPreviewDescription,isEdited,isSender,isDeleted,isSaved,isUploaded,isMediaCancelledByUser,contactName,contactImage,contactNumber,isRegisteredContact,isReply,replyedKryptId,replyedMessageType,replyedMessage,userName,userImage,accepteCall,rejectedCall,missedCall FROM ChatMessages WHERE UPPER(messageType) =:messageType AND  mediaUrl != '' AND localMediaPath != '' GROUP BY localMediaPath")
    suspend fun getDownlodedList(messageType: String): List<ChatMessagesSchema>

    @Query("SELECT DISTINCT localMediaPath,messageId,roomId,kryptId,message,messageType,messageStatus,messageTime,mediaUrl,mediaDocumentName,mediaDocumentType,mediaThumbUrl,mediaCaption,mediaLength,latitude,longitude,locationName,linkPreviewIcon,linkPreviewDescription,isEdited,isSender,isDeleted,isSaved,isUploaded,isMediaCancelledByUser,contactName,contactImage,contactNumber,isRegisteredContact,isReply,replyedKryptId,replyedMessageType,replyedMessage,userName,userImage,accepteCall,rejectedCall,missedCall FROM ChatMessages WHERE UPPER(messageType) =:messageType AND  mediaUrl != '' AND localMediaPath != '' GROUP BY localMediaPath")
    fun getDownlodedMediaList(messageType: String): LiveData<List<ChatMessagesSchema>>

    @Query("SELECT COUNT(*) FROM ChatMessages WHERE  UPPER(messageId) in (:list) AND isSaved = 0")
    suspend fun getIsAllSaved(list: List<String>): Int


    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(chatmessage: ChatMessagesSchema)

    @Query("SELECT * FROM ChatMessages WHERE  UPPER(messageId) in (:selectedChatMessage)")
    fun getChatMessage(selectedChatMessage: ArrayList<String>): List<ChatMessagesSchema>

    @Query("SELECT * FROM ChatMessages WHERE  UPPER(messageId) = :messageId ")
    fun getreplyMessage(messageId: String): ChatMessagesSchema?

    @Update
    fun updateCallStatus(chatmessage: ChatMessagesSchema)

    @Query("SELECT * FROM ChatMessages  WHERE UPPER(messageId) in (:selectedChatMessage)")
    fun getAllMessages(selectedChatMessage: List<String>): List<ChatMessagesSchema>


    //Delete Messages

    @Query("DELETE FROM chatmessages WHERE messageId = :chatmessage")
    fun deletecallStatus(chatmessage: String)


    //delete All Messages


    //selection delete in chatactivity
    @Query("DELETE FROM ChatMessages WHERE UPPER(messageId) in (:list) AND isSaved = 0 ")
    suspend fun deleteMessage(list: List<String>)


    @Query("UPDATE  ChatMessages SET isDeleted = 1 WHERE UPPER(messageId) in (:list)")
    suspend fun updateMessageAsDeleted(list: List<String>)


    //
    @Query("DELETE FROM ChatMessages WHERE UPPER(roomId) in (:selectedRoomIds) AND isSaved = 0")
    fun deleteRoomsMessage(selectedRoomIds: ArrayList<String>)

    @Query(" UPDATE  ChatMessages SET isDeleted = 1 WHERE UPPER(roomId) in (:selectedRoomIds)")
    fun updateRoomsMessageAdDeleted(selectedRoomIds: ArrayList<String>)


    //
    @Query("DELETE FROM CHATMESSAGES WHERE isSaved = 0 ")
    fun clearAllMessage()

    @Query("UPDATE  ChatMessages SET isDeleted = 1")
    fun updateAsDeleteAllMessage()


    //
    @Query("DELETE FROM CHATMESSAGES WHERE isSender = 1 AND isSaved = 0")
    fun clearSentMessage()

    @Query("UPDATE  ChatMessages SET isDeleted = 1 WHERE isSender = 1 ")
    fun updateAsDeleteSentMessage()


    //
    @Query("DELETE FROM CHATMESSAGES WHERE UPPER(roomId) =:roomId AND isSaved = 0")
    suspend fun clearMessage(roomId: String)

    @Query("UPDATE  ChatMessages SET isDeleted = 1 WHERE  UPPER(roomId) =:roomId ")
    fun updateAsDeleteRoomMessage(roomId: String)


    @Query("UPDATE  chatmessages set isDeleted = 1 WHERE UPPER(messageId) = :messageId")
    fun updateMessageAsDeleted(messageId: String)


    @Query("UPDATE  chatmessages set isDeleted = 1 WHERE UPPER(kryptId) = :kryptId AND isSender = 0")
    fun burnMessage(kryptId: String)


}