package com.simplemobiletools.flashlight

import android.app.Application
import com.simplemobiletools.commons.extensions.checkUseEnglish

class FlashlightApp : Application() {
    override fun onCreate() {
        super.onCreate()
        checkUseEnglish()
    }
}
