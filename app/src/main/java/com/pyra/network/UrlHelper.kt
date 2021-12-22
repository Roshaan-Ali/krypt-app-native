package com.pyra.network

import com.pyra.krpytapplication.BuildConfig
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
object UrlHelper {

    private const val BASE = BuildConfig.BASE_URL
    const val SOCKETURL = BuildConfig.SOCKETURL
    private const val BASE_URL = BASE + "users/"

    const val CREATE_ACCOUNT = BASE_URL + "createAccount"
    const val LOGIN = BASE_URL + "login"

    const val CREATECALL = BASE_URL + "createCall"
    const val ACCEPTCALL = BASE_URL + "acceptCall"
    const val ENDCALL = BASE_URL + "rejectCall"
    const val UPDATEDEVICETOKEN = BASE_URL + "updateDeviceToken"
    const val SEARCHUSER = BASE_URL + "searchUsers"
    const val GETPROPERTIES = BASE_URL + "getProperties"
    const val UPDATEPROPERTIES = BASE_URL + "updateProperties"

    const val CREATEGROUP = BASE_URL + "createGroup"
    const val JOINGROUP = BASE_URL + "joinGroup"
    const val GETGROUPDETAILS = BASE_URL + "getGroupDetails"
    const val REMOVEUSERFROMGROUP = BASE_URL + "removeUserFromGroup"
    const val QUITFROMGROUP = BASE_URL + "quitFromGroup"
    const val TRIGGERNOTIFICATION = BASE_URL + "sendtestnotification"
    const val UPDATEGROUPPROPERTY = BASE_URL + "updateGroupProperty"
    const val GETUSERSPROPERTY = BASE_URL + "getUsersProperties"
    const val BLOCKUSERACCOUNT = BASE_URL + "blockUserAccount"
    const val UPDATEPASSWORD = BASE_URL + "updatePassword"
    const val GETUSERDETAILS = BASE_URL + "getUserDetails"

    const val CREATEGROUPCALL = BASE_URL + "createGroupCall"

    const val SENDMESSAGE = BASE_URL + "sendmessage"
    const val RESETMESSAGESTATUS = BASE_URL + "resetsendmessagestatus"

    const val UPDATEONLINE = "updateOnline"
    const val UPDATEOFFLINE = "updateOffline"

}