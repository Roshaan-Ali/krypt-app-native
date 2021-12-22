package com.pyra.krpytapplication.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.view.activity.ChatActivity
import java.util.*

class NotificationUtils(var context: Context) {

    private var notificationManager: NotificationManager? = null
    private val DEFAULT_NOTIFICATION_ICON: Int = R.mipmap.ic_launcher

    fun notificationIncomingCallVoice(
        docName: String,
        body: String,
        intent: Intent,
        bundle: Bundle,
        uniqueId: UUID
    ) {

        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        val intentActivity = PendingIntent.getActivity(
            context,
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification =
            NotificationCompat.Builder(context, Constants.NotificationIds.INCOMING_CALL_CHANNEL_ID)
                .setContentTitle(docName)
                .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
                .setContentText(body)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .setContentIntent(intentActivity)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setFullScreenIntent(intentActivity, true)
                .setUsesChronometer(false)
                .setOngoing(false)
                .setSmallIcon(DEFAULT_NOTIFICATION_ICON)


        val actionCall = Intent(context, CallNotificationReceiver::class.java)
        actionCall.action = Constants.NotificationActions.ACCEPT_CALL
        actionCall.putExtra(Constants.NotificationIntentValues.UNIQUEID,uniqueId.toString())
        actionCall.putExtras(bundle)

        val actionReject = Intent(context, CallNotificationReceiver::class.java)
        actionReject.action = Constants.NotificationActions.REJECT_CALL
        actionReject.putExtra(Constants.NotificationIntentValues.UNIQUEID,uniqueId.toString())
        actionReject.putExtras(bundle)

        val pendingActionCall = PendingIntent.getBroadcast(
            context,
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            actionCall,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pendingActionReject = PendingIntent.getBroadcast(
            context,
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            actionReject,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        notification.addAction(
            R.drawable.call_icon,
            Constants.NotificationActions.ACCEPT_CALL,
            pendingActionCall
        )
        notification.addAction(
            R.drawable.call_end,
            Constants.NotificationActions.REJECT_CALL,
            pendingActionReject
        )

        notificationManager?.notify(
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            notification.build()
        )
    }

    fun notificationIncomingCallVideo(
        docName: String,
        body: String,
        intent: Intent,
        bundle: Bundle,
        uniqueId: UUID
    ) {

        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        val intentActivity = PendingIntent.getActivity(
            context,
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        var notification: NotificationCompat.Builder? = null


        notification =
            NotificationCompat.Builder(context, Constants.NotificationIds.INCOMING_CALL_CHANNEL_ID)
                .setContentTitle(docName)
                .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
                .setContentText(body)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .setContentIntent(intentActivity)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setFullScreenIntent(intentActivity, true)
                .setUsesChronometer(false)
                .setOngoing(false)
                .setSmallIcon(DEFAULT_NOTIFICATION_ICON)

//        if (BaseUtils.isAppBackground(context)) {
//
//        } else {
//            notification = NotificationCompat.Builder(context, Constants.NotificationIds.INCOMING_CALL_CHANNEL_ID_NOSOUND)
//                    .setContentTitle(docName)
//                    .setContentText(body)
//                    .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
//                    .setAutoCancel(true)
//                    .setAutoCancel(false)
//                    .setContentIntent(intentActivity)
//                    .setFullScreenIntent(intentActivity, true)
//                    .setUsesChronometer(false)
//                    .setSmallIcon(DEFAULT_NOTIFICATION_ICON)
//        }


        val actionCall = Intent(context, CallNotificationReceiver::class.java)
        actionCall.action = Constants.NotificationActions.ACCEPT_CALL
        actionCall.putExtra(Constants.NotificationIntentValues.UNIQUEID,uniqueId.toString())
        actionCall.putExtras(bundle)

        val actionReject = Intent(context, CallNotificationReceiver::class.java)
        actionReject.putExtra(Constants.NotificationIntentValues.UNIQUEID,uniqueId.toString())
        actionReject.action = Constants.NotificationActions.REJECT_CALL

        val pendingActionCall = PendingIntent.getBroadcast(
            context,
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            actionCall,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pendingActionReject = PendingIntent.getBroadcast(
            context,
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            actionReject,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        notification.addAction(
            R.drawable.video_on,
            Constants.NotificationActions.ACCEPT_CALL,
            pendingActionCall
        )
        notification.addAction(
            R.drawable.call_end,
            Constants.NotificationActions.REJECT_CALL,
            pendingActionReject
        )



        notificationManager?.notify(
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            notification.build()
        )
    }

    fun notificationEndCall(docName: String) {

        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification =
            NotificationCompat.Builder(context, Constants.NotificationIds.HANGUP_CALL_CHANNEL_ID)
                .setContentTitle(docName)
                .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .setUsesChronometer(true)
                .setOngoing(false)
                .setSmallIcon(DEFAULT_NOTIFICATION_ICON)


        val actionEnd = Intent(context, CallNotificationReceiver::class.java)
        actionEnd.action = Constants.NotificationActions.END_CALL

        val pendingActionHangUp = PendingIntent.getBroadcast(
            context,
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            actionEnd,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        notification.addAction(
            R.drawable.call_end,
            Constants.NotificationActions.END_CALL,
            pendingActionHangUp
        )

        notificationManager?.notify(
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            notification.build()
        )
    }

    fun notificationHangupCall(docName: String, body: String) {

        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification =
            NotificationCompat.Builder(context, Constants.NotificationIds.HANGUP_CALL_CHANNEL_ID)
                .setContentTitle(docName)
                .setContentText(body)
                .setOngoing(false)
                .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .setSmallIcon(DEFAULT_NOTIFICATION_ICON)


        val actionEnd = Intent(context, CallNotificationReceiver::class.java)
        actionEnd.action = Constants.NotificationActions.HANGUP_CALL

        val pendingActionHangUp = PendingIntent.getBroadcast(
            context,
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            actionEnd,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        notification.addAction(
            R.drawable.call_end,
            Constants.NotificationActions.HANGUP_CALL,
            pendingActionHangUp
        )

        notificationManager?.notify(
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            notification.build()
        )
    }

    fun removeCallNotifications() {
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager?.cancel(Constants.NotificationIds.CALL_NOTIFICATION_ID)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun createChannel(context: Context) {

        var notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val callTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE).toString()
        val attributesCall = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val incomingCall = NotificationChannel(
                Constants.NotificationIds.INCOMING_CALL_CHANNEL_ID,
                Constants.NotificationIds.INCOMING_CALL_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            incomingCall.setSound(Uri.parse(callTone), attributesCall)
            notificationManager.createNotificationChannel(incomingCall)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun createChannelNoSound(context: Context) {

        var notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val callTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE).toString()
        val attributesCall = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val incomingCall = NotificationChannel(
                Constants.NotificationIds.INCOMING_CALL_CHANNEL_ID,
                Constants.NotificationIds.INCOMING_CALL_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            incomingCall.setSound(Uri.parse(callTone), attributesCall)
            notificationManager.createNotificationChannel(incomingCall)
        }
    }

    fun createMessageNotification(
        title: String,
        content: String,
        roomId: String,
        kryptId: String?,
        addedToContact: Boolean,
        name: String?
    ) {

        var intent = Intent(MyApp.getInstance().baseContext, ChatActivity::class.java)
            .putExtra(Constants.IntentKeys.ROOMID, roomId)
            .putExtra(Constants.IntentKeys.DISPLAY_NAME, name)
            .putExtra(Constants.IntentKeys.KRYPTKEY, kryptId)
            .putExtra(Constants.IntentKeys.IS_ADDED_TO_CONTACTS, addedToContact)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val intentActivity = PendingIntent.getActivity(
            context,
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)


        val notification =
            NotificationCompat.Builder(
                context,
                Constants.NotificationIds.INCOMING_MESSAGE_CHANNEL_ID
            )
                .setContentTitle("You have message to read")
                .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
//                .setContentText(content)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .setContentIntent(intentActivity)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
                .setSmallIcon(DEFAULT_NOTIFICATION_ICON)


        notificationManager?.notify (
            System.currentTimeMillis().toInt(),
            notification.build()
        )
    }


    fun removeNotification() {
       val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
       notificationManager.cancelAll()
    }








    //group Call










    fun groupNotificationIncomingCallVoice(
        docName: String,
        body: String,
        intent: Intent,
        bundle: Bundle) {

        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        val intentActivity = PendingIntent.getActivity(
            context,
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )



        val notification =
            NotificationCompat.Builder(context, Constants.NotificationIds.INCOMING_CALL_CHANNEL_ID)
                .setContentTitle(docName)
                .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
                .setContentText(body)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .setContentIntent(intentActivity)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setFullScreenIntent(intentActivity, true)
                .setUsesChronometer(false)
                .setOngoing(false)
                .setSmallIcon(DEFAULT_NOTIFICATION_ICON)


        val actionCall = Intent(context, GroupCallNotificationReceiver::class.java)
        actionCall.action = Constants.NotificationActions.ACCEPT_CALL
        actionCall.putExtras(bundle)

        val actionReject = Intent(context, GroupCallNotificationReceiver::class.java)
        actionReject.action = Constants.NotificationActions.REJECT_CALL
        actionReject.putExtras(bundle)

        val pendingActionCall = PendingIntent.getBroadcast(
            context,
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            actionCall,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pendingActionReject = PendingIntent.getBroadcast(
            context,
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            actionReject,
            PendingIntent.FLAG_UPDATE_CURRENT
        )


        notification.addAction(
            R.drawable.call_icon,
            Constants.NotificationActions.ACCEPT_CALL,
            pendingActionCall
        )
        notification.addAction(
            R.drawable.call_end,
            Constants.NotificationActions.REJECT_CALL,
            pendingActionReject
        )


        notificationManager?.notify(
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            notification.build()
        )
    }


    fun groupNotificationIncomingCallVideo(
        docName: String,
        body: String,
        intent: Intent,
        bundle: Bundle,
    ) {

        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        val intentActivity = PendingIntent.getActivity(
            context,
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        var notification: NotificationCompat.Builder? = null


        notification =
            NotificationCompat.Builder(context, Constants.NotificationIds.INCOMING_CALL_CHANNEL_ID)
                .setContentTitle(docName)
                .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
                .setContentText(body)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .setContentIntent(intentActivity)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setFullScreenIntent(intentActivity, true)
                .setUsesChronometer(false)
                .setOngoing(false)
                .setSmallIcon(DEFAULT_NOTIFICATION_ICON)

        val actionCall = Intent(context, GroupCallNotificationReceiver::class.java)
        actionCall.action = Constants.NotificationActions.ACCEPT_CALL
        actionCall.putExtras(bundle)

        val actionReject = Intent(context, GroupCallNotificationReceiver::class.java)
        actionReject.action = Constants.NotificationActions.REJECT_CALL

        val pendingActionCall = PendingIntent.getBroadcast(
            context,
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            actionCall,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pendingActionReject = PendingIntent.getBroadcast(
            context,
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            actionReject,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        notification.addAction(
            R.drawable.video_on,
            Constants.NotificationActions.ACCEPT_CALL,
            pendingActionCall
        )
        notification.addAction(
            R.drawable.call_end,
            Constants.NotificationActions.REJECT_CALL,
            pendingActionReject
        )



        notificationManager?.notify(
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            notification.build()
        )
    }

    fun groupNotificationEndCall(docName: String) {

        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification =
            NotificationCompat.Builder(context, Constants.NotificationIds.HANGUP_CALL_CHANNEL_ID)
                .setContentTitle(docName)
                .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .setUsesChronometer(true)
                .setOngoing(false)
                .setSmallIcon(DEFAULT_NOTIFICATION_ICON)


        val actionEnd = Intent(context, GroupCallNotificationReceiver::class.java)
        actionEnd.action = Constants.NotificationActions.END_CALL

        val pendingActionHangUp = PendingIntent.getBroadcast(
            context,
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            actionEnd,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        notification.addAction(
            R.drawable.call_end,
            Constants.NotificationActions.END_CALL,
            pendingActionHangUp
        )

        notificationManager?.notify(
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            notification.build()
        )
    }


    fun groupNotificationHangupCall(docName: String, body: String) {

        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification =
            NotificationCompat.Builder(context, Constants.NotificationIds.HANGUP_CALL_CHANNEL_ID)
                .setContentTitle(docName)
                .setContentText(body)
                .setOngoing(false)
                .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .setSmallIcon(DEFAULT_NOTIFICATION_ICON)


        val actionEnd = Intent(context, GroupCallNotificationReceiver::class.java)
        actionEnd.action = Constants.NotificationActions.HANGUP_CALL

        val pendingActionHangUp = PendingIntent.getBroadcast(
            context,
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            actionEnd,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        notification.addAction(
            R.drawable.call_end,
            Constants.NotificationActions.HANGUP_CALL,
            pendingActionHangUp
        )

        notificationManager?.notify(
            Constants.NotificationIds.CALL_NOTIFICATION_ID,
            notification.build()
        )
    }
}