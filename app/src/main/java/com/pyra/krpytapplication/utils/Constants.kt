package com.pyra.krpytapplication.utils

import android.Manifest
import com.amazonaws.regions.Regions
import com.pyra.krpytapplication.BuildConfig
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
object Constants {

    object ApiKeys {

        const val USER_NAME = "userName"
        const val PASSWORD = "password"
        const val NEWPASSWORD = "newpassword"
        const val IMEI = "IMEI"
        const val OS = "os"
        const val DEVICETOKEN = "deviceToken"

        const val ID = "id"
        const val CHANNELNAME = "channelName"
        const val FROM = "from"
        const val TO = "to"
        const val CALLTYPE = "callType"

        const val FROMUSERNAME = "fromUserName"
        const val TOUSERNAME = "toUserName"
        const val NAME = "name"
        const val IMAGEURL = "imageUrl"
        const val PROPERTIES = "properties"
        const val KEY = "key"
        const val VALUE = "value"
        const val IMAGE = "image"
        const val STATUS = "status"

        const val USERNAME = "userName"
        const val GROUPNAME = "groupName"
        const val DESCRIPTION = "description"
        const val GROUPTITLENAME = "groupTitleName"
        const val OLDGROUPTITLENAME = "oldGroupTitleName"
        const val ROLE = "role"
        const val GROUPTYPE = "groupType"

        const val FROMUSER = "fromUser"
        const val TOUSER = "toUser"

    }

    object IntentKeys {
        const val GROUPTYPE = "groupType"
        const val IS_ADDED_TO_CONTACTS = "isAddedToContacts"
        const val KRYPTKEY = "kryptKey"
        const val DISPLAY_NAME = "displayName"
        const val CONTENT = "content"
        const val ISVIDEO = "isVideo"
        const val ROOMID = "roomId"
        const val ISGROUP = "isGroup"
        const val FILE = "file"
        const val FILEURL = "fileUrl"
        const val ISVIDEOAVAILABLE = "isvideoAvailable"
        const val ISUPLOADAVAILABLE = "isUploadAvailable"
        const val CHANGEPASSWORDFLOW = "isChangePassword"
        const val ISVAULT = "isVault"
        const val MEDIAURL = "mediaUrl"
        const val THUMBURL = "thumbUrl"
    }

    object Permission {

        const val READ_WRITE_STORAGE_PERMISSIONS = 202
        const val READ_STORAGE_PERMISSIONS = 202
        const val CAMERA_STORAGE_PERMISSIONS = 203
        const val READ_FILE_PERMISSIONS = 204
        const val READ_AUDIO_PERMISSIONS = 205
        const val VIDEO_CALL_PERMISSION = 206
        const val AUDIO_CALL_PERMISSION = 207
        const val RECORD_AUDIO = 208

        const val GROUP_VIDEO_CALL_PERMISSION = 209
        const val GROUP_AUDIO_CALL_PERMISSION = 210

        val CAMERA_PERM_LIST = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val READ_STORAGE_PERM_LIST = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        val AUDIO_CALL_PERMISSION_LIST = arrayOf(Manifest.permission.RECORD_AUDIO)
        val VIDEO_CALL_PERMISSION_LIST =
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)

        val GROUP_AUDIO_CALL_PERMISSION_LIST = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
            , Manifest.permission.RECORD_AUDIO
        )

        val GROUP_VIDEO_CALL_PERMISSION_LIST = arrayOf(
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
            , Manifest.permission.RECORD_AUDIO
        )

        val READ_WRITE_STORAGE_PERM_LIST = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

    }

    object RequestCode {

        const val CAMERA_INTENT = 101
        const val GALLERY_INTENT = 102
        const val FILE_INTENT = 103
        const val AUDIO_INTENT = 104
        const val DOC_INTENT = 105

    }

    object NotificationIds {

        const val CALL_NOTIFICATION_ID = 1
        const val INCOMING_CALL_CHANNEL_ID = "IncomingCall"
        const val INCOMING_CALL_CHANNEL_NAME = "IncomingCall"

        const val HANGUP_CALL_CHANNEL_ID = "HangupCall"
        const val HANGUP_CALL_CHANNEL_NAME = "HangupCall"

        const val INCOMING_MESSAGE_CHANNEL_NAME = "IncomingMessage"
        const val INCOMING_MESSAGE_CHANNEL_ID = "IncomingMessage"

    }

    object NotificationActions {

        const val ACCEPT_CALL = "Accept"
        const val REJECT_CALL = "Reject"
        const val HANGUP_CALL = "Hang Up"
        const val END_CALL = "End Call"

        const val CALLDIVERTION = "CallDivertion"

    }

    object NotificationIntentValues {

        const val CHANNEL_ID = "channelid"
        const val FROM_ID = "fromid"
        const val TO_ID = "toid"
        const val NAME = "docName"
        const val IMAGE = "docImage"
        const val CALL_TYPE = "voiceVideo"
        const val CALL_FROM = "incomingOutgoing"
        const val ID = "id"
        const val CALLDIVERTION = "CallDivertion"
        const val UNIQUEID = "uniqueId"
    }

    object ChatTypes {

        const val VIDEO_CALL = "video"
        const val VOICE_CALL = "voice"
        const val INCOMING_CALL = "incoming"
        const val OUTGOING_CALL = "outgoing"
        const val ENDCALL = "end_call"
    }

    object EventBusKeys {

        const val ACCEPT_CALL = "acceptcall"
        const val DOCTOR_ACCEPTED_CALL = "doctoracceptcall"
        const val DOCTOR_REJECTED_CALL = "doctorrejectcall"
        const val REJECT_CALL = "rejectCall"
        const val HANGUP_CALL = "hangupcall"
        const val END_CALL = "endcall"
    }

    object XMPPKeys {
        const val CHAT_DOMAIN = "xmpp.pyradev.com"
        const val GROUP_CHAT_INVITATION = "Group Intitation"
        const val GROUP_GREETINGS = "Welcome group"
    }

    //image Upload from amazon s3
    object AWS {
        const val POOL_ID: String = BuildConfig.POOL_ID
        const val BASE_S3_URL: String = BuildConfig.BASE_S3_URL
        const val ENDPOINT: String = BuildConfig.ENDPOINT
        const val BUCKET_NAME: String = BuildConfig.BUCKET_NAME
        val REGION = Regions.US_EAST_2
    }

    object SocketKeys {
        const val USERNAME = "userName"
    }

}