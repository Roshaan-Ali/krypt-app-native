package com.pyra.krpytapplication.roomDb.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface ClearDbDao {

    @Query("DELETE FROM ChatList")
    fun clearChatList()

    @Query("DELETE FROM ChatMessages")
    fun clearChatMessageList()

    @Query("DELETE FROM ChatParticipation")
    fun clearParticipation()

    @Query("DELETE FROM BlockList")
    fun clearBlockUser()

    @Query("DELETE FROM BurnMessage")
    fun clearBurnMessages()

}