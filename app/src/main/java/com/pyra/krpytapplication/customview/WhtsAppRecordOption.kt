package com.pyra.krpytapplication.customview

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.*
import android.view.View.OnTouchListener
import android.view.animation.*
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.LogUtil
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import com.vanniktech.emoji.EmojiEditText
import kotlinx.android.synthetic.main.activity_whts_app_record_option.*
import kotlin.math.hypot

/**
 * Created by Varun John on 4 Dec, 2018
 * Github : https://github.com/varunjohn
 */
class AudioRecordView {

    enum class UserBehaviour {
        CANCELING, LOCKING, NONE
    }

    enum class RecordingBehaviour {
        CANCELED, LOCKED, LOCK_DONE, RELEASED
    }

    private val TAG = "AudioRecordView"
    private var viewContainer: LinearLayout? = null

    // private var layoutAttachmentOptions: LinearLayout? = null
    private var imageViewAudio: View? = null
    private var imageViewLockArrow: View? = null
    private var imageViewLock: View? = null
    private var imageViewMic: View? = null
    private var dustin: View? = null
    private var dustbinCover: View? = null
    private var imageViewStop: View? = null
    var sendView: View? = null
        private set
    private var layoutAttachment: View? = null
    private var layoutDustin: View? = null
    private var layoutMessage: View? = null
    var attachmentView: View? = null
        private set
    var cameraView: View? = null
        private set
    var emojiView: View? = null
        private set
    private var layoutSlideCancel: View? = null
    private var layoutLock: View? = null
    private var layoutEffect1: View? = null
    private var layoutEffect2: View? = null
    var messageView: EmojiEditText? = null
        private set
    private var timeText: TextView? = null
    private var textViewSlide: TextView? = null
    private var stop: ImageView? = null
    private var audio: ImageView? = null
    var send: ImageView? = null
    private var animBlink: Animation? = null
    private var animJump: Animation? = null
    private var animJumpFast: Animation? = null
    private var isDeleting = false
    private var stopTrackingAction = false
    private var handler: Handler? = null
    private var audioTotalTime = 0
    private var timerTask: TimerTask? = null
    private var audioTimer: Timer? = null
    private var timeFormatter: SimpleDateFormat? = null
    private var lastX = 0f
    private var lastY = 0f
    private var firstX = 0f
    private var firstY = 0f
    private val directionOffset = 0f
    private var cancelOffset = 0f
    private var lockOffset = 0f
    private var dp = 0f
    var isLocked = false
    private var userBehaviour: UserBehaviour = UserBehaviour.NONE
    var recordingListener: RecordingListener? = null
    var isLayoutDirectionRightToLeft = false
    var screenWidth = 0
    var screenHeight = 0
    private var layoutAttachments: MutableList<LinearLayout>? = null
    private var context: Context? = null
    var isShowCameraIcon = true
        private set
    var isShowAttachmentIcon = true
        private set
    var isShowEmojiIcon = true
        private set
    private var removeAttachmentOptionAnimation = false
    var cancelTxt: View? = null

    fun initView(view: ViewGroup?) {
        if (view == null) {
            showErrorLog("initView ViewGroup can't be NULL")
            return
        }
        context = view.context
        view.removeAllViews()
        view.addView(
            LayoutInflater.from(view.context).inflate(
                R.layout.activity_whts_app_record_option,
                null
            )
        )
        timeFormatter = SimpleDateFormat("m:ss", Locale.getDefault())
        val displayMetrics = view.context.resources.displayMetrics
        screenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels
        isLayoutDirectionRightToLeft = view.context.resources.getBoolean(R.bool.is_right_to_left)
        viewContainer = view.findViewById(R.id.layoutContainer)
        // layoutAttachmentOptions = view.findViewById(R.id.layoutAttachmentOptions)
        attachmentView = view.findViewById(R.id.imageViewAttachment)
        cameraView = view.findViewById(R.id.imageViewCamera)
        emojiView = view.findViewById(R.id.imageViewEmoji)
        messageView = view.findViewById(R.id.editTextMessage)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            messageView?.imeOptions = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
        }

        send = view.findViewById(R.id.imageSend)
        stop = view.findViewById(R.id.imageStop)
        audio = view.findViewById(R.id.imageAudio)
        imageViewAudio = view.findViewById(R.id.imageViewAudio)
        imageViewStop = view.findViewById(R.id.imageViewStop)
        sendView = view.findViewById(R.id.imageViewSend)
        imageViewLock = view.findViewById(R.id.imageViewLock)
        imageViewLockArrow = view.findViewById(R.id.imageViewLockArrow)
        layoutDustin = view.findViewById(R.id.layoutDustin)
        layoutMessage = view.findViewById(R.id.layoutMessage)
        layoutAttachment = view.findViewById(R.id.layoutAttachment)
        textViewSlide = view.findViewById(R.id.textViewSlide)
        timeText = view.findViewById(R.id.textViewTime)
        layoutSlideCancel = view.findViewById(R.id.layoutSlideCancel)
        layoutEffect2 = view.findViewById(R.id.layoutEffect2)
        layoutEffect1 = view.findViewById(R.id.layoutEffect1)
        layoutLock = view.findViewById(R.id.layoutLock)
        imageViewMic = view.findViewById(R.id.imageViewMic)
        dustin = view.findViewById(R.id.dustin)
        dustbinCover = view.findViewById(R.id.dustin_cover)
        cancelTxt = view.findViewById(R.id.cancelTxt)
        handler = Handler(Looper.getMainLooper())

        dp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            1f,
            view.context.resources.displayMetrics
        )
        animBlink = AnimationUtils.loadAnimation(
            view.context,
            R.anim.blink
        )
        animJump = AnimationUtils.loadAnimation(
            view.context,
            R.anim.jump
        )
        animJumpFast = AnimationUtils.loadAnimation(
            view.context,
            R.anim.jump_fast
        )
        setupRecording()
        messageView?.setOnClickListener {
            if (layoutAttachment?.visibility == View.VISIBLE) {
                hideAttachmentOption()
            }
        }
        // setupAttachmentOptions()
    }

    fun changeSlideToCancelText(textResourceId: Int) {
        textViewSlide!!.setText(textResourceId)
    }

    fun showCameraIcon(showCameraIcon: Boolean) {
        isShowCameraIcon = showCameraIcon
        if (showCameraIcon) {
            cameraView!!.visibility = View.VISIBLE
        } else {
            cameraView!!.visibility = View.GONE
        }
    }

    fun showAttachmentIcon(showAttachmentIcon: Boolean) {
        isShowAttachmentIcon = showAttachmentIcon
        if (showAttachmentIcon) {
            attachmentView!!.visibility = View.VISIBLE
        } else {
            attachmentView!!.visibility = View.INVISIBLE
        }
    }

    fun showEmojiIcon(showEmojiIcon: Boolean) {
        isShowEmojiIcon = showEmojiIcon
        if (showEmojiIcon) {
            emojiView!!.visibility = View.VISIBLE
        } else {
            emojiView!!.visibility = View.INVISIBLE
        }
    }

    fun hideAttachmentOptionView() {
        if (layoutAttachment!!.visibility == View.VISIBLE) {
            attachmentView!!.performClick()
        }
    }

    fun showAttachmentOptionView() {
        if (layoutAttachment!!.visibility != View.VISIBLE) {
            attachmentView!!.performClick()
        }
    }

    fun removeAttachmentOptionAnimation(removeAttachmentOptionAnimation: Boolean) {
        this.removeAttachmentOptionAnimation = removeAttachmentOptionAnimation
    }

    fun setContainerView(layoutResourceID: Int): View? {
        val view = LayoutInflater.from(viewContainer!!.context).inflate(layoutResourceID, null)
        if (view == null) {
            showErrorLog("Unable to create the Container View from the layoutResourceID")
            return null
        }
        viewContainer!!.removeAllViews()
        viewContainer!!.addView(view)
        return view
    }

    fun setAudioRecordButtonImage(imageResource: Int) {
        audio!!.setImageResource(imageResource)
    }

    fun setStopButtonImage(imageResource: Int) {
        stop!!.setImageResource(imageResource)
    }

    fun setSendButtonImage(imageResource: Int) {
        send!!.setImageResource(imageResource)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupRecording() {
        sendView!!.animate().scaleX(0f).scaleY(0f).setDuration(100)
            .setInterpolator(LinearInterpolator()).start()
        messageView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.toString().trim { it <= ' ' }.isEmpty()) {
                    if (sendView!!.visibility != View.GONE) {
                        sendView!!.visibility = View.GONE
                        sendView!!.animate().scaleX(0f).scaleY(0f).setDuration(100)
                            .setInterpolator(LinearInterpolator()).start()
                    }
                    if (isShowCameraIcon) {
                        if (cameraView!!.visibility != View.VISIBLE && !isLocked) {
                            cameraView!!.visibility = View.VISIBLE
                            cameraView!!.animate().scaleX(1f).scaleY(1f).setDuration(100)
                                .setInterpolator(LinearInterpolator()).start()
                        }
                    }
                } else {
                    if (sendView!!.visibility != View.VISIBLE && !isLocked) {
                        sendView!!.visibility = View.VISIBLE
                        sendView!!.animate().scaleX(1f).scaleY(1f).setDuration(100)
                            .setInterpolator(LinearInterpolator()).start()
                        hideAttachmentOption()
                    }
                    if (isShowCameraIcon) {
                        if (cameraView!!.visibility != View.GONE) {
                            cameraView!!.visibility = View.GONE
                            hideAttachmentOption()
                            cameraView!!.animate().scaleX(0f).scaleY(0f).setDuration(100)
                                .setInterpolator(LinearInterpolator()).start()
                        }
                    }
                }
            }
        })
        imageViewAudio!!.setOnTouchListener(OnTouchListener { view, motionEvent ->
            if (isDeleting) {
                return@OnTouchListener true
            }
            if (checkPermissions()) {
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {

                    cancelOffset = (screenWidth / 2.8).toFloat()
                    lockOffset = (screenWidth / 2.5).toFloat()
                    if (firstX == 0f) {
                        firstX = motionEvent.rawX
                    }
                    if (firstY == 0f) {
                        firstY = motionEvent.rawY
                    }
                    startRecord()

                } else if (motionEvent.action == MotionEvent.ACTION_UP
                    || motionEvent.action == MotionEvent.ACTION_CANCEL
                ) {
                    if (motionEvent.action == MotionEvent.ACTION_UP) {
                        stopRecording(RecordingBehaviour.RELEASED)
                    }
                } else if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                    if (stopTrackingAction) {
                        return@OnTouchListener true
                    }
                    var direction: UserBehaviour = UserBehaviour.NONE
                    val motionX = abs(firstX - motionEvent.rawX)
                    val motionY = abs(firstY - motionEvent.rawY)
                    if (if (isLayoutDirectionRightToLeft) motionX > directionOffset && lastX > firstX && lastY > firstY else motionX > directionOffset && lastX < firstX && lastY < firstY) {
                        if (if (isLayoutDirectionRightToLeft) motionX > motionY && lastX > firstX else motionX > motionY && lastX < firstX) {
                            direction = UserBehaviour.CANCELING
                        } else if (motionY > motionX && lastY < firstY) {
                            direction = UserBehaviour.LOCKING
                        }
                    } else if (if (isLayoutDirectionRightToLeft) motionX > motionY && motionX > directionOffset && lastX > firstX else motionX > motionY && motionX > directionOffset && lastX < firstX) {
                        direction = UserBehaviour.CANCELING
                    } else if (motionY > motionX && motionY > directionOffset && lastY < firstY) {
                        direction = UserBehaviour.LOCKING
                    }
                    if (direction == UserBehaviour.CANCELING) {
                        if (userBehaviour == UserBehaviour.NONE || motionEvent.rawY + imageViewAudio!!.width / 2 > firstY) {
                            userBehaviour = UserBehaviour.CANCELING
                        }
                        if (userBehaviour == UserBehaviour.CANCELING) {
                            translateX(-(firstX - motionEvent.rawX))
                        }
                    } else if (direction == UserBehaviour.LOCKING) {
                        if (userBehaviour == UserBehaviour.NONE || motionEvent.rawX + imageViewAudio!!.width / 2 > firstX) {
                            userBehaviour = UserBehaviour.LOCKING
                        }
                        if (userBehaviour == UserBehaviour.LOCKING) {
                            translateY(-(firstY - motionEvent.rawY))
                        }
                    }
                    lastX = motionEvent.rawX
                    lastY = motionEvent.rawY
                }
                view.onTouchEvent(motionEvent)
                true
            } else {
                recordingListener!!.requestRecordPermission()
                true
            }
        })
        imageViewStop!!.setOnClickListener {
            isLocked = false
            stopRecording(RecordingBehaviour.LOCK_DONE)
        }
    }

    private fun translateY(y: Float) {
        if (y < -lockOffset) {
            locked()
            imageViewAudio!!.translationY = 0f
            return
        }
        if (layoutLock!!.visibility != View.VISIBLE) {
            layoutLock!!.visibility = View.VISIBLE
        }
        imageViewAudio!!.translationY = y
        layoutLock!!.translationY = y / 2
        imageViewAudio!!.translationX = 0f
    }

    private fun translateX(x: Float) {
        if (if (isLayoutDirectionRightToLeft) x > cancelOffset else x < -cancelOffset) {
            canceled()
            imageViewAudio!!.translationX = 0f
            layoutSlideCancel!!.translationX = 0f
            return
        }
        imageViewAudio!!.translationX = x
        layoutSlideCancel!!.translationX = x
        layoutLock!!.translationY = 0f
        imageViewAudio!!.translationY = 0f
        if (abs(x) < imageViewMic!!.width / 2) {
            if (layoutLock!!.visibility != View.VISIBLE) {
                layoutLock!!.visibility = View.VISIBLE
            }
        } else {
            if (layoutLock!!.visibility != View.GONE) {
                layoutLock!!.visibility = View.GONE
            }
        }
    }

    private fun locked() {
        stopTrackingAction = true
        stopRecording(RecordingBehaviour.LOCKED)
        isLocked = true
    }

    fun canceled() {
        stopTrackingAction = true
        stopRecording(RecordingBehaviour.CANCELED)
    }

    fun stopRecording(recordingBehaviour: RecordingBehaviour) {
        stopTrackingAction = true
        firstX = 0f
        firstY = 0f
        lastX = 0f
        lastY = 0f
        userBehaviour = UserBehaviour.NONE
        imageViewAudio!!.animate().scaleX(1f).scaleY(1f).translationX(0f).translationY(0f)
            .setDuration(100).setInterpolator(LinearInterpolator()).start()
        layoutSlideCancel!!.translationX = 0f
        layoutSlideCancel!!.visibility = View.GONE
        layoutLock!!.visibility = View.GONE
        layoutLock!!.translationY = 0f
        imageViewLockArrow!!.clearAnimation()
        imageViewLock!!.clearAnimation()
        if (isLocked) {
            return
        }
        if (recordingBehaviour == RecordingBehaviour.LOCKED) {
            imageViewStop!!.visibility = View.VISIBLE
            if (recordingListener != null) recordingListener!!.onRecordingLocked()
        } else if (recordingBehaviour == RecordingBehaviour.CANCELED) {
            timeText!!.clearAnimation()
            timeText!!.visibility = View.INVISIBLE
            imageViewMic!!.visibility = View.INVISIBLE
            imageViewStop!!.visibility = View.GONE
            layoutEffect2!!.visibility = View.GONE
            layoutEffect1!!.visibility = View.GONE
            timerTask!!.cancel()
            delete()
            if (recordingListener != null) recordingListener!!.onRecordingCanceled()
        } else if (recordingBehaviour == RecordingBehaviour.RELEASED || recordingBehaviour == RecordingBehaviour.LOCK_DONE) {
            timeText!!.clearAnimation()
            timeText!!.visibility = View.INVISIBLE
            imageViewMic!!.visibility = View.INVISIBLE
            messageView!!.visibility = View.VISIBLE
            if (isShowAttachmentIcon) {
                attachmentView!!.visibility = View.VISIBLE
            }
            if (isShowCameraIcon) {
                cameraView!!.visibility = View.VISIBLE
            }
            if (isShowEmojiIcon) {
                emojiView!!.visibility = View.VISIBLE
            }
            imageViewStop!!.visibility = View.GONE
            messageView!!.requestFocus()
            layoutEffect2!!.visibility = View.GONE
            layoutEffect1!!.visibility = View.GONE
            timerTask!!.cancel()
            if (recordingListener != null) recordingListener!!.onRecordingCompleted()
        }
    }

    private fun startRecord() {
        if (recordingListener != null) recordingListener!!.onRecordingStarted()
        hideAttachmentOption()
        stopTrackingAction = false
        messageView!!.visibility = View.INVISIBLE
        attachmentView!!.visibility = View.INVISIBLE
        cameraView!!.visibility = View.INVISIBLE
        emojiView!!.visibility = View.INVISIBLE
        imageViewAudio!!.animate().scaleXBy(1f).scaleYBy(1f).setDuration(200)
            .setInterpolator(OvershootInterpolator()).start()
        timeText!!.visibility = View.VISIBLE
        layoutLock!!.visibility = View.VISIBLE
        layoutSlideCancel!!.visibility = View.VISIBLE
        imageViewMic!!.visibility = View.VISIBLE
        layoutEffect2!!.visibility = View.VISIBLE
        layoutEffect1!!.visibility = View.VISIBLE
        timeText!!.startAnimation(animBlink)
        imageViewLockArrow!!.clearAnimation()
        imageViewLock!!.clearAnimation()
        imageViewLockArrow!!.startAnimation(animJumpFast)
        imageViewLock!!.startAnimation(animJump)
        if (audioTimer == null) {
            audioTimer = Timer()
            timeFormatter!!.timeZone = TimeZone.getTimeZone("UTC")
        }
        timerTask = object : TimerTask() {
            override fun run() {
                handler!!.post {
                    val timeLong = audioTotalTime * 1000
                    timeText!!.text = timeFormatter!!.format(Date(timeLong.toLong()))
                    audioTotalTime++
                }
            }
        }
        audioTotalTime = 0
        audioTimer!!.schedule(timerTask, 0, 1000)
    }

    private fun delete() {
        imageViewMic!!.visibility = View.VISIBLE
        imageViewMic!!.rotation = 0f
        isDeleting = true
        imageViewAudio!!.isEnabled = false
        handler!!.postDelayed({
            isDeleting = false
            imageViewAudio!!.isEnabled = true
            if (isShowAttachmentIcon) {
                attachmentView!!.visibility = View.VISIBLE
            }
            if (isShowCameraIcon) {
                cameraView!!.visibility = View.VISIBLE
            }
            if (isShowEmojiIcon) {
                emojiView!!.visibility = View.VISIBLE
            }
        }, 1250)
        imageViewMic!!.animate().translationY(-dp * 150).rotation(180f).scaleXBy(0.6f)
            .scaleYBy(0.6f).setDuration(500).setInterpolator(
                DecelerateInterpolator()
            ).setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    var displacement = 0f
                    displacement = if (isLayoutDirectionRightToLeft) {
                        dp * 40
                    } else {
                        -dp * 40
                    }
                    dustin!!.translationX = displacement
                    dustbinCover!!.translationX = displacement
                    dustbinCover!!.animate().translationX(0f).rotation(-120f).setDuration(350)
                        .setInterpolator(
                            DecelerateInterpolator()
                        ).start()
                    dustin!!.animate().translationX(0f).setDuration(350).setInterpolator(
                        DecelerateInterpolator()
                    ).setListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                            dustin!!.visibility = View.VISIBLE
                            dustbinCover!!.visibility = View.VISIBLE
                        }

                        override fun onAnimationEnd(animation: Animator) {}
                        override fun onAnimationCancel(animation: Animator) {}
                        override fun onAnimationRepeat(animation: Animator) {}
                    }).start()
                }

                override fun onAnimationEnd(animation: Animator) {
                    imageViewMic!!.animate().translationY(0f).scaleX(1f).scaleY(1f).setDuration(350)
                        .setInterpolator(LinearInterpolator()).setListener(
                            object : Animator.AnimatorListener {
                                override fun onAnimationStart(animation: Animator) {}
                                override fun onAnimationEnd(animation: Animator) {
                                    imageViewMic!!.visibility = View.INVISIBLE
                                    imageViewMic!!.rotation = 0f
                                    var displacement = 0f
                                    displacement = if (isLayoutDirectionRightToLeft) {
                                        dp * 40
                                    } else {
                                        -dp * 40
                                    }
                                    dustbinCover!!.animate().rotation(0f).setDuration(150)
                                        .setStartDelay(50)
                                        .start()
                                    dustin!!.animate().translationX(displacement).setDuration(200)
                                        .setStartDelay(250).setInterpolator(
                                            DecelerateInterpolator()
                                        ).start()
                                    dustbinCover!!.animate().translationX(displacement)
                                        .setDuration(200)
                                        .setStartDelay(250).setInterpolator(
                                            DecelerateInterpolator()
                                        ).setListener(object : Animator.AnimatorListener {
                                            override fun onAnimationStart(animation: Animator) {}
                                            override fun onAnimationEnd(animation: Animator) {
                                                messageView!!.visibility = View.VISIBLE
                                                messageView!!.requestFocus()
                                            }

                                            override fun onAnimationCancel(animation: Animator) {}
                                            override fun onAnimationRepeat(animation: Animator) {}
                                        }).start()
                                }

                                override fun onAnimationCancel(animation: Animator) {}
                                override fun onAnimationRepeat(animation: Animator) {}
                            }
                        ).start()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            }).start()
    }

    private fun showErrorLog(s: String) {
        LogUtil.e(TAG, s)
    }


    private fun hideAttachmentOption() {
        val x =
            if (isLayoutDirectionRightToLeft) (dp * (18 + 40 + 4 + 56)) else (screenWidth - dp * (18 + 40 + 4 + 56))
        val y = (dp * 220)
        val startRadius = 0
        val endRadius =
            Math.hypot(screenWidth - (dp * (8 + 8)).toDouble(), (dp * 220).toDouble())
                .toInt()
        val anim = ViewAnimationUtils.createCircularReveal(
            layoutAttachment,
            x.toInt(),
            y.toInt(),
            endRadius.toFloat(),
            startRadius.toFloat()
        )
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                layoutAttachment?.visibility = View.GONE
            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        anim.start()
    }


    fun setupAttachmentOptions(layoutAttachment: View) {
        if (layoutAttachment.visibility == View.VISIBLE) {
            val x =
                if (isLayoutDirectionRightToLeft) (dp * (18 + 40 + 4 + 56)) else (screenWidth - dp * (18 + 40 + 4 + 56))
            val y = (dp * 220)
            val startRadius = 0
            val endRadius =
                hypot(screenWidth - (dp * (8 + 8)).toDouble(), (dp * 220).toDouble())
                    .toInt()
            val anim = ViewAnimationUtils.createCircularReveal(
                layoutAttachment,
                x.toInt(),
                y.toInt(),
                endRadius.toFloat(),
                startRadius.toFloat()
            )
            anim.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {
                    layoutAttachment.visibility = View.GONE
                }

                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
            })
            anim.start()
        } else {
            val x =
                if (isLayoutDirectionRightToLeft) (dp * (18 + 40 + 4 + 56)) else (screenWidth - dp * (18 + 40 + 4 + 56))
            val y = (dp * 220)
            val startRadius = 0
            val endRadius =
                hypot(screenWidth - (dp * (8 + 8)).toDouble(), (dp * 220).toDouble())
                    .toInt()
            val anim = ViewAnimationUtils.createCircularReveal(
                layoutAttachment,
                x.toInt(),
                y.toInt(),
                startRadius.toFloat(),
                endRadius.toFloat()
            )
            anim.duration = 500
            layoutAttachment.visibility = View.VISIBLE
            anim.start()
        }

    }


    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val storage = ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val audio = ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.RECORD_AUDIO
            )
            storage == PackageManager.PERMISSION_GRANTED && audio == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    }


}