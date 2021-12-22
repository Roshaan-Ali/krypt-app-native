package com.pyra.krpytapplication.roomDb.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pyra.krpytapplication.roomDb.entity.BlockListSchema

@Dao
interface BlockListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun blockUser(blockList: BlockListSchema)

    @Query("DELETE FROM BlockList WHERE UPPER(kryptId) = :krypyId")
    fun removeUser(krypyId: String)

    @Query("SELECT COUNT(kryptId) FROM BlockList WHERE UPPER(kryptId) = :krypyId")
    fun isUserBlocked(krypyId: String): Int

    @Query("SELECT COUNT(kryptId) FROM BlockList WHERE UPPER(kryptId) = :krypyId")
    fun getBlockedUser(krypyId: String): LiveData<Int>

    @Query("SELECT BlockList.kryptId,(SELECT roomName from ChatList WHERE UPPER(ChatList.kryptId) = UPPER(BlockList.kryptId)) AS roomName ,(SELECT roomImage from ChatList WHERE UPPER(ChatList.kryptId) = UPPER(BlockList.kryptId)) AS roomImage FROM BlockList")
    fun getAllBlockedUser(): LiveData<List<BlockListSchema>>

    @Query("SELECT COUNT(kryptId) FROM BlockList WHERE UPPER(kryptId) = :krypyId")
    suspend fun isBlocked(krypyId: String): Int

    @Query("DELETE FROM BlockList WHERE UPPER(kryptId) not in (:kryptList)")
    fun removeUnBlockedList(kryptList: List<String>)
}