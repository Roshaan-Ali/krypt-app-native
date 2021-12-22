package com.simplemobiletools.calculator

import android.app.Application
import android.util.Log
import com.simplemobiletools.commons.extensions.checkUseEnglish

open class CalculatorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("CalculatorApp", "Calculator App onCreate")
        checkUseEnglish()
    }
}
