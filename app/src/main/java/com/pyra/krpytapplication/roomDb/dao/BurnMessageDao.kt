package com.pyra.krpytapplication.roomDb.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pyra.krpytapplication.roomDb.entity.BurnMessageSchema

@Dao
interface BurnMessageDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertMessage(burnMessage: BurnMessageSchema)

    @Query("SELECT COUNT() FROM BurnMessage WHERE UPPER(messageId) = :messageId")
    fun isMessageAvailable(messageId: String): Int


}