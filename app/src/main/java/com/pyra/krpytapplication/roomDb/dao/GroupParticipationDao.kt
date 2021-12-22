package com.pyra.krpytapplication.roomDb.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pyra.krpytapplication.roomDb.entity.GroupParticipationSchema

@Dao
interface GroupParticipationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceChatList(groupParticipationSchema: GroupParticipationSchema?)


    @Query("SELECT ChatParticipation.id,ChatParticipation.roomName,ChatParticipation.roomId,ChatParticipation.kryptId,ChatParticipation.userImage,ChatParticipation.role, (SELECT roomName FROM ChatList WHERE UPPER(ChatList.kryptId) = UPPER(ChatParticipation.kryptId)) as userName FROM ChatParticipation WHERE UPPER(ChatParticipation.roomId) = :roomId")
    fun getParticipations(roomId: String): LiveData<List<GroupParticipationSchema>>

    @Query("SELECT * FROM ChatParticipation WHERE UPPER(roomId) =:roomId")
    fun getParticipationsPrivateGrp(roomId: String): LiveData<List<GroupParticipationSchema>>

    @Query("SELECT COUNT(roomId) FROM ChatParticipation WHERE UPPER(roomId) = :roomID AND UPPER(kryptId) =:userId")
    suspend fun getParticipationExist(roomID: String, userId: String): Int

    @Query("SELECT * FROM ChatParticipation WHERE UPPER(roomId) =:roomId")
    suspend fun getParticipants(roomId: String): List<GroupParticipationSchema>

    @Query("DELETE FROM ChatParticipation WHERE UPPER(roomId) = :roomId AND UPPER(kryptId) NOT IN (:presentMemberList)")
    fun removeParticipants(roomId: String, presentMemberList: List<String>)

    @Query("DELETE FROM ChatParticipation WHERE UPPER(roomId) = :roomId AND UPPER(kryptId) = :userId")
    fun removeUserFromGroupDb(roomId: String, userId: String)

    @Query("DELETE FROM ChatParticipation WHERE UPPER(roomId) NOT IN (:presentGrpList)")
    fun removeUsersFromGroup(presentGrpList: List<String>)

    @Query("DELETE FROM ChatParticipation WHERE UPPER(roomId) = :roomId")
    fun quitGroup(roomId: String)

    @Query("SELECT DISTINCT UPPER(kryptId) FROM ChatParticipation")
    fun getUsers(): List<String>

}