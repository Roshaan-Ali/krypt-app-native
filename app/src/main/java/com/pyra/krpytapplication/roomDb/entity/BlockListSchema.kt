package com.pyra.krpytapplication.roomDb.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "BlockList")
class BlockListSchema {

    @PrimaryKey
    @ColumnInfo(name = "kryptId", defaultValue = "")
    var kryptId: String = ""

    @ColumnInfo(name = "roomName", defaultValue = "")
    var roomName: String? = ""

    @ColumnInfo(name = "roomImage", defaultValue = "")
    var roomImage: String? = ""

}