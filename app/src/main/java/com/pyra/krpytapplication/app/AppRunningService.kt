package com.pyra.krpytapplication.app

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.LogUtil
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.network.UrlHelper
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import isAppIsInBackground
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import java.net.URISyntaxException

class AppRunningService : Service() {

    companion object {
        private var socket: Socket? = null
        var sharedHelper: SharedHelper? = null
        var handler = Handler()
        lateinit var runnable: Runnable

        fun updateOffline() {
            socket?.let {
                if (it.connected()) {

                    val jsonObject = JSONObject()
                    jsonObject.put(
                        Constants.SocketKeys.USERNAME,
                        sharedHelper?.kryptKey.toString().toUpperCase()
                    )

                    sharedHelper?.isSocketUpdatedToOffline = true
                    LogUtil.d(" Socket ", " offline updated")
                    socket?.emit(UrlHelper.UPDATEOFFLINE, jsonObject, object : Ack {
                        override fun call(vararg args: Any?) {
                        }
                    })
                }
            }

        }

        fun updateOnline() {
            socket?.let {
                if (it.connected()) {

                    val jsonObject = JSONObject()
                    jsonObject.put(
                        Constants.SocketKeys.USERNAME,
                        sharedHelper?.kryptKey.toString().toUpperCase()
                    )

                    sharedHelper?.isSocketUpdatedToOffline = false
                    LogUtil.d(" Socket ", " online updated")
                    socket?.emit(UrlHelper.UPDATEONLINE, jsonObject, object : Ack {
                        override fun call(vararg args: Any?) {
                        }
                    })
                }
            }

        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sharedHelper = SharedHelper(this)
        initRunnable()
        handler.postDelayed(runnable, 1000)
        initSockets()
        return START_STICKY
    }

    private fun initRunnable() {
        runnable = Runnable {
            doAsync {
                if (isAppIsInBackground(this@AppRunningService) && SharedHelper(this@AppRunningService).onlineUpdated) {
//                    MyApp.xmppOperations.getInstance().updateOnlinePresence(false)
                    SharedHelper(this@AppRunningService).onlineUpdated = false

                } else {

                }
                handler.postDelayed(runnable, 5000)
            }
        }
    }


    private fun initSockets() {
        val opts = IO.Options()
        opts.forceNew = true
        opts.reconnection = false
//        opts.query = Constants.ApiKeys.AUTHORIZATION + "=" + sharedHelper.token
        try {
            socket = IO.socket(UrlHelper.SOCKETURL, opts)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        socket?.on(Socket.EVENT_DISCONNECT) {
            LogUtil.d(" Socket ", " DisConnected")
            initSockets()

        }

        socket?.on(Socket.EVENT_CONNECT) {
            LogUtil.d(" Socket ", " Connected")
//            initSockets()
        }

        socket?.let {
            if (!it.connected())
                socket?.connect()
        }

        runnable = Runnable {

            if (socket == null || !socket?.connected()!!) {
                initSockets()
            }
            val jsonObject = JSONObject()
            jsonObject.put(
                Constants.SocketKeys.USERNAME,
                sharedHelper?.kryptKey.toString().toUpperCase()
            )

            if (jsonObject.getString(Constants.SocketKeys.USERNAME) != "" &&
                jsonObject.getString(Constants.SocketKeys.USERNAME) != "null"
            ) {
                if (isAppIsInBackground(this@AppRunningService)) {

                    if (!sharedHelper?.isSocketUpdatedToOffline!!) {
                        updateOffline()

                    }
                    handler.removeCallbacks(runnable)
                    handler.postDelayed(runnable, 4000)

                } else {

                    updateOnline()

                    handler.removeCallbacks(runnable)
                    handler.postDelayed(runnable, 4000)
                }

            } else {
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 4000)
            }
        }

        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 1000)

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

}