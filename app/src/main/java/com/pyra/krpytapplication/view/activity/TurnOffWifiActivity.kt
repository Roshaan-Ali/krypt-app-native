package com.pyra.krpytapplication.view.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pyra.krpytapplication.R
import isWifiTurnOn
import kotlinx.android.synthetic.main.dialog_turnoff_wifi.*

class TurnOffWifiActivity : AppCompatActivity(){

    private lateinit var reciver: BroadcastReceiver
    private var intentFilter: IntentFilter = IntentFilter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_turnoff_wifi)

        tryAgain.setOnClickListener {
            checkWifiState()
        }
    }


    override fun onResume() {
        super.onResume()


        reciver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                checkWifiState()
            }
        }
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED")
        registerReceiver(reciver, intentFilter)
    }

    private fun checkWifiState() {

        if(!isWifiTurnOn(this)){
            finish()
        }
    }


}