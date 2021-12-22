package com.pyra.krpytapplication.roomDb.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ChatMessages")
data class ChatMessagesSchema (

    @PrimaryKey
    @ColumnInfo(name = "messageId", defaultValue = "")
    var messageId: String = "",

    @ColumnInfo(name = "roomId", defaultValue = "")
    var roomId: String = "",

    @ColumnInfo(name = "kryptId", defaultValue = "")
    var kryptId: String = "",

    @ColumnInfo(name = "userName", defaultValue = "")
    var userName: String? = "",

    @ColumnInfo(name = "userImage", defaultValue = "")
    var userImage: String? = "",

    @ColumnInfo(name = "message", defaultValue = "")
    var message: String = "",

    @ColumnInfo(name = "messageType", defaultValue = "text")
    var messageType: String = "",

    @ColumnInfo(name = "messageStatus", defaultValue = "text")
    var messageStatus: String = "",

    @ColumnInfo(name = "messageTime", defaultValue = "")
    var messageTime: String = "",

    @ColumnInfo(name = "mediaUrl", defaultValue = "")
    var mediaUrl: String = "",

    @ColumnInfo(name = "mediaDocumentName", defaultValue = "")
    var mediaDocumentName: String = "",

    @ColumnInfo(name = "mediaDocumentType", defaultValue = "")
    var mediaDocumentType: String = "",

    @ColumnInfo(name = "mediaThumbUrl", defaultValue = "")
    var mediaThumbUrl: String = "",

    @ColumnInfo(name = "mediaCaption", defaultValue = "")
    var mediaCaption: String = "",

    @ColumnInfo(name = "mediaLength", defaultValue = "")
    var mediaLength: String = "",

    @ColumnInfo(name = "localMediaPath", defaultValue = "")
    var localMediaPath: String = "",

    @ColumnInfo(name = "latitude", defaultValue = "")
    var latitude: String = "",

    @ColumnInfo(name = "longitude", defaultValue = "")
    var longitude: String = "",

    @ColumnInfo(name = "locationName", defaultValue = "")
    var locationName: String = "",

    @ColumnInfo(name = "linkPreviewIcon", defaultValue = "")
    var linkPreviewIcon: String = "",

    @ColumnInfo(name = "linkPreviewDescription", defaultValue = "")
    var linkPreviewDescription: String = "",

    @ColumnInfo(name = "isEdited")
    var isEdited :Boolean = false ,

    @ColumnInfo(name = "isSender")
    var isSender:Boolean = true,

    @ColumnInfo(name = "isDeleted")
    var isDeleted:Boolean = false,

    @ColumnInfo(name = "isSaved")
    var isSaved:Boolean = false,

    @ColumnInfo(name = "isUploaded")
    var isUploaded:Boolean = false,

    //upload cancelled by senderOrReceiver
    @ColumnInfo(name = "isMediaCancelledByUser")
    var isMediaCancelledByUser:Boolean = false,

    @ColumnInfo(name = "contactName", defaultValue = "")
    var contactName: String = "",

    @ColumnInfo(name = "contactImage", defaultValue = "")
    var contactImage: String = "",

    @ColumnInfo(name = "contactNumber", defaultValue = "")
    var contactNumber: String = "",

    @ColumnInfo(name = "isRegisteredContact", defaultValue = "")
    var isRegisteredContact: Boolean = false,


    @ColumnInfo(name = "isReply")
    var isReply:Boolean = false,


    @ColumnInfo(name = "replyedKryptId", defaultValue = "")
    var replyedKryptId: String = "",

    @ColumnInfo(name = "replyerName", defaultValue = "")
    var replyerName: String? = "",


    @ColumnInfo(name = "replyedMessageType", defaultValue = "")
    var replyedMessageType: String = "",


    @ColumnInfo(name = "replyedMessage", defaultValue = "")
    var replyedMessage: String = "",

    @ColumnInfo(name = "accepteCall")
    var acceptedCall:Int = 0,

    @ColumnInfo(name = "rejectedCall")
    var rejectedCall:Int = 0,

    @ColumnInfo(name = "missedCall")
    var missedCall:Int = 0


)