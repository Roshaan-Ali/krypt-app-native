package com.pyra.krpytapplication.roomDb.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "BurnMessage")
class BurnMessageSchema {

    @PrimaryKey
    @ColumnInfo(name = "messageId", defaultValue = "")
    var messageId: String = ""

    @ColumnInfo(name = "kryptId", defaultValue = "")
    var kryptId: String = ""


}