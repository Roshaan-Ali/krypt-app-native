package com.pyra.krpytapplication.utils

import android.util.Log
import com.pyra.krpytapplication.BuildConfig

class LogUtil {
    companion object {

        fun e(tag: String?, msg: String?) {
            if (BuildConfig.DEBUG)
                msg?.let {
                    Log.e(tag, msg)
                }
        }

        fun d(tag: String?, msg: String?) {
            if (BuildConfig.DEBUG)
                msg?.let {
                    Log.d(tag, msg)
                }
        }

        fun i(tag: String?, msg: String?) {
            if (BuildConfig.DEBUG)
                msg?.let {
                    Log.i(tag, msg)
                }
        }

        fun v(tag: String?, msg: String?) {
            if (BuildConfig.DEBUG)
                msg?.let {
                    Log.v(tag, msg)
                }
        }

        fun getStackTraceString(e: Exception?) {
            if (BuildConfig.DEBUG)
                e?.let {
                    Log.getStackTraceString(e)
                }
        }
    }

}