package com.pyra.krpytapplication.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.annotation.RequiresApi
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessaging
import com.nicoqueijo.android.currencyconverter.kotlin.app.CurrencyApplication
import com.oriondev.moneywallet.WalletApp
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.LogUtil
import com.pyra.krpytapplication.chat.ChatWorker
import com.pyra.krpytapplication.chat.XMPPOperations
import com.pyra.krpytapplication.roomDb.AppDataBase
import com.pyra.krpytapplication.videocallutils.events.AGEventHandler
import com.pyra.krpytapplication.videocallutils.events.EngineConfig
import com.pyra.krpytapplication.videocallutils.events.MyEngineEventHandler
import com.simplemobiletools.clock.ClockApp
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import io.agora.rtc.RtcEngine
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class MyApp : WalletApp() {

    override fun onCreate() {
        super.onCreate()

        //disable ssl checking
        disableSSLCertificateChecking()

        EmojiManager.install(IosEmojiProvider())

        val myPeriodicWorkRequest =
            PeriodicWorkRequestBuilder<ChatWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "Notification",
            ExistingPeriodicWorkPolicy.KEEP,
            myPeriodicWorkRequest
        )
        println("App created")

        initXmpp()
//        val data = Data.Builder()
//        data.putString("Type", "AbstractConnection")
//        val abstractConnectRequest: OneTimeWorkRequest =
//            OneTimeWorkRequestBuilder<ChatWorker>()
//                .setInputData(data.build())
//                .build()

//        WorkManager
//            .getInstance(this)
//            .enqueueUniqueWork(
//                "OneTimeAbstractConnect",
//                ExistingWorkPolicy.KEEP, abstractConnectRequest
//            )

//        startService(Intent(this, ChatRoomConnection::class.java))

        subscribeToTopic()
        createRtcEngine()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    private fun createRtcEngine() {
        val context = applicationContext
        val appId = context.getString(R.string.agora_app_id)
        if (TextUtils.isEmpty(appId)) {
            throw RuntimeException("NEED TO use your App ID, get your own ID at https://dashboard.agora.io/")
        }
        mEventHandler = MyEngineEventHandler()
        mRtcEngine = try {
            // Creates an RtcEngine instance
            RtcEngine.create(context, appId, mEventHandler)
        } catch (e: Exception) {
            throw RuntimeException(
                "NEED TO check rtc sdk init fatal error\n" + LogUtil.getStackTraceString(e)
            )
        }

        /*
          Sets the channel profile of the Agora RtcEngine.
          The Agora RtcEngine differentiates channel profiles and applies different optimization
          algorithms accordingly. For example, it prioritizes smoothness and low latency for a
          video call, and prioritizes video quality for a video broadcast.
         */mRtcEngine?.setChannelProfile(io.agora.rtc.Constants.CHANNEL_PROFILE_COMMUNICATION)
        // Enables the video module.
        mRtcEngine?.enableVideo()
        /*
          Enables the onAudioVolumeIndication callback at a set time interval to report on which
          users are speaking and the speakers' volume.
          Once this method is enabled, the SDK returns the volume indication in the
          onAudioVolumeIndication callback at the set time interval, regardless of whether any user
          is speaking in the channel.
         */mRtcEngine?.enableAudioVolumeIndication(200, 3, false)
        mConfig = EngineConfig()
    }

    private fun initXmpp() {
        LogUtil.d("Fcm XMPP", "Called")
        xmppOperations = XMPPOperations.getInstance()
    }

    private fun subscribeToTopic() {

        FirebaseMessaging.getInstance().subscribeToTopic("heartbeat").addOnSuccessListener {
            LogUtil.d("Susbscribed", "HeartBeat")
        }

    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        instance = this
    }

    companion object {
        lateinit var xmppOperations: XMPPOperations
        private val TAG: String = MyApp::class.java.simpleName
        private var instance: MyApp? = null
        private var requestQueue: RequestQueue? = null

        private val callTone =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE).toString()

        private val messageTone =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString()

        private var mRtcEngine: RtcEngine? = null
        private var mConfig: EngineConfig? = null
        private var mEventHandler: MyEngineEventHandler? = null

        @Synchronized
        fun getInstance(): MyApp {
            return instance as MyApp
        }

    }

    fun rtcEngine(): RtcEngine? {
        return mRtcEngine
    }

    fun config(): EngineConfig? {
        return mConfig
    }


    fun addEventHandler(handler: AGEventHandler?) {
        mEventHandler?.addEventHandler(handler)
    }

    fun remoteEventHandler(handler: AGEventHandler?) {
        mEventHandler?.removeEventHandler(handler)
    }

    fun getAppDatabase(): AppDataBase? {
        return AppDataBase.getInstance(this)
    }

    fun getRequestQueue(): RequestQueue? {

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(instance)

        return requestQueue
    }

    fun <T> addrequestToQueue(request: Request<T>) {
        request.tag = TAG
        getRequestQueue()?.add(request)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {

        registerReceiver(
            TransferNetworkLossHandler.getInstance(applicationContext), IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION
            )
        )

        val attributesCall = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build()

        val messageAttributesCall = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build()

        val notificationManager =
            instance?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val incomingCall = NotificationChannel(
            Constants.NotificationIds.INCOMING_CALL_CHANNEL_ID,
            Constants.NotificationIds.INCOMING_CALL_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        val chatGroup = NotificationChannel(
            Constants.NotificationIds.HANGUP_CALL_CHANNEL_ID,
            Constants.NotificationIds.HANGUP_CALL_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )

        val messageGroup = NotificationChannel(
            Constants.NotificationIds.INCOMING_MESSAGE_CHANNEL_ID,
            Constants.NotificationIds.INCOMING_MESSAGE_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )

        messageGroup.setSound(Uri.parse(messageTone), messageAttributesCall)

        incomingCall.setSound(Uri.parse(callTone), attributesCall)
        chatGroup.setSound(null, null)

        notificationManager.createNotificationChannel(incomingCall)
        notificationManager.createNotificationChannel(chatGroup)
        notificationManager.createNotificationChannel(messageGroup)
    }

    private fun disableSSLCertificateChecking() {
        val trustAllCerts: Array<TrustManager> = arrayOf(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(arg0: Array<X509Certificate?>?, arg1: String?) {
                // Not implemented
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(arg0: Array<X509Certificate?>?, arg1: String?) {
                // Not implemented
            }

            override fun getAcceptedIssuers(): Array<X509Certificate>? {
                return null
            }
        })
        try {
            val sc: SSLContext = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

}
