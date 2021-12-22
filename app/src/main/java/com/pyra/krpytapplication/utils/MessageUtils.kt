package com.pyra.krpytapplication.utils

enum class MessageType {
    TEXT,
    IMAGE,
    AUDIO,
    VIDEO,
    DOCUMENT,
    CONTACT,
    LOCATION,
    MISSEDCALL,
    NONE
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

fun String.toMessageStatus(): MessageStatus {
    when (this) {
        "sending" -> return MessageStatus.SENDING
        "delivered" -> return MessageStatus.DELIVERED
        "sent" -> return MessageStatus.SENT
        "read" -> return MessageStatus.READ
    }
    return MessageStatus.SENDING
}

fun MessageStatus.toMessageString(): String {
    return when (this) {
        MessageStatus.SENDING -> "sending"
        MessageStatus.SENT -> "sent"
        MessageStatus.DELIVERED -> "delivered"
        MessageStatus.READ -> "read"
    }
}

fun String.toMessageType(): MessageType {
    when (this) {
        "text" -> return MessageType.TEXT
        "image" -> return MessageType.IMAGE
        "audio" -> return MessageType.AUDIO
        "video" -> return MessageType.VIDEO
        "document" -> return MessageType.DOCUMENT
        "contact" -> return MessageType.CONTACT
        "location" -> return MessageType.LOCATION
        "missedcall" -> return MessageType.MISSEDCALL
    }
    return MessageType.TEXT
}

fun MessageType.toMessageString(): String {
    return when (this) {
        MessageType.TEXT -> "text"
        MessageType.IMAGE -> "image"
        MessageType.AUDIO -> "audio"
        MessageType.VIDEO -> "video"
        MessageType.DOCUMENT -> "document"
        MessageType.CONTACT -> "contact"
        MessageType.LOCATION -> "location"
        MessageType.MISSEDCALL -> "missedcall"
        MessageType.NONE -> ""

    }
}

