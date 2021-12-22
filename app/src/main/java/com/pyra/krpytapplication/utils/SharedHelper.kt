package com.pyra.krpytapplication.utils

import android.content.Context
import java.util.*

class SharedHelper(context: Context) {

    private var sharedPreference: SharedPref = SharedPref(context)

    var imei: String
        get() : String {
            return sharedPreference.getKey("imei")
        }
        set(value) {
            sharedPreference.putKey("imei", value)
        }

    var kryptKey: String
        get() : String {
            return sharedPreference.getKey("kryptKey")
        }
        set(value) {
            sharedPreference.putKey("kryptKey", value)
        }

    var firebaseToken: String
        get() : String {
            return sharedPreference.getKey("firebaseToken")
        }
        set(value) {
            sharedPreference.putKey("firebaseToken", value)
        }

    var loggedIn: Boolean
        get() : Boolean {
            return sharedPreference.getBoolean("loggedIn")
        }
        set(value) {
            sharedPreference.putBoolean("loggedIn", value)
        }

    var token: String
        get() : String {
            return sharedPreference.getKey("token")
        }
        set(value) {
            sharedPreference.putKey("token", value)
        }

    var password: String
        get() : String {
            return sharedPreference.getKey("password")
        }
        set(value) {
            sharedPreference.putKey("password", value)
        }

    var status: String
        get() : String {
            return sharedPreference.getKey("status")
        }
        set(value) {
            sharedPreference.putKey("status", value)
        }

    var userName: String
        get() : String {
            return sharedPreference.getKey("userName")
        }
        set(value) {
            sharedPreference.putKey("userName", value)
        }

    var imageUploadPath: String
        get() : String {
            return sharedPreference.getKey("imageUploadPath")
        }
        set(value) {
            sharedPreference.putKey("imageUploadPath", value)
        }

    var userImage: String
        get() : String {
            return sharedPreference.getKey("userImage")
        }
        set(value) {
            sharedPreference.putKey("userImage", value)
        }

    var vaultPassword: String
        get() : String {
            return sharedPreference.getKey("vaultPassword")
        }
        set(value) {
            sharedPreference.putKey("vaultPassword", value)
        }

    var duressPassword: String
        get() : String {
            return sharedPreference.getKey("duressPassword")
        }
        set(value) {
            sharedPreference.putKey("duressPassword", value)
        }

    var loggedInTime: String
        get() : String {
            return sharedPreference.getKey("loggedInTime")
        }
        set(value) {
            sharedPreference.putKey("loggedInTime", value)
        }

    var currentChatingUser: String
        get() : String {
            return sharedPreference.getKey("currentChatingUser")
        }
        set(value) {
            sharedPreference.putKey("currentChatingUser", value)
        }

    var currentScreen: String
        get() : String {
            return sharedPreference.getKey("currentScreen")
        }
        set(value) {
            sharedPreference.putKey("currentScreen", value)
        }

    var burnMessageTime: Int
        get() : Int {
            return if (sharedPreference.getInt("burnMessageTime") == 0) {
                30
            } else {
                sharedPreference.getInt("burnMessageTime")
            }
        }
        set(value) {
            sharedPreference.putInt("burnMessageTime", value)
        }

    var burnMessageType: String
        get() : String {
            return if (sharedPreference.getKey("burnMessageType") == "") {
                MessageBurnType.DAYS.type
            } else {
                sharedPreference.getKey("burnMessageType")
            }
        }
        set(value) {
            sharedPreference.putKey("burnMessageType", value)
        }


    var autoLockTime: Int
        get() : Int {
            return if (sharedPreference.getInt("autoLockTime") == 0) {
                30
            } else {
                sharedPreference.getInt("autoLockTime")
            }
        }
        set(value) {
            sharedPreference.putInt("autoLockTime", value)
        }

    var autoLockType: String
        get() : String {
            return if (sharedPreference.getKey("autoLockType") == "") {
                MessageBurnType.DAYS.type
            } else {
                sharedPreference.getKey("autoLockType")
            }
        }
        set(value) {
            sharedPreference.putKey("autoLockType", value)
        }


    var autoLogoutSavedTime: String
        get() : String {
            return if (sharedPreference.getKey("autoLogoutSavedTime") == "") {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, 30)
                calendar.time.time.toString()
            } else {
                sharedPreference.getKey("autoLogoutSavedTime")
            }
        }
        set(value) {
            sharedPreference.putKey("autoLogoutSavedTime", value)
        }


    var showKryptScreen: Boolean
        get() : Boolean {
            return sharedPreference.getBoolean("showKryptScreen")
        }
        set(value) {
            sharedPreference.putBoolean("showKryptScreen", value)
        }

    var onlineUpdated: Boolean
        get() : Boolean {
            return sharedPreference.getBoolean("onlineUpdated")
        }
        set(value) {
            sharedPreference.putBoolean("onlineUpdated", value)
        }

    var theme: String
        get() : String {

            val themeData = sharedPreference.getKey("theme")

            return if (themeData == "") {
                "dark"
            } else {
                themeData
            }
        }
        set(value) {
            sharedPreference.putKey("theme", value)
        }

    var imageUrlPath: String
        get() : String {
            return sharedPreference.getKey("imageUrlPath")
        }
        set(value) {
            sharedPreference.putKey("imageUrlPath", value)
        }

    var imagePath: String
        get() : String {
            return sharedPreference.getKey("imagePath")
        }
        set(value) {
            sharedPreference.putKey("imagePath", value)
        }

    var isSocketUpdatedToOffline: Boolean
        get() : Boolean {
            return sharedPreference.getBoolean("isSocketUpdatedToOffline")
        }
        set(value) {
            sharedPreference.putBoolean("isSocketUpdatedToOffline", value)
        }

    var vaultPasswordEnabled: Boolean
        get() : Boolean {
            return sharedPreference.getBooleanDefaultTrue("vaultPasswordEnabled")
        }
        set(value) {
            sharedPreference.putBoolean("vaultPasswordEnabled", value)
        }

    var isBurnMessageEnabled: Boolean
        get() : Boolean {
            return sharedPreference.getBooleanDefaultTrue("isBurnMessageEnabled")
        }
        set(value) {
            sharedPreference.putBoolean("isBurnMessageEnabled", value)
        }

    var isMorphVoiceEnabled: Boolean
        @JvmName("getIsMorphEnabled")
        get() : Boolean {
            return sharedPreference.getBoolean("isMorphVoiceEnabled")
        }
        @JvmName("setIsMorphEnabled")
        set(value) {
            sharedPreference.putBoolean("isMorphVoiceEnabled", value)
        }

    var morphVoiceFrequency: String
        get() : String {
            return sharedPreference.getKey("morphVoiceFrequency")
        }
        set(value) {
            sharedPreference.putKey("morphVoiceFrequency", value)
        }

    var chatBubbleColor: String
        get() : String {
            return sharedPreference.getKey("chatBubbleColor")
        }
        set(value) {
            LogUtil.d("TAG", ": $value")
            return sharedPreference.putKey("chatBubbleColor", value)
        }

    var chatBubbleColorReciver: String
        get() : String {
            return sharedPreference.getKey("chatBubbleColorReciver")
        }
        set(value) {
            LogUtil.d("TAG", ": " + value)
            return sharedPreference.putKey("chatBubbleColorReciver", value)
        }
}