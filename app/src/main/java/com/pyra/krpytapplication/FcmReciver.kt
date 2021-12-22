package com.pyra.krpytapplication

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pyra.krpytapplication.utils.*
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.chat.ChatWorker
import com.pyra.krpytapplication.chat.GetGroupMessagesWorker
import com.pyra.krpytapplication.notification.NotificationUtils
import com.pyra.krpytapplication.repositories.implementations.ChatListRepository
import com.pyra.krpytapplication.roomDb.AppDataBase
import com.pyra.krpytapplication.roomDb.dao.ChatMessagesDao
import com.pyra.krpytapplication.roomDb.entity.ChatMessagesSchema
import com.pyra.krpytapplication.rxbus.RxBusNotification
import com.pyra.krpytapplication.videocallutils.view.activity.GroupCallActivity
import com.pyra.krpytapplication.view.activity.VideoCallActivity
import getRoomId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*

class FcmReciver : FirebaseMessagingService() {

    var chatListRepository: ChatListRepository? = null

    private lateinit var database: ChatMessagesDao


    private val chatMessagesSchema by lazy {
        ChatMessagesSchema()
    }

    val scope = CoroutineScope(Dispatchers.IO)

    private val uniqueId by lazy {
        UUID.randomUUID()
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        chatListRepository = ChatListRepository.getInstance((application as MyApp).getAppDatabase())

        startChatWorkerService()

        LogUtil.e("Fcm Message Recived", "recived")
        val jsonObject = JSONObject(p0.data as Map<*, *>)
        LogUtil.e("Fcm Message Recived", jsonObject.toString())
        if (jsonObject.has("type"))
            when {
                jsonObject["type"] == "call_notification" -> {

                    val jsonObjectBody = JSONObject(jsonObject["your_custom_data_key"].toString())

                    if (jsonObjectBody.has("type") && jsonObjectBody["type"] == Constants.ChatTypes.ENDCALL
                        &&
                        jsonObjectBody.has("isGroup") && jsonObjectBody["isGroup"] == "1"
                    ) {
//                        endCall(jsonObjectBody)
                    } else if (jsonObjectBody.has("callType") && jsonObjectBody.has("type")
                        && jsonObjectBody["type"] == "create_call"
                        &&
                        jsonObjectBody.has("isGroup") && jsonObjectBody["isGroup"] == "1"
                    ) {
                        if (jsonObjectBody["callType"] == Constants.ChatTypes.VOICE_CALL ||
                            jsonObjectBody["callType"] == Constants.ChatTypes.VIDEO_CALL
                        )
                            incomingGroupCall(jsonObjectBody)
                    } else
//                            &&
//                            jsonObjectBody.has("isGroup") && jsonObjectBody["isGroup"] == "0"

                        if (jsonObjectBody.has("type") && jsonObjectBody["type"] == Constants.ChatTypes.ENDCALL

                        ) {
                            endCall(jsonObjectBody)
                        } else if (jsonObjectBody.has("callType") && jsonObjectBody.has("type")
                            && jsonObjectBody["type"] == "create_call"
                            &&
                            jsonObjectBody.has("isGroup") && jsonObjectBody["isGroup"] == "0"
                        ) {
                            if (jsonObjectBody["callType"] == Constants.ChatTypes.VOICE_CALL ||
                                jsonObjectBody["callType"] == Constants.ChatTypes.VIDEO_CALL
                            )
                                incomingCall(jsonObjectBody)
                        }

                }

            }

    }

    private fun startChatWorkerService() {

        val data = Data.Builder()
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

//        startService(Intent(this, XmppService::class.java))
    }


    private fun startGroupChatWorkerService() {

        val data = Data.Builder()
        val connectRequest: OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<GetGroupMessagesWorker>()
                .setInputData(data.build())
                .build()

        WorkManager
            .getInstance(this)
            .enqueueUniqueWork(
                "GroupRequest",
                ExistingWorkPolicy.KEEP, connectRequest
            )
    }

    private fun incomingCall(jsonObject: JSONObject) {

        val bundle = Bundle()

        scope.launch {

            database = AppDataBase.getInstance(applicationContext)?.chatMessagesDao()!!
            database.insertChatMessage(
                chatMessagesSchema.copy(
                    messageId = uniqueId.toString(),
                    messageTime = System.currentTimeMillis().toString(),
                    message = applicationContext.getString(R.string.missedCall),
                    messageType = MessageType.MISSEDCALL.toMessageString(),
                    kryptId = jsonObject["fromUserName"].toString(),
                    roomId = getRoomId(
                        applicationContext,
                        jsonObject["fromUserName"].toString()
                    ).toUpperCase(
                        Locale.ROOT
                    ),
                    acceptedCall = 0,
                    rejectedCall = 0,
                    missedCall = 1
                )
            )
        }

        Coroutine.iOWorker {

            val data = chatListRepository?.getRoomData(jsonObject["fromUserName"].toString())

            Coroutine.mainWorker {
                bundle.putString(
                    Constants.NotificationIntentValues.CALL_TYPE,
                    jsonObject["callType"].toString()
                )

                bundle.putString(
                    Constants.NotificationIntentValues.CALL_FROM,
                    Constants.ChatTypes.INCOMING_CALL
                )
                bundle.putString(
                    Constants.NotificationIntentValues.CHANNEL_ID,
                    jsonObject["fromUserName"].toString()
                )
                bundle.putString(
                    Constants.NotificationIntentValues.TO_ID,
                    jsonObject["fromUserName"].toString()
                )
                bundle.putString(
                    Constants.NotificationIntentValues.FROM_ID,
                    jsonObject["toUserName"].toString()
                )

                bundle.putString(Constants.NotificationIntentValues.UNIQUEID, uniqueId.toString())

                if (data?.roomName != "") {
                    bundle.putString(
                        Constants.NotificationIntentValues.NAME,
                        data?.roomName
                    )
                } else {
                    bundle.putString(
                        Constants.NotificationIntentValues.NAME,
                        jsonObject["fromUserName"].toString()
                    )

                }

                bundle.putString(Constants.NotificationIntentValues.IMAGE, data?.roomImage)
                bundle.putString(Constants.NotificationIntentValues.ID, "id")


                val intent = Intent(this, VideoCallActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtras(bundle)
                applicationContext.startActivity(intent)
                setIncomingNotification(jsonObject, intent, bundle)
            }
        }

    }

    private fun incomingGroupCall(jsonObject: JSONObject) {

        val bundle = Bundle()

        Coroutine.iOWorker {

            val data = chatListRepository?.getRoomData(jsonObject["fromUserName"].toString())

            Coroutine.mainWorker {


                bundle.putString(
                    Constants.NotificationIntentValues.CALL_TYPE,
                    jsonObject["callType"].toString()
                )

                bundle.putString(
                    Constants.NotificationIntentValues.CALL_FROM,
                    Constants.ChatTypes.INCOMING_CALL
                )

                bundle.putString(
                    Constants.NotificationIntentValues.CHANNEL_ID,
                    jsonObject["channelName"].toString()
                )

                bundle.putString(
                    Constants.IntentKeys.ROOMID,
                    jsonObject["groupId"].toString()
                )

                bundle.putString(
                    Constants.NotificationIntentValues.NAME,
                    data?.roomName
                )

                bundle.putString(Constants.NotificationIntentValues.IMAGE, data?.roomImage)


                val intent = Intent(this, GroupCallActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtras(bundle)
                applicationContext.startActivity(intent)
                setIncomingGroupNotification(jsonObject, intent, bundle)
            }
        }

    }

    private fun setIncomingGroupNotification(
        jsonObject: JSONObject,
        intent: Intent,
        bundle: Bundle
    ) {

        val notificationUtils = NotificationUtils(this)
        if (jsonObject["callType"].toString() == Constants.ChatTypes.VOICE_CALL)

            notificationUtils.groupNotificationIncomingCallVoice(
                "Incoming Voice call",
                intent.getStringExtra(Constants.NotificationIntentValues.NAME)!!,
                intent,
                bundle
            )
        else if (jsonObject["callType"].toString() == Constants.ChatTypes.VIDEO_CALL)

            notificationUtils.groupNotificationIncomingCallVideo(
                "Incoming Video call",
                intent.getStringExtra(Constants.NotificationIntentValues.NAME)!!,
                intent, bundle
            )

    }

    private fun setIncomingNotification(
        jsonObject: JSONObject,
        intent: Intent,
        bundle: Bundle
    ) {

        val notificationUtils = NotificationUtils(this)
        if (jsonObject["callType"].toString() == Constants.ChatTypes.VOICE_CALL)
            notificationUtils.notificationIncomingCallVoice(
                "Incoming Voice call",
                intent.getStringExtra(Constants.NotificationIntentValues.NAME)!!,
                intent,
                bundle, uniqueId
            )
        else if (jsonObject["callType"].toString() == Constants.ChatTypes.VIDEO_CALL)
            notificationUtils.notificationIncomingCallVideo(
                "Incoming Video call",
                intent.getStringExtra(Constants.NotificationIntentValues.NAME)!!,
                intent, bundle, uniqueId
            )
    }

    private fun endCall(jsonObject: JSONObject) {
        NotificationUtils(this).removeCallNotifications()
        RxBusNotification.send(Constants.EventBusKeys.END_CALL)
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        LogUtil.d("token", p0)
        saveToken(p0)
    }

    private fun saveToken(token: String) {
        val sharedHelper = SharedHelper(this)
        sharedHelper.firebaseToken = token
    }

    private fun sampleNotification() {
        LogUtil.d("Workmanager ", "notification start running")
        val manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "task_channel"
        val channelName = "task_name"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
        val builder =
            NotificationCompat.Builder(applicationContext, channelId)
                .setContentTitle("test")
                .setContentText("test Content")
                .setSmallIcon(R.drawable.ic_launcher_background)
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        val runningProcesses = am.runningAppProcesses
        if (runningProcesses != null)
            for (processInfo in runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.packageName) {
                            isInBackground = false
                        }
                    }
                }
            }
        return isInBackground
    }

}