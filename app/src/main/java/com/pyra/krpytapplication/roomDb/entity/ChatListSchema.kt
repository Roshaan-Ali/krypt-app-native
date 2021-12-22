package com.pyra.krpytapplication.roomDb.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ChatList")
data class ChatListSchema(

    @PrimaryKey
    @ColumnInfo(name = "roomId", defaultValue = "")
    var roomId: String = "",

    @ColumnInfo(name = "kryptId", defaultValue = "")
    var kryptId: String = "",

    @ColumnInfo(name = "chatType", defaultValue = "PRIVATE")
    var chatType: String = "",

    @ColumnInfo(name = "groupType", defaultValue = "PRIVATE")
    var groupType: String = "",

    @ColumnInfo(name = "roomName", defaultValue = "")
    var roomName: String? = "",

    @ColumnInfo(name = "roomImage", defaultValue = "")
    var roomImage: String = "",

    @ColumnInfo(name = "userStatus", defaultValue = "")
    var userStatus: String = "",

    @ColumnInfo(name = "wallpaper", defaultValue = "")
    var wallpaper: String = "",

    @ColumnInfo(name = "showInContacts")
    var showInContacts: Boolean = false,

    @ColumnInfo(name = "ContactDeleted", defaultValue = "")
    var ContactDeleted: String = "",

    @ColumnInfo(name = "lastMessage", defaultValue = "")
    var lastMessage: String = "",

    @ColumnInfo(name = "lastMessageType", defaultValue = "")
    var lastMessageType: String = "",

    @ColumnInfo(name = "lastMessageStatus", defaultValue = "")
    var lastMessageStatus: String = "",

    @ColumnInfo(name = "lastMessageTime", defaultValue = "")
    var lastMessageTime: String = "",

    @ColumnInfo(name = "isPinned")
    var isPinned: Boolean = false,

    @ColumnInfo(name = "isDeleted")
    var isDeleted: Boolean = false,

    @ColumnInfo(name = "isLocked")
    var isLocked: Boolean = false,

    @ColumnInfo(name = "hasBeenAddedToShortcut")
    var hasBeenAddedToShortcut: Boolean = false,

    @ColumnInfo(name = "lockPassword", defaultValue = "")
    var lockPassword: String = "",

    @ColumnInfo(name = "unReadCount")
    var unReadCount: Int = 0,

    @ColumnInfo(name = "sentBy", defaultValue = "")
    var sentBy: String? = "",

    @ColumnInfo(name = "showNotification")
    var showNotification: Boolean? = true,

    @ColumnInfo(name = "isOnline")
    var isOnline: Int = 0,

    @ColumnInfo(name = "lastSeen", defaultValue = "")
    var lastSeen: String = "",

)
