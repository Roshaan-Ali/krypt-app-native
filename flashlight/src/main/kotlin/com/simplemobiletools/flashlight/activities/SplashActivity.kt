package com.simplemobiletools.flashlight.activities

import android.content.Intent
import com.simplemobiletools.commons.activities.BaseSplashActivity

class SplashActivity : BaseSplashActivity() {
    override fun initActivity() {
        startActivity(Intent(this, MainFlashActivity::class.java))
        finish()
    }
}
