package com.pyra.krpytapplication.roomDb

import com.pyra.krpytapplication.utils.MessageStatus
import com.pyra.krpytapplication.utils.MessageType
import com.pyra.krpytapplication.utils.toMessageString
import com.pyra.krpytapplication.roomDb.entity.ChatMessagesSchema
import java.util.*

class ChatMessageSchemaFactory {
    companion object {
        fun createSenderTextMessage(
            roomId: String,
            toId: String,
            userName: String,
            userImage: String,
            message: String,
            value: ChatMessagesSchema?,
            myKryptKey: String,
            group: Boolean
        ): ChatMessagesSchema {
            val messagesEntity = ChatMessagesSchema()
            messagesEntity.messageId = UUID.randomUUID().toString()
            messagesEntity.message = message
            messagesEntity.messageType = MessageType.TEXT.toMessageString()
            messagesEntity.messageStatus = MessageStatus.SENDING.toMessageString()
            messagesEntity.messageTime = System.currentTimeMillis().toString()
            messagesEntity.isSender = true
            messagesEntity.roomId = roomId
            messagesEntity.isEdited = false
            if (group) {
                messagesEntity.kryptId = myKryptKey
            } else {
                messagesEntity.kryptId = toId
            }

            messagesEntity.userImage = userImage
            messagesEntity.userName = userName

            value?.let {

                if (value.messageId != "") {

                    messagesEntity.isReply = true
                    if (value.isSender) {
                        messagesEntity.replyedKryptId = myKryptKey
                    } else {
                        messagesEntity.replyedKryptId = value.kryptId
                    }

                    if (value.messageType.equals(MessageType.TEXT.toString(), ignoreCase = true)
                    ) {
                        messagesEntity.replyedMessage = value.message
                    } else {
                        messagesEntity.replyedMessage = value.mediaUrl
                    }

                    messagesEntity.replyedMessageType = value.messageType


                } else {
                    messagesEntity.isReply = false
                }


            }


            return messagesEntity
        }

        fun createReceiverTextMessage(
            messageId: String,
            messageTime: String,
            roomId: String,
            toId: String,
            userName: String,
            userImage: String,
            message: String,
            reply: Boolean,
            replyedKryptId: String,
            replyedMessageType: String,
            replyedMessage: String
        ): ChatMessagesSchema {
            val messagesEntity = ChatMessagesSchema()
            messagesEntity.messageId = messageId
            messagesEntity.message = message
            messagesEntity.messageType = MessageType.TEXT.toMessageString()
            messagesEntity.messageStatus = MessageStatus.SENT.toMessageString()
            messagesEntity.messageTime = messageTime
            messagesEntity.isSender = false
            messagesEntity.roomId = roomId
            messagesEntity.isDeleted = false
            messagesEntity.isEdited = false
            messagesEntity.kryptId = toId
            messagesEntity.userImage = userImage
            messagesEntity.userName = userName

            messagesEntity.isReply = reply
            messagesEntity.replyedKryptId = replyedKryptId
            messagesEntity.replyedMessageType = replyedMessageType
            messagesEntity.replyedMessage = replyedMessage

            return messagesEntity
        }

        fun createLocalImageMesssage(
            roomId: String,
            toId: String,
            userName: String,
            userImage: String,
            message: String,
            thumbImage: String,
            localPath: String,
            value: ChatMessagesSchema?,
            kryptKey: String,
            group: Boolean
        ): ChatMessagesSchema {

            val messagesEntity = ChatMessagesSchema()
            messagesEntity.messageId = UUID.randomUUID().toString()
            messagesEntity.message = message
            messagesEntity.messageType = MessageType.IMAGE.toMessageString()
            messagesEntity.messageStatus = MessageStatus.SENT.toMessageString()
            messagesEntity.messageTime = System.currentTimeMillis().toString()
            messagesEntity.isSender = true
            messagesEntity.roomId = roomId
            messagesEntity.isDeleted = false
            messagesEntity.isEdited = false
            messagesEntity.isUploaded = false
            if (group) {
                messagesEntity.kryptId = kryptKey
            } else {
                messagesEntity.kryptId = toId
            }

            messagesEntity.userImage = userImage
            messagesEntity.userName = userName
            messagesEntity.mediaThumbUrl = thumbImage
            messagesEntity.localMediaPath = localPath

            value?.let {

                if (value.messageId != "") {

                    messagesEntity.isReply = true
                    if (value.isSender) {
                        messagesEntity.replyedKryptId = kryptKey
                    } else {
                        messagesEntity.replyedKryptId = value.kryptId
                    }

                    if (value.messageType.equals(MessageType.TEXT.toString(), ignoreCase = true)
                    ) {
                        messagesEntity.replyedMessage = value.message
                    } else {
                        messagesEntity.replyedMessage = value.mediaUrl
                    }

                    messagesEntity.replyedMessageType = value.messageType


                } else {
                    messagesEntity.isReply = false
                }


            }


            return messagesEntity
        }


        fun createSenderImageMesssage(
            data: ChatMessagesSchema
        ): ChatMessagesSchema {

            val messagesEntity = ChatMessagesSchema()
            messagesEntity.messageId = data.messageId
            messagesEntity.message = data.message
            messagesEntity.messageType = MessageType.IMAGE.toMessageString()
            messagesEntity.messageStatus = MessageStatus.SENT.toMessageString()
            messagesEntity.messageTime = System.currentTimeMillis().toString()
            messagesEntity.isSender = true
            messagesEntity.roomId = data.roomId
            messagesEntity.isDeleted = false
            messagesEntity.isEdited = false
            messagesEntity.isUploaded = false
            messagesEntity.kryptId = data.kryptId
            messagesEntity.userImage = data.userImage
            messagesEntity.userName = data.userName
            messagesEntity.mediaThumbUrl = data.mediaThumbUrl
            messagesEntity.mediaUrl = data.mediaUrl

            messagesEntity.isReply = data.isReply
            messagesEntity.replyedKryptId = data.replyedKryptId
            messagesEntity.replyedMessageType = data.replyedMessageType
            messagesEntity.replyedMessage = data.replyedMessage

            return messagesEntity
        }


        fun createReceiverImageMessage(
            messageId: String,
            messageTime: String,
            roomId: String,
            toId: String,
            userName: String,
            userImage: String,
            message: String,
            mediaUrl: String,
            mediaThumbUrl: String,
            reply: Boolean,
            replyedKryptId: String,
            replyedMessageType: String,
            replyedMessage: String
        ): ChatMessagesSchema {
            val messagesEntity = ChatMessagesSchema()
            messagesEntity.messageId = messageId
            messagesEntity.message = message
            messagesEntity.messageType = MessageType.IMAGE.toMessageString()
            messagesEntity.messageStatus = MessageStatus.SENT.toMessageString()
            messagesEntity.messageTime = messageTime
            messagesEntity.isSender = false
            messagesEntity.roomId = roomId
            messagesEntity.isDeleted = false
            messagesEntity.isMediaCancelledByUser = true
            messagesEntity.isEdited = false
            messagesEntity.kryptId = toId
            messagesEntity.userImage = userImage
            messagesEntity.userName = userName
            messagesEntity.mediaUrl = mediaUrl
            messagesEntity.mediaThumbUrl = mediaThumbUrl

            messagesEntity.isReply = reply
            messagesEntity.replyedKryptId = replyedKryptId
            messagesEntity.replyedMessageType = replyedMessageType
            messagesEntity.replyedMessage = replyedMessage

            return messagesEntity
        }


        fun createLocalVideoMesssage(
            roomId: String,
            toId: String,
            userName: String,
            userImage: String,
            message: String,
            thumbImage: String,
            localPath: String,
            mediaTime: String,
            value: ChatMessagesSchema?,
            kryptKey: String,
            group: Boolean
        ): ChatMessagesSchema {

            val messagesEntity = ChatMessagesSchema()
            messagesEntity.messageId = UUID.randomUUID().toString()
            messagesEntity.message = message
            messagesEntity.messageType = MessageType.VIDEO.toMessageString()
            messagesEntity.messageStatus = MessageStatus.SENT.toMessageString()
            messagesEntity.messageTime = System.currentTimeMillis().toString()
            messagesEntity.isSender = true
            messagesEntity.roomId = roomId
            messagesEntity.isDeleted = false
            messagesEntity.isEdited = false
            messagesEntity.isUploaded = false
            if (group) {
                messagesEntity.kryptId = kryptKey
            } else {
                messagesEntity.kryptId = toId
            }
            messagesEntity.userImage = userImage
            messagesEntity.userName = userName
            messagesEntity.mediaThumbUrl = thumbImage
            messagesEntity.localMediaPath = localPath
            messagesEntity.mediaLength = mediaTime


            value?.let {

                if (value.messageId != "") {

                    messagesEntity.isReply = true
                    if (value.isSender) {
                        messagesEntity.replyedKryptId = kryptKey
                    } else {
                        messagesEntity.replyedKryptId = value.kryptId
                    }

                    if (value.messageType.toLowerCase() == MessageType.TEXT.toString()
                            .toLowerCase()
                    ) {
                        messagesEntity.replyedMessage = value.message
                    } else {
                        messagesEntity.replyedMessage = value.mediaUrl
                    }

                    messagesEntity.replyedMessageType = value.messageType


                } else {
                    messagesEntity.isReply = false
                }


            }


            return messagesEntity
        }


        fun createSenderVideoMesssage(
            data: ChatMessagesSchema
        ): ChatMessagesSchema {

            val messagesEntity = ChatMessagesSchema()
            messagesEntity.messageId = data.messageId
            messagesEntity.message = data.message
            messagesEntity.messageType = MessageType.VIDEO.toMessageString()
            messagesEntity.messageStatus = MessageStatus.SENT.toMessageString()
            messagesEntity.messageTime = System.currentTimeMillis().toString()
            messagesEntity.isSender = true
            messagesEntity.roomId = data.roomId
            messagesEntity.isDeleted = false
            messagesEntity.isEdited = false
            messagesEntity.isUploaded = false
            messagesEntity.kryptId = data.kryptId
            messagesEntity.userImage = data.userImage
            messagesEntity.userName = data.userName
            messagesEntity.mediaThumbUrl = data.mediaThumbUrl
            messagesEntity.mediaUrl = data.mediaUrl
            messagesEntity.mediaLength = data.mediaLength

            messagesEntity.isReply = data.isReply
            messagesEntity.replyedKryptId = data.replyedKryptId
            messagesEntity.replyedMessageType = data.replyedMessageType
            messagesEntity.replyedMessage = data.replyedMessage

            return messagesEntity
        }


        fun createReceiverVideoMessage(
            messageId: String,
            messageTime: String,
            roomId: String,
            toId: String,
            userName: String,
            userImage: String,
            message: String,
            mediaUrl: String,
            mediaThumbUrl: String,
            mediaLength: String,
            reply: Boolean,
            replyedKryptId: String,
            replyedMessageType: String,
            replyedMessage: String
        ): ChatMessagesSchema {
            val messagesEntity = ChatMessagesSchema()
            messagesEntity.messageId = messageId
            messagesEntity.message = message
            messagesEntity.messageType = MessageType.VIDEO.toMessageString()
            messagesEntity.messageStatus = MessageStatus.SENT.toMessageString()
            messagesEntity.messageTime = messageTime
            messagesEntity.isSender = false
            messagesEntity.roomId = roomId
            messagesEntity.isDeleted = false
            messagesEntity.isMediaCancelledByUser = true
            messagesEntity.isEdited = false
            messagesEntity.kryptId = toId
            messagesEntity.userImage = userImage
            messagesEntity.userName = userName
            messagesEntity.mediaUrl = mediaUrl
            messagesEntity.mediaThumbUrl = mediaThumbUrl
            messagesEntity.mediaLength = mediaLength

            messagesEntity.isReply = reply
            messagesEntity.replyedKryptId = replyedKryptId
            messagesEntity.replyedMessageType = replyedMessageType
            messagesEntity.replyedMessage = replyedMessage

            return messagesEntity
        }


        fun createLocalDocumentMesssage(
            roomId: String,
            toId: String,
            userName: String,
            userImage: String,
            message: String,
            localPath: String,
            fileType: String,
            fileName: String,
            value: ChatMessagesSchema?,
            kryptKey: String,
            group: Boolean
        ): ChatMessagesSchema {

            val messagesEntity = ChatMessagesSchema()
            messagesEntity.messageId = UUID.randomUUID().toString()
            messagesEntity.message = message
            messagesEntity.messageType = MessageType.DOCUMENT.toMessageString()
            messagesEntity.messageStatus = MessageStatus.SENT.toMessageString()
            messagesEntity.messageTime = System.currentTimeMillis().toString()
            messagesEntity.isSender = true
            messagesEntity.roomId = roomId
            messagesEntity.isDeleted = false
            messagesEntity.isEdited = false
            messagesEntity.isUploaded = false
            if (group) {
                messagesEntity.kryptId = kryptKey
            } else {
                messagesEntity.kryptId = toId
            }

            messagesEntity.userImage = userImage
            messagesEntity.userName = userName
            messagesEntity.localMediaPath = localPath
            messagesEntity.mediaDocumentName = fileName
            messagesEntity.mediaDocumentType = fileType

            value?.let {

                if (value.messageId != "") {

                    messagesEntity.isReply = true
                    if (value.isSender) {
                        messagesEntity.replyedKryptId = kryptKey
                    } else {
                        messagesEntity.replyedKryptId = value.kryptId
                    }

                    if (value.messageType.toLowerCase() == MessageType.TEXT.toString()
                            .toLowerCase()
                    ) {
                        messagesEntity.replyedMessage = value.message
                    } else {
                        messagesEntity.replyedMessage = value.mediaUrl
                    }

                    messagesEntity.replyedMessageType = value.messageType


                } else {
                    messagesEntity.isReply = false
                }


            }


            return messagesEntity
        }


        fun createSenderDocumentMesssage(
            data: ChatMessagesSchema
        ): ChatMessagesSchema {

            val messagesEntity = ChatMessagesSchema()
            messagesEntity.messageId = data.messageId
            messagesEntity.message = data.message
            messagesEntity.messageType = MessageType.DOCUMENT.toMessageString()
            messagesEntity.messageStatus = MessageStatus.SENT.toMessageString()
            messagesEntity.messageTime = System.currentTimeMillis().toString()
            messagesEntity.isSender = true
            messagesEntity.roomId = data.roomId
            messagesEntity.isDeleted = false
            messagesEntity.isEdited = false
            messagesEntity.isUploaded = false
            messagesEntity.kryptId = data.kryptId
            messagesEntity.userImage = data.userImage
            messagesEntity.userName = data.userName
            messagesEntity.mediaUrl = data.mediaUrl
            messagesEntity.mediaDocumentType = data.mediaDocumentType
            messagesEntity.mediaDocumentName = data.mediaDocumentName


            messagesEntity.isReply = data.isReply
            messagesEntity.replyedKryptId = data.replyedKryptId
            messagesEntity.replyedMessageType = data.replyedMessageType
            messagesEntity.replyedMessage = data.replyedMessage

            return messagesEntity
        }


        fun createReceiverDocumentMessage(
            messageId: String,
            messageTime: String,
            roomId: String,
            toId: String,
            userName: String,
            userImage: String,
            message: String,
            mediaUrl: String,
            mediaDocumentName: String,
            mediaDocumentType: String,
            reply: Boolean,
            replyedKryptId: String,
            replyedMessageType: String,
            replyedMessage: String
        ): ChatMessagesSchema {
            val messagesEntity = ChatMessagesSchema()
            messagesEntity.messageId = messageId
            messagesEntity.message = message
            messagesEntity.messageType = MessageType.DOCUMENT.toMessageString()
            messagesEntity.messageStatus = MessageStatus.SENT.toMessageString()
            messagesEntity.messageTime = messageTime
            messagesEntity.isSender = false
            messagesEntity.roomId = roomId
            messagesEntity.isDeleted = false
            messagesEntity.isMediaCancelledByUser = true
            messagesEntity.isEdited = false
            messagesEntity.kryptId = toId
            messagesEntity.userImage = userImage
            messagesEntity.userName = userName
            messagesEntity.mediaUrl = mediaUrl
            messagesEntity.mediaDocumentName = mediaDocumentName
            messagesEntity.mediaDocumentType = mediaDocumentType


            messagesEntity.isReply = reply
            messagesEntity.replyedKryptId = replyedKryptId
            messagesEntity.replyedMessageType = replyedMessageType
            messagesEntity.replyedMessage = replyedMessage

            return messagesEntity
        }


        fun createLocalAudioMesssage(
            roomId: String,
            toId: String,
            userName: String,
            userImage: String,
            message: String,
            localPath: String,
            mediaTime: String,
            mediaDocumentName: String,
            value: ChatMessagesSchema?,
            kryptKey: String,
            group: Boolean
        ): ChatMessagesSchema {

            val messagesEntity = ChatMessagesSchema()
            messagesEntity.messageId = UUID.randomUUID().toString()
            messagesEntity.message = message
            messagesEntity.messageType = MessageType.AUDIO.toMessageString()
            messagesEntity.messageStatus = MessageStatus.SENT.toMessageString()
            messagesEntity.messageTime = System.currentTimeMillis().toString()
            messagesEntity.isSender = true
            messagesEntity.roomId = roomId
            messagesEntity.isDeleted = false
            messagesEntity.isEdited = false
            messagesEntity.isUploaded = false
            if (group) {
                messagesEntity.kryptId = kryptKey
            } else {
                messagesEntity.kryptId = toId
            }

            messagesEntity.userImage = userImage
            messagesEntity.userName = userName
            messagesEntity.localMediaPath = localPath
            messagesEntity.mediaLength = mediaTime
            messagesEntity.mediaDocumentName = mediaDocumentName


            value?.let {

                if (value.messageId != "") {

                    messagesEntity.isReply = true
                    if (value.isSender) {
                        messagesEntity.replyedKryptId = kryptKey
                    } else {
                        messagesEntity.replyedKryptId = value.kryptId
                    }

                    if (value.messageType.toLowerCase() == MessageType.TEXT.toString()
                            .toLowerCase()
                    ) {
                        messagesEntity.replyedMessage = value.message
                    } else {
                        messagesEntity.replyedMessage = value.mediaUrl
                    }

                    messagesEntity.replyedMessageType = value.messageType


                } else {
                    messagesEntity.isReply = false
                }


            }


            return messagesEntity
        }


        fun createSenderAudioMesssage(
            data: ChatMessagesSchema
        ): ChatMessagesSchema {

            val messagesEntity = ChatMessagesSchema()
            messagesEntity.messageId = data.messageId
            messagesEntity.message = data.message
            messagesEntity.messageType = MessageType.AUDIO.toMessageString()
            messagesEntity.messageStatus = MessageStatus.SENT.toMessageString()
            messagesEntity.messageTime = System.currentTimeMillis().toString()
            messagesEntity.isSender = true
            messagesEntity.roomId = data.roomId
            messagesEntity.isDeleted = false
            messagesEntity.isEdited = false
            messagesEntity.isUploaded = false
            messagesEntity.kryptId = data.kryptId
            messagesEntity.userImage = data.userImage
            messagesEntity.userName = data.userName
            messagesEntity.mediaUrl = data.mediaUrl
            messagesEntity.mediaLength = data.mediaLength
            messagesEntity.mediaDocumentName = data.mediaDocumentName


            messagesEntity.isReply = data.isReply
            messagesEntity.replyedKryptId = data.replyedKryptId
            messagesEntity.replyedMessageType = data.replyedMessageType
            messagesEntity.replyedMessage = data.replyedMessage

            return messagesEntity
        }

        fun createReceiverAudioMessage(
            messageId: String,
            messageTime: String,
            roomId: String,
            toId: String,
            userName: String,
            userImage: String,
            message: String,
            mediaUrl: String,
            mediaDocumentName: String,
            mediaLength: String,
            reply: Boolean,
            replyedKryptId: String,
            replyedMessageType: String,
            replyedMessage: String
        ): ChatMessagesSchema {
            val messagesEntity = ChatMessagesSchema()
            messagesEntity.messageId = messageId
            messagesEntity.message = message
            messagesEntity.messageType = MessageType.AUDIO.toMessageString()
            messagesEntity.messageStatus = MessageStatus.SENT.toMessageString()
            messagesEntity.messageTime = messageTime
            messagesEntity.isSender = false
            messagesEntity.roomId = roomId
            messagesEntity.isDeleted = false
            messagesEntity.isMediaCancelledByUser = true
            messagesEntity.isEdited = false
            messagesEntity.kryptId = toId
            messagesEntity.userImage = userImage
            messagesEntity.userName = userName
            messagesEntity.mediaUrl = mediaUrl
            messagesEntity.mediaDocumentName = mediaDocumentName
            messagesEntity.mediaLength = mediaLength

            messagesEntity.isReply = reply
            messagesEntity.replyedKryptId = replyedKryptId
            messagesEntity.replyedMessageType = replyedMessageType
            messagesEntity.replyedMessage = replyedMessage

            return messagesEntity
        }


        fun createForwardMessage(
            chatMessage: ChatMessagesSchema,
            kryptId: String,
            roomId: String,
            userName: String?,
            userImage: String,
        ): ChatMessagesSchema {

            chatMessage.messageId = UUID.randomUUID().toString()
            chatMessage.kryptId = kryptId
            chatMessage.roomId = roomId
            chatMessage.userName = userName
            chatMessage.userImage = userImage

            chatMessage.messageStatus = MessageStatus.SENDING.toMessageString()
            chatMessage.messageTime = System.currentTimeMillis().toString()

            chatMessage.isSender = true
            chatMessage.isSaved = false
            chatMessage.isUploaded = true
            chatMessage.isDeleted = false

            return chatMessage
        }

    }


}
