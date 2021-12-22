package com.pyra.krpytapplication.view.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.LogUtil
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.app.AppRunningService
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.chat.ChatWorker
import isAppIsInBackground
import isMyServiceRunning
import isWifiTurnOn
import java.util.*

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {

    private lateinit var reciver: BroadcastReceiver
    private var intentFilter: IntentFilter = IntentFilter()
    private val sharedHelper by lazy {
        SharedHelper(this)
    }

    lateinit var timerBase: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SharedHelper(this).theme == "light") {
            setTheme(R.style.lightTheme)
        } else {
            setTheme(R.style.darkTheme)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        checkForService()

        checkForWifi()
        countDownForAutoLogout()
        if (sharedHelper.loggedIn) {
            val data = Data.Builder()
            data.putString("Type", "UserConnection")
            val connectRequest: OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<ChatWorker>()
                    .setInputData(data.build())
                    .build()

            WorkManager
                .getInstance(this)
                .enqueueUniqueWork(
                    "OneTimeUserConnect",
                    ExistingWorkPolicy.KEEP, connectRequest
                )

        }

        //update status
        Handler().postDelayed({
            MyApp.xmppOperations.getInstance().updateOnlinePresence(true)
            SharedHelper(this).onlineUpdated = true
        }, 2000)

        if (sharedHelper.showKryptScreen && sharedHelper.loggedIn) {
            val intent = Intent(this, PasswordActivity::class.java)
            intent.putExtra("kryptCode", sharedHelper.kryptKey)
            intent.putExtra("isBackEnabled", false)
//            startActivity(intent)
            sharedHelper.showKryptScreen = false
        }

        reciver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                checkForWifi()
            }
        }
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED")
        registerReceiver(reciver, intentFilter)

    }

    private fun checkForService() {

        if (!isMyServiceRunning(this, AppRunningService::class.java) && !isAppIsInBackground(this))
            startService(Intent(this, AppRunningService::class.java))

    }


    private fun checkForWifi() {
        if (isWifiTurnOn(this)) {
//            startActivity(Intent(this, TurnOffWifiActivity::class.java))
        }

    }


    fun countDownForAutoLogout() {

        val duration =
            sharedHelper.autoLogoutSavedTime.toLong().minus(Calendar.getInstance().time.time)
        timerBase = object : CountDownTimer(duration, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                var diff = millisUntilFinished
                val secondsInMilli: Long = 1000
                val minutesInMilli = secondsInMilli * 60
                val hoursInMilli = minutesInMilli * 60
                val daysInMilli = hoursInMilli * 24

                val elapsedDays = diff / daysInMilli
                diff %= daysInMilli

                val elapsedHours = diff / hoursInMilli
                diff %= hoursInMilli

                val elapsedMinutes = diff / minutesInMilli
                diff %= minutesInMilli

                val elapsedSeconds = diff / secondsInMilli
                // Log.e("CountDown","$elapsedDays days $elapsedHours hs $elapsedMinutes min $elapsedSeconds sec")
            }

            override fun onFinish() {
                LogUtil.e("CountDown", "Done")
                this.cancel()
                Intent(this@BaseActivity, PasswordActivity::class.java).apply {
                    putExtra("kryptCode", sharedHelper.kryptKey)
                    putExtra("isBackEnabled", false)
                    startActivity(this)
                }
                sharedHelper.showKryptScreen = false
            }
        }.start()

    }


    override fun onPause() {
        super.onPause()

        Handler(Looper.getMainLooper()).postDelayed({
            if (isAppIsInBackground(this))
                AppRunningService.updateOffline()
        }, 500)


        unregisterReceiver(reciver)
        Handler().postDelayed({
            val sharedHelper = SharedHelper(this)
            if (isAppIsInBackground(this)) {
                sharedHelper.showKryptScreen = true
            }
        }, 1000)

//        MyApp.xmppOperations.getInstance().updateOnlinePresence(false)
    }
}