package com.pyra.krpytapplication.view.activity

import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.LogUtil
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.utils.loadImage
import com.pyra.krpytapplication.notification.NotificationUtils
import com.pyra.krpytapplication.roomDb.AppDataBase
import com.pyra.krpytapplication.roomDb.entity.ChatMessagesSchema
import com.pyra.krpytapplication.rxbus.RxBusNotification
import com.pyra.krpytapplication.viewmodel.CallViewModel
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.layout_audio_call.*
import kotlinx.android.synthetic.main.layout_video_call.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class VideoCallActivity : BaseActivity(), LifecycleOwner {

    private var isMute: Boolean = false
    private var isVideoMute: Boolean = false
    private var isFrontCamera: Boolean = false

    private var isSpeakerOnAudio: Boolean = false

    private var mRtcEngine: RtcEngine? = null

    private var callType: String = ""
    private var incomingOutGoing: String = ""
    private var channelId: String = ""
    private var toId: String = ""
    private var fromId: String = ""
    private var doctorname: String = ""
    private var chatId: String = ""
    private var doctorImage: String = ""
    private var roomId = ""

    private var callViewModel: CallViewModel? = null
    private lateinit var notificationEventListner: Disposable

    var ringTone: Ringtone? = null
    var mPlayer: MediaPlayer? = null

    var incomingConstraintSet: ConstraintSet? = null
    var acceptedConstraintSet: ConstraintSet? = null

    private var isCallAccepted = false
    private var isCallRejected = false

    private val database by lazy {

        AppDataBase.getInstance(this)?.chatMessagesDao()
    }

    private val chatMessagesSchema by lazy {
        ChatMessagesSchema()
    }

    val scope = CoroutineScope(Job() + Dispatchers.IO)

    private var uniqueId = ""


    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        // Tutorial Step 1
        override fun onFirstRemoteVideoDecoded(
            uid: Int,
            width: Int,
            height: Int,
            elapsed: Int
        ) { // Tutorial Step 5
            runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            runOnUiThread {
                stopRingtone()
                stopEarpiceRingtone()
                onCallAccepted()
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) { // Tutorial Step 7
            runOnUiThread { onRemoteUserLeft() }
        }

        override fun onUserMuteVideo(uid: Int, muted: Boolean) { // Tutorial Step 10
            runOnUiThread { onRemoteUserVideoMuted(uid, muted) }
        }

    }

    override fun onResume() {
        super.onResume()
        SharedHelper(this).currentScreen = "VideoCallActivity"
    }

    override fun onPause() {
        super.onPause()
        SharedHelper(this).currentScreen = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_call_new)
        callViewModel = ViewModelProvider(this).get(CallViewModel::class.java)

        initListener()
        getIntentValues()

    }

    private fun setUi() {

        if (callType == Constants.ChatTypes.VOICE_CALL) {
            if (incomingOutGoing == Constants.ChatTypes.INCOMING_CALL) {
                setIncomingVoice()
//                startRigtone()
            } else if (incomingOutGoing == Constants.ChatTypes.OUTGOING_CALL) {
                setOutgoingVoice()
            }

        } else if (callType == Constants.ChatTypes.VIDEO_CALL) {
            if (incomingOutGoing == Constants.ChatTypes.INCOMING_CALL) {
                setIncomingVideo()
//                startRigtone()
            } else if (incomingOutGoing == Constants.ChatTypes.OUTGOING_CALL) {
                setOutgoingVideo()
            }

        }

    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------
    private fun setOutgoingVideo() {

        callLayout.visibility = View.GONE
        videoLayout.visibility = View.VISIBLE

        groupCommonVideo.visibility = View.VISIBLE
        groupIncomingVideo.visibility = View.GONE
        groupOutGoingVideo.visibility = View.VISIBLE

        startEarpeiceRingtone()


    }

    private fun setOutgoingVoice() {

        callLayout.visibility = View.VISIBLE
        videoLayout.visibility = View.GONE

        groupOutGoing.visibility = View.VISIBLE
        groupInComing.visibility = View.GONE
        startEarpeiceRingtone()
    }

    private fun setIncomingVideo() {

        callLayout.visibility = View.GONE
        videoLayout.visibility = View.VISIBLE

        groupCommonVideo.visibility = View.VISIBLE
        groupIncomingVideo.visibility = View.VISIBLE
        groupOutGoingVideo.visibility = View.GONE

    }

    private fun setIncomingVoice() {
        callLayout.visibility = View.VISIBLE
        videoLayout.visibility = View.GONE

        groupOutGoing.visibility = View.GONE
        groupInComing.visibility = View.VISIBLE

    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------
    private fun getIntentValues() {

        intent.extras?.let {
            callType = it.getString(Constants.NotificationIntentValues.CALL_TYPE, "")
            incomingOutGoing = it.getString(Constants.NotificationIntentValues.CALL_FROM, "")
            channelId = it.getString(Constants.NotificationIntentValues.CHANNEL_ID, "")
            toId = it.getString(Constants.NotificationIntentValues.TO_ID, "")
            fromId = it.getString(Constants.NotificationIntentValues.FROM_ID, "")
            doctorname = it.getString(Constants.NotificationIntentValues.NAME, "")
            roomId = it.getString(Constants.IntentKeys.ROOMID, "")
            chatId = it.getString(Constants.NotificationIntentValues.ID, "")
            doctorImage = it.getString(Constants.NotificationIntentValues.IMAGE, "")
            uniqueId = it.getString(Constants.NotificationIntentValues.UNIQUEID, "")
        }

        doctoreName.text = doctorname
        doctoreNameVideo.text = doctorname

        audioProfielImage.loadImage(doctorImage)
        profileImage.loadImage(doctorImage)

//        if (doctorname == "") {
        getUserDetails()
//        }

        askPermission()
        setUi()
    }

    private fun getUserDetails() {

        if (doctorname == "")
            callViewModel?.userName?.observe(this, Observer {

                if (it == "") {
                    doctoreName.text = toId
                    doctoreNameVideo.text = toId

                } else {
                    doctoreName.text = it
                    doctoreNameVideo.text = it
                }
            })

        callViewModel?.userImage?.observe(this, Observer {
            if (it != "") {

                audioProfielImage.loadImage(it)
                profileImage.loadImage(it)
            }
        })

        callViewModel?.getUserDetails(toId)

    }

    private fun askPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (callType == Constants.ChatTypes.VOICE_CALL) {
                requestPermissions(
                    Constants.Permission.AUDIO_CALL_PERMISSION_LIST,
                    Constants.Permission.AUDIO_CALL_PERMISSION
                )
            } else if (callType == Constants.ChatTypes.VIDEO_CALL) {
                requestPermissions(
                    Constants.Permission.VIDEO_CALL_PERMISSION_LIST,
                    Constants.Permission.VIDEO_CALL_PERMISSION
                )
            }
        } else {
            initAgoraEngine()
        }

    }

    private fun initListener() {


        notificationEventListner =
            RxBusNotification.listen(String::class.java).doOnError {
            }.subscribe {
                runOnUiThread {
                    when {
                        //made by user
                        it.equals(Constants.EventBusKeys.ACCEPT_CALL, true) -> {
                            onCallAccepted()
                        }
                        //made by user
                        it.equals(Constants.EventBusKeys.REJECT_CALL, true) -> {
                            endCall()
                        }
                        //made by both
                        it.equals(Constants.EventBusKeys.END_CALL, true) -> {
                            endCall()
                        }
                        //made by both
                        it.equals(Constants.EventBusKeys.HANGUP_CALL, true) -> {
                            endCall()
                        }
                    }
                }
            }

    }


    private fun initAgoraEngine() {

        initializeAgoraEngine() // Tutorial Step 1

        setupVideoProfile() // Tutorial Step 2

        setupLocalVideo() // Tutorial Step 3

        if (incomingOutGoing == Constants.ChatTypes.OUTGOING_CALL) {
            joinChannel(channelId)
        }

        setSpeakerType()
        setSpeakerUi()
        setMicUi()
        setVideoMuteUi()
    }

    private fun setSpeakerType() {

        if (callType == Constants.ChatTypes.VOICE_CALL) {
            isSpeakerOnAudio = false
        } else if (callType == Constants.ChatTypes.VIDEO_CALL) {
            isSpeakerOnAudio = true
        }
    }

    // Tutorial Step 1
    private fun initializeAgoraEngine() {

        try {
            mRtcEngine =
                RtcEngine.create(baseContext, getString(R.string.agora_app_id), mRtcEventHandler)
        } catch (e: Exception) {
            throw RuntimeException(
                "NEED TO check rtc sdk init fatal error\n" + LogUtil.getStackTraceString(
                    e
                )
            )
        }
    }

    // Tutorial Step 2
    private fun setupVideoProfile() {

        if (callType == Constants.ChatTypes.VOICE_CALL) {
            mRtcEngine!!.disableVideo()
        } else {
            mRtcEngine!!.enableVideo()
        }

        mRtcEngine!!.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
            )
        )
    }


    // Tutorial Step 3
    private fun setupLocalVideo() {
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        local_video_view_container.addView(surfaceView)
        mRtcEngine!!.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
    }

    // Tutorial Step 4
    private fun joinChannel(channelId: String?) {
        mRtcEngine!!.joinChannel(
            null,
            channelId,
            "Extra Optional Data",
            0
        ) // if you do not specify the uid, we will generate the uid for you
//        mRtcEngine!!.joinChannel(null, "demo", "Extra Optional Data", 0) // if you do not specify the uid, we will generate the uid for you
    }


    // Tutorial Step 5
    private fun setupRemoteVideo(uid: Int) {

        if (remote_video_view_container.childCount >= 1) {
            return
        }
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        remote_video_view_container.addView(surfaceView)
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
        surfaceView.tag = uid // for mark purpose

    }

    // Tutorial Step 7
    private fun onRemoteUserLeft() {
        remote_video_view_container.removeAllViews()
        endCall()
    }

    // Tutorial Step 10
    private fun onRemoteUserVideoMuted(uid: Int, muted: Boolean) {
        val surfaceView = remote_video_view_container.getChildAt(0) as SurfaceView
        val tag = surfaceView.tag
        if (tag != null && tag as Int == uid) {
            surfaceView.visibility = if (muted) View.GONE else View.VISIBLE
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.Permission.VIDEO_CALL_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    Constants.Permission.AUDIO_CALL_PERMISSION_LIST,
                    Constants.Permission.AUDIO_CALL_PERMISSION
                )
            } else {
                initAgoraEngine()
            }
        } else if (requestCode == Constants.Permission.AUDIO_CALL_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    Constants.Permission.VIDEO_CALL_PERMISSION_LIST,
                    Constants.Permission.VIDEO_CALL_PERMISSION
                )
            } else {
                initAgoraEngine()
            }
        }
    }


    //user actions
    fun onVideoMuteClicked2(view: View) {

        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(resources.getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        }

        mRtcEngine!!.muteLocalVideoStream(iv.isSelected)

        val surfaceView = local_video_view_container.getChildAt(0) as SurfaceView
        surfaceView.setZOrderMediaOverlay(!iv.isSelected)
        surfaceView.visibility = if (iv.isSelected) View.GONE else View.VISIBLE
    }

    fun onAudioMuteClicked(view: View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(
                ContextCompat.getColor(this, R.color.colorPrimary),
                PorterDuff.Mode.MULTIPLY
            )
        }

        mRtcEngine!!.muteLocalAudioStream(iv.isSelected)
    }

    fun onSwitchCameraClicked2(view: View) {
        mRtcEngine!!.switchCamera()
    }

    fun onEndCallClicked(view: View) {
        finishAndRemoveTask()
    }

    fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(permission),
                requestCode
            )
            return false
        }
        return true
    }

//    ---------------------------------------------------------------------------------------------------------------------------------

    //voice call actions
    public fun onEndVoiceCall(view: View) {
        isCallRejected = true
        scope.launch {
            database?.deletecallStatus(uniqueId.toString())
        }
        endCall()
    }

    public fun onVoiceCallAccepted(view: View) {
        isCallAccepted = true
        scope.launch {
            database?.deletecallStatus(uniqueId.toString())
        }
        onCallAccepted()
    }

    //video call actions
    public fun onVideoCallAccepted(view: View) {
        scope.launch {
            database?.deletecallStatus(uniqueId.toString())
        }
        onCallAccepted()
    }

    public fun onEndVideoCall(view: View) {
        isCallAccepted = true
        scope.launch {
            database?.deletecallStatus(uniqueId.toString())
        }
        endCall()
    }

    //    ---------------------------------------------------------------------------------------------------------------------------------

    private fun onCallAccepted() {

        joinChannel(channelId)

        if (incomingOutGoing == Constants.ChatTypes.INCOMING_CALL) {
            acceptCall()
        }

        if (callType == Constants.ChatTypes.VOICE_CALL) {

            groupInComing.visibility = View.GONE
            groupOutGoing.visibility = View.VISIBLE

            status.text = resources.getString(R.string.connected)


        } else if (callType == Constants.ChatTypes.VIDEO_CALL) {

            groupCommonVideo.visibility = View.GONE
            groupIncomingVideo.visibility = View.GONE
            groupOutGoingVideo.visibility = View.VISIBLE
            local_video_view_container.visibility = View.VISIBLE

            statusVideo.text = resources.getString(R.string.connected)

//            setCameraAnimate()
        }

    }

//    private fun setCameraAnimate() {
//
//        incomingConstraintSet = ConstraintSet()
//        incomingConstraintSet?.clone(this, R.layout.layout_video_call)
//
//        acceptedConstraintSet = ConstraintSet()
//        acceptedConstraintSet?.clone(incomingConstraintSet)
//
//
//        acceptedConstraintSet?.connect(
//            R.id.local_video_view_container,
//            ConstraintSet.TOP,
//            R.id.guidelineTop,
//            ConstraintSet.TOP,
//            0
//        );
//        acceptedConstraintSet?.connect(
//            R.id.local_video_view_container,
//            ConstraintSet.START,
//            R.id.guidlineStart,
//            ConstraintSet.START,
//            0
//        );
//        acceptedConstraintSet?.connect(
//            R.id.local_video_view_container,
//            ConstraintSet.END,
//            R.id.guidlineEnd,
//            ConstraintSet.END,
//            0
//        );
//        acceptedConstraintSet?.connect(
//            R.id.local_video_view_container,
//            ConstraintSet.BOTTOM,
//            R.id.guidlineBottom,
//            ConstraintSet.BOTTOM,
//            0
//        );
//
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            val transition = ChangeBounds()
//            transition.interpolator = AnticipateInterpolator(1.0f)
//            transition.duration = 1000;
//
//            TransitionManager.beginDelayedTransition(videoLayout, transition)
//        }
//        acceptedConstraintSet?.applyTo(videoLayout);
//
//    }

    //    ---------------------------------------------------------------------------------------------------------------------------------


    private fun endCall() {
        notificationEventListner.dispose()
        callViewModel?.endCall(fromId, toId)?.observe(this, Observer {
            if (it.error == "true") {
                showSnack(it.message)
            } else if (it.error == "false") {
                stopRingtone()
                stopEarpiceRingtone()
                NotificationUtils(this).removeCallNotifications()
                mRtcEngine?.leaveChannel()
                RtcEngine.destroy()

                finishAndRemoveTask()
            }
        })

    }

    private fun acceptCall() {
        NotificationUtils(this).notificationEndCall(doctorname)
        callViewModel?.acceptCall(fromId, toId)
    }


    //    ---------------------------------------------------------------------------------------------------------------------------------


    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
        stopEarpiceRingtone()
        NotificationUtils(this).removeCallNotifications()
    }


    override fun onBackPressed() {

    }

    //    ---------------------------------------------------------------------------------------------------------------------------------

    private fun startRigtone() {
        val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringTone = RingtoneManager.getRingtone(applicationContext, notification)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            ringTone?.isLooping = true
        }
        ringTone?.play()
    }

    private fun stopRingtone() {
        ringTone?.let { tone ->
            if (tone.isPlaying) {
                tone.stop()
            }
        }
    }

    private fun startEarpeiceRingtone() {

        mPlayer = MediaPlayer()

        val mUri = Uri.parse(
            "android.resource://"
                    + packageName + "/raw/ringsound"
        );
        mPlayer?.setDataSource(this, mUri)
        mPlayer?.setAudioStreamType(AudioManager.STREAM_VOICE_CALL)
        mPlayer?.isLooping = true
        mPlayer?.prepare()
        mPlayer?.start()
    }

    private fun stopEarpiceRingtone() {
        mPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
        }
    }

    //    ---------------------------------------------------------------------------------------------------------------------------------


    public fun onSpeakerChangeClicked(view: View) {
        changeSpeakerAudio()
    }

    public fun onMicChangeClicked(view: View) {
        changeMicSettings()
    }

    fun onVideoMuteClicked(view: View) {
        changeVideo()
    }

    fun onSwitchCameraClicked(view: View) {
        setCameraFacing()
    }

    //    ---------------------------------------------------------------------------------------------------------------------------------

    private fun setSpeakerUi() {
        mRtcEngine?.let {

            mRtcEngine?.setEnableSpeakerphone(isSpeakerOnAudio)
            if (isSpeakerOnAudio) {
                speaker.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.speaker_on))
                speaker.setBackgroundResource(R.drawable.rounded_bg)
            } else {
                speaker.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.speaker_on))
                speaker.setBackgroundResource(R.color.transparent)
            }
        }
    }

    private fun changeSpeakerAudio() {
        isSpeakerOnAudio = !isSpeakerOnAudio
        setSpeakerUi()
    }


    private fun setMicUi() {

        mRtcEngine?.let {
            it.muteLocalAudioStream(isMute)
            if (isMute) {
                mic.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.mic_off))
                micVideo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.mic_off))

                mic.setBackgroundResource(R.drawable.rounded_bg)
                micVideo.setBackgroundResource(R.drawable.rounded_bg)

            } else {
                mic.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.mic_off))
                micVideo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.mic_off))

                mic.setBackgroundResource(R.color.transparent)
                micVideo.setBackgroundResource(R.color.transparent)



            }
        }
    }

    private fun changeMicSettings() {
        isMute = !isMute
        setMicUi()
    }

    private fun setCameraFacing() {
        mRtcEngine?.let {
            it.switchCamera()
        }
    }

    private fun setVideoMuteUi() {
        mRtcEngine?.let {
            it.muteLocalVideoStream(isVideoMute)
            if (isVideoMute) {
                camera.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.video_off))
                camera.setBackgroundResource(R.drawable.rounded_bg)
            } else {
                camera.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.video_off))
                camera.setBackgroundResource(R.color.transparent)
            }
        }
    }

    private fun changeVideo() {
        isVideoMute = !isVideoMute
        setVideoMuteUi()
    }

    fun showSnack(content: String) {

        showSnack(content)
    }

}