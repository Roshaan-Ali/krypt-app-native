package com.pyra.krpytapplication.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.roomDb.AppDataBase
import com.pyra.krpytapplication.roomDb.dao.ChatMessagesDao
import com.pyra.krpytapplication.rxbus.RxBusNotification
import com.pyra.krpytapplication.view.activity.VideoCallActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CallNotificationReceiver : BroadcastReceiver() {


    private lateinit var database: ChatMessagesDao


    val scope = CoroutineScope(Job() + Dispatchers.IO)

    private lateinit var uniqueId: String


    override fun onReceive(p0: Context?, p1: Intent?) {
        scope.launch {
            database = AppDataBase.getInstance(p0!!)?.chatMessagesDao()!!

        }
        uniqueId = p1?.getStringExtra(Constants.NotificationIntentValues.UNIQUEID) ?: ""
        scope.launch {
            database.deletecallStatus(uniqueId)
        }
        getIntentAction(p1, p0)
    }

    private fun getIntentAction(p1: Intent?, p0: Context?) {
        p1?.action?.let {
            when (it) {

                Constants.NotificationActions.ACCEPT_CALL -> {

                    if (SharedHelper(p0!!).currentScreen == "") {
                        p1.extras?.let { bundle ->

                            val intent = Intent(p0, VideoCallActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            intent.putExtras(bundle)
                            p0.startActivity(intent)
                        }
                        Handler().postDelayed(
                            {
                                NotificationUtils(p0).removeCallNotifications()
                                RxBusNotification.send(Constants.EventBusKeys.ACCEPT_CALL)
                            },
                            2000
                        )
                    } else {
                        RxBusNotification.send(Constants.EventBusKeys.ACCEPT_CALL)
                    }


                }
                Constants.NotificationActions.REJECT_CALL -> {
                    scope.launch {
                        database.deletecallStatus(uniqueId)
                    }
                    NotificationUtils(p0!!).removeCallNotifications()
                    RxBusNotification.send(Constants.EventBusKeys.REJECT_CALL)
                }
                Constants.NotificationActions.END_CALL -> {
                    scope.launch {
                        database.deletecallStatus(uniqueId)
                    }
                    NotificationUtils(p0!!).removeCallNotifications()
                    RxBusNotification.send(Constants.EventBusKeys.END_CALL)
                }
                Constants.NotificationActions.HANGUP_CALL -> {
                    scope.launch {
                        database.deletecallStatus(uniqueId)
                    }
                    NotificationUtils(p0!!).removeCallNotifications()
                    RxBusNotification.send(Constants.EventBusKeys.HANGUP_CALL)
                }

                else -> {
                }
            }
        }
    }

}