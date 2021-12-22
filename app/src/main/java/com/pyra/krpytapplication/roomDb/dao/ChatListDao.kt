package com.pyra.krpytapplication.roomDb.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceChatList(chatMessages: ChatListSchema?)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertChatList(chatMessages: ChatListSchema?)

    @Query("UPDATE ChatList  SET roomName = :userName WHERE UPPER(kryptId) ==:kryptKey")
    fun updateUserName(kryptKey: String, userName: String)

    @Query("UPDATE ChatList  SET isDeleted = 0,lastMessage = :lastMessage, lastMessageTime = :lastMessageTime, lastMessageType = :lastMessageType, lastMessageStatus = :lastMessageStatus WHERE UPPER(roomId) =:roomId")
    fun updateLastMessage(
        lastMessage: String,
        lastMessageTime: String,
        lastMessageType: String,
        lastMessageStatus: String,
        roomId: String
    )

    @Query("UPDATE ChatList  SET unReadCount = unReadCount+1 WHERE UPPER(roomId) =:roomId")
    fun updateUnreadMessageCount(roomId: String)

    @Query("UPDATE ChatList  SET unReadCount = 0 WHERE UPPER(roomId) =:roomId")
    fun resetUnreadMessageCount(roomId: String)

    @Query("SELECT * FROM ChatList WHERE roomName != '' AND chatType ='PRIVATE'")
    fun getNamedUser(): LiveData<List<ChatListSchema>>

    @Query("SELECT * FROM ChatList WHERE roomName == '' AND chatType ='PRIVATE'")
    fun getUnnamedUser(): LiveData<List<ChatListSchema>>

    @Query("SELECT * FROM ChatList WHERE isDeleted != 1 ORDER BY lastMessageTime DESC ")
    fun getChatList(): LiveData<List<ChatListSchema>>

    @Query("SELECT * FROM ChatList WHERE UPPER(kryptId) =:toUserName")
    fun getUserDetail(toUserName: String): LiveData<ChatListSchema>

    @Query("SELECT roomName FROM ChatList WHERE UPPER(roomId) = :roomId")
    fun getRoomName(roomId: String): String

    @Query("SELECT roomName FROM ChatList WHERE UPPER(kryptId) = :kryptId")
    fun getRoomNameByKryptId(kryptId: String): String

    @Query("SELECT roomImage FROM ChatList WHERE UPPER(roomId) = :roomId")
    fun getRoomImage(roomId: String): LiveData<String>

    @Query("UPDATE ChatList  SET lastMessage = '', lastMessageTime = '', lastMessageType = '', lastMessageStatus = '' WHERE UPPER(roomId) =:roomId")
    suspend fun removeMessage(roomId: String)

    @Query("SELECT COUNT(roomId) FROM ChatList WHERE UPPER(roomId) = :roomId")
    fun getCountOfUser(roomId: String): Int

    @Query("SELECT * FROM ChatList WHERE ( UPPER(roomId) LIKE :searchableString OR UPPER(roomName) LIKE :searchableString)")
    fun getSearchedList(searchableString: String): LiveData<List<ChatListSchema>>?

    @Query("SELECT * FROM ChatList WHERE UPPER(roomId) = :roomId")
    suspend fun getProfileData(roomId: String): ChatListSchema

    @Query("SELECT * FROM ChatList WHERE UPPER(kryptId) = :kryptId")
    suspend fun getRoomData(kryptId: String): ChatListSchema

    @Query("SELECT * FROM ChatList where chatType != 'PRIVATE'")
    fun getChatRoom(): List<ChatListSchema>

    @Query("SELECT * FROM ChatList where chatType = 'PRIVATE'")
    fun getUsersList(): List<ChatListSchema>

    @Query("SELECT kryptId FROM ChatList where chatType = 'PRIVATE'")
    fun getUsers(): List<String>

    @Query("SELECT COUNT(roomId) FROM ChatList WHERE UPPER(roomId) = :roomId")
    suspend fun getRoomExist(roomId: String): Int

    @Query("SELECT * FROM ChatList WHERE UPPER(kryptId) not in (:listofUser) AND chatType ='PRIVATE' AND roomName != '' ")
    fun getNamedUser(listofUser: ArrayList<String>): LiveData<List<ChatListSchema>>

    @Query("SELECT * FROM ChatList WHERE UPPER(kryptId) not in (:listofUser) AND roomName = '' AND chatType ='PRIVATE'")
    fun getUnnamedUser(listofUser: java.util.ArrayList<String>): LiveData<List<ChatListSchema>>

    @Query("DELETE FROM CHATLIST WHERE UPPER(roomId) NOT IN (:removedGroup) AND chatType !='PRIVATE'")
    fun markGrpAsRemoved(removedGroup: List<String>)

    @Query("UPDATE CHATLIST SET roomImage = :image , roomName = :name WHERE UPPER(roomId) = :id")
    fun updateGroupInfo(image: String, name: String, id: String)

    @Query("UPDATE CHATLIST SET roomImage = :image ,userStatus = :status WHERE UPPER(kryptId) = :kryptId")
    fun updateUserImage(status: String, image: String, kryptId: String)

    @Query("SELECT kryptId FROM CHATLIST WHERE chatType ='PRIVATE'")
    suspend fun getPrivateChatmembers(): List<String>?

    @Query("SELECT * FROM ChatList where roomId = :roomId")
    fun getRoomDataUsingRoomID(roomId: String): ChatListSchema

    @Query("UPDATE ChatList SET unReadCount = 0, isDeleted = 1 WHERE UPPER(roomId) in (:selectedRoomIds)")
    fun deleteChat(selectedRoomIds: ArrayList<String>)

    @Query("DELETE FROM ChatList WHERE UPPER(kryptId) in (:selectedContactList)")
    suspend fun removeContacts(selectedContactList: java.util.ArrayList<String>)

    @Query("UPDATE ChatList SET isDeleted = 1,lastMessage = '', lastMessageTime = :time, lastMessageType = '', lastMessageStatus = ''")
    fun clearAllChatsMesasges(time: String)

    @Query("UPDATE ChatList SET lastMessage = '', lastMessageTime = '', lastMessageType = '', lastMessageStatus = ''")
    fun removeAllMessage()


    @Query("SELECT * FROM ChatList WHERE roomId = :roomId")
    fun getUser(roomId: String): Flow<ChatListSchema>

    @Query("SELECT * FROM ChatList")
    fun getUserList(): Flow<MutableList<ChatListSchema>>

    @Query("UPDATE ChatList SET roomName = :roomName WHERE roomId =:roomId")
    suspend fun updateUser(roomId: String, roomName: String)

    @Query("SELECT * FROM ChatList WHERE UPPER(roomId) in (:selectedList)")
    fun getselectedChatData(selectedList: java.util.ArrayList<String>): List<ChatListSchema>

    @Query("DELETE FROM ChatList WHERE UPPER(roomId) = :roomId ")
    fun deleteGroup(roomId: String)

    @Query("UPDATE ChatList SET showNotification = :b WHERE UPPER(roomId) = :roomId")
    fun changeNotification(roomId: String, b: Boolean)

    @Query("SELECT * FROM ChatList WHERE UPPER(kryptId) = :toUpperCase")
    fun getonlineStatus(toUpperCase: String): LiveData<ChatListSchema>

    @Query("UPDATE ChatList SET isOnline = :status  ,lastseen = :lastSeen WHERE UPPER(kryptId) = :kryptId")
    fun updateOnline(kryptId: String, status: Int, lastSeen: String)


}