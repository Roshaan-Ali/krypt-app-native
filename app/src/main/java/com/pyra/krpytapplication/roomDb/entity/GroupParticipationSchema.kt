package com.pyra.krpytapplication.roomDb.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ChatParticipation")
class GroupParticipationSchema {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0

    @ColumnInfo(name = "roomId", defaultValue = "")
    var roomId: String = ""

    @ColumnInfo(name = "roomName", defaultValue = "")
    var roomName: String = ""

    @ColumnInfo(name = "kryptId", defaultValue = "")
    var kryptId: String = ""

    @ColumnInfo(name = "userName", defaultValue = "")
    var userName: String? = ""

    @ColumnInfo(name = "userImage", defaultValue = "")
    var userImage: String = ""

    @ColumnInfo(name = "role", defaultValue = "")
    var role: String = ""

}