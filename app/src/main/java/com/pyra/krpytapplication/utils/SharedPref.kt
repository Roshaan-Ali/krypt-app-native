package com.pyra.krpytapplication.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

import com.google.gson.Gson
import com.pyra.krpytapplication.BuildConfig

class SharedPref(private val context: Context) {
    private var sharedPreferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    companion object {
        const val Cache = "Cache"
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    }

    fun putKey(key: String, value: String) {

        sharedPreferences = if (BuildConfig.DEBUG) {
            context.getSharedPreferences(Cache, MODE_PRIVATE)
        } else {
            EncryptedSharedPreferences.create(
                Cache,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        editor = sharedPreferences!!.edit()
        editor!!.putString(key, value)
        editor!!.apply()
    }

    fun getKey(Key: String): String {
        sharedPreferences = if (BuildConfig.DEBUG) {
            context.getSharedPreferences(Cache, MODE_PRIVATE)
        } else {
            EncryptedSharedPreferences.create(
                Cache,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        return sharedPreferences!!.getString(Key, "").toString()
    }

    fun getKey(Key: String, defaultvalue: String): String? {
        sharedPreferences = if (BuildConfig.DEBUG) {
            context.getSharedPreferences(Cache, MODE_PRIVATE)
        } else {
            EncryptedSharedPreferences.create(
                Cache,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        return sharedPreferences!!.getString(Key, defaultvalue)
    }

    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences = if (BuildConfig.DEBUG) {
            context.getSharedPreferences(Cache, MODE_PRIVATE)
        } else {
            EncryptedSharedPreferences.create(
                Cache,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        editor = sharedPreferences!!.edit()
        editor!!.putBoolean(key, value)
        editor!!.apply()
    }

    fun getBoolean(Key: String): Boolean {
        sharedPreferences = if (BuildConfig.DEBUG) {
            context.getSharedPreferences(Cache, MODE_PRIVATE)
        } else {
            EncryptedSharedPreferences.create(
                Cache,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        return sharedPreferences!!.getBoolean(Key, false)
    }

    fun getBooleanDefaultTrue(Key: String): Boolean {
        sharedPreferences = if (BuildConfig.DEBUG) {
            context.getSharedPreferences(Cache, MODE_PRIVATE)
        } else {
            EncryptedSharedPreferences.create(
                Cache,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        return sharedPreferences!!.getBoolean(Key, true)
    }

    fun putInt(key: String, value: Int) {
        sharedPreferences = if (BuildConfig.DEBUG) {
            context.getSharedPreferences(Cache, MODE_PRIVATE)
        } else {
            EncryptedSharedPreferences.create(
                Cache,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        editor = sharedPreferences!!.edit()
        editor!!.putInt(key, value)
        editor!!.apply()
    }

    fun getInt(key: String): Int {
        sharedPreferences = if (BuildConfig.DEBUG) {
            context.getSharedPreferences(Cache, MODE_PRIVATE)
        } else {
            EncryptedSharedPreferences.create(
                Cache,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        return sharedPreferences!!.getInt(key, 0)
    }

    fun saveObjectToSharedPreference(serializedObjectKey: String, `object`: Any) {
        sharedPreferences = if (BuildConfig.DEBUG) {
            context.getSharedPreferences(Cache, MODE_PRIVATE)
        } else {
            EncryptedSharedPreferences.create(
                Cache,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        val sharedPreferencesEditor = sharedPreferences!!.edit()
        val gson = Gson()
        val serializedObject = gson.toJson(`object`)
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject)
        sharedPreferencesEditor.apply()
    }

    fun <GenericClass> getSavedObjectFromPreference(
        preferenceKey: String,
        classType: Class<GenericClass>
    ): GenericClass? {
        sharedPreferences = if (BuildConfig.DEBUG) {
            context.getSharedPreferences(Cache, MODE_PRIVATE)
        } else {
            EncryptedSharedPreferences.create(
                Cache,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        if (sharedPreferences!!.contains(preferenceKey)) {
            val gson = Gson()
            return gson.fromJson(sharedPreferences!!.getString(preferenceKey, ""), classType)
        }
        return null
    }

    fun removeValues() {
        sharedPreferences = if (BuildConfig.DEBUG) {
            context.getSharedPreferences(Cache, MODE_PRIVATE)
        } else {
            EncryptedSharedPreferences.create(
                Cache,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        sharedPreferences?.edit()?.clear()?.apply()
    }
}