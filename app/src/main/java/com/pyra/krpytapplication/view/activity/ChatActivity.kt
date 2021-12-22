package com.pyra.krpytapplication.view.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.*
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pyra.krpytapplication.BuildConfig
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.*
import com.pyra.krpytapplication.app.AppRunningService
import com.pyra.krpytapplication.customview.AudioRecordView
import com.pyra.krpytapplication.customview.RecordingListener
import com.pyra.krpytapplication.notification.NotificationUtils
import com.pyra.krpytapplication.roomDb.entity.ChatMessagesSchema
import com.pyra.krpytapplication.videocallutils.view.activity.GroupCallActivity
import com.pyra.krpytapplication.view.adapter.ChatMessageAdapter
import com.pyra.krpytapplication.view.adapter.ParticipationListAdapter
import com.pyra.krpytapplication.view.adapter.SelectedUsersAdapter
import com.pyra.krpytapplication.view.fragment.AudioRecordDialogFragment
import com.pyra.krpytapplication.viewmodel.*
import com.pyra.network.UrlHelper
import com.vanniktech.emoji.EmojiPopup
import copyFile
import de.hdodenhof.circleimageview.CircleImageView
import getPath
import io.socket.client.IO
import io.socket.client.Socket
import isMyServiceRunning
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_whts_app_record_option.*
import openAudioIntent
import openCamera
import openFileIntent
import openGallery
import org.json.JSONObject
import java.io.*
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : BaseActivity(), RecordingListener {

    lateinit var chatMessageAdapter: ChatMessageAdapter
    lateinit var callViewModel: CallViewModel
    lateinit var chatListViewModel: ChatListViewModel
    lateinit var chatMessagesViewModel: ChatMessagesViewModel
    lateinit var createRoomViewModel: CreateRoomViewModel
    private val profileViewModel: ProfileViewModel by viewModels()
    lateinit var sharedHelper: SharedHelper
    var kryptCode = ""
    var roomId = ""
    var name = ""
    var image = ""
    var presenceInfo = ""
    lateinit var menuItemView: View
    private var emojipopup: EmojiPopup? = null

    private var createRoomDialog: BottomSheetDialog? = null
    private var selectedView: RecyclerView? = null
    private var groupMembers: RecyclerView? = null

    private var voiceCall: ImageView? = null
    private var videoCall: ImageView? = null

    private var socket: Socket? = null

    private val audioRecordView by lazy {
        AudioRecordView()
    }
    private val containerView by lazy {
        audioRecordView.setContainerView(R.layout.activity_chat)
    }
    private val selectedLayout by lazy {
        containerView?.findViewById<Group>(R.id.selectedLayout)
    }
    private val unSelectedLayout by lazy {
        containerView?.findViewById<Group>(R.id.unSelectedLayout)
    }
    private val backButton by lazy {
        containerView?.findViewById<ImageView>(R.id.backButton)
    }
    private val closeButton by lazy {
        containerView?.findViewById<ImageView>(R.id.closeButton)
    }
    private val userImage by lazy {
        containerView?.findViewById<CircleImageView>(R.id.userImage)
    }
    private val chatName by lazy {
        containerView?.findViewById<TextView>(R.id.chatName)
    }
    private val membersCount by lazy {
        containerView?.findViewById<TextView>(R.id.membersCount)
    }
    private val menuIcon by lazy {
        containerView?.findViewById<ImageView>(R.id.menuIcon)
    }
    private val deleteIcon by lazy {
        containerView?.findViewById<ImageView>(R.id.deleteIcon)
    }
    private val bookmarkIcon by lazy {
        containerView?.findViewById<ImageView>(R.id.bookmarkIcon)
    }
    private val forward by lazy {
        containerView?.findViewById<ImageView>(R.id.forward)
    }
    private val editIcon by lazy {
        containerView?.findViewById<ImageView>(R.id.editIcon)
    }
    private val audioCallIcon by lazy {
        containerView?.findViewById<ImageView>(R.id.audioCallIcon)
    }
    private val groupCall by lazy {
        containerView?.findViewById<ImageView>(R.id.groupCall)
    }
    private val videoCallIcon by lazy {
        containerView?.findViewById<ImageView>(R.id.videoCallIcon)
    }
    private val addContactLayout by lazy {
        containerView?.findViewById<ConstraintLayout>(R.id.addContactLayout)
    }
    private val addContactImage by lazy {
        containerView?.findViewById<CircleImageView>(R.id.addContactImage)
    }
    private val addContactTitle by lazy {
        containerView?.findViewById<TextView>(R.id.addContactTitle)
    }
    private val bottomView by lazy {
        containerView?.findViewById<LinearLayout>(R.id.bottomView)
    }

    private lateinit var audioTrack: AudioTrack

    private var time: Long = 0

    private lateinit var myAudioRecorder: MediaRecorder

    private lateinit var file: File

    companion object {
        private const val AUDIO_EXTENSION = ".3gp"
        private const val TAG = "ChatActivity"
    }

    private val loader by lazy {
        showLoader(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!BuildConfig.DEBUG) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        setContentView(R.layout.chat_activity_main)
        audioRecordView.initView(findViewById<View>(R.id.layoutMain) as FrameLayout)

        callViewModel = ViewModelProvider(this).get(CallViewModel::class.java)
        chatListViewModel = ViewModelProvider(this).get(ChatListViewModel::class.java)
        chatMessagesViewModel = ViewModelProvider(this).get(ChatMessagesViewModel::class.java)
        createRoomViewModel = ViewModelProvider(this).get(CreateRoomViewModel::class.java)
        sharedHelper = SharedHelper(this)
        init()
        setMenuInflater()
        initEmoji()
        initObservers()
        val intentFilter = IntentFilter()
        intentFilter.addAction("PRESENCE")
        intentFilter.addAction("TYPING")
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(MyBroadcastReceiver, intentFilter)
        audioRecordView.recordingListener = this
        setListener()
        initSockets()

        initService()

        getUserOnlineStatus()
    }

    private fun initService() {

        if (!isMyServiceRunning(this, AppRunningService::class.java))
            startService(Intent(this, AppRunningService::class.java))
    }

    private fun initCreateRoomDialog() {

        createRoomViewModel.selectedList = ArrayList()
        createRoomViewModel.selectedIds = ArrayList()

        createRoomDialog = getCreateRoomDialog(this)

        selectedView = createRoomDialog?.findViewById(R.id.selectedView)
        groupMembers = createRoomDialog?.findViewById(R.id.groupMembers)
        voiceCall = createRoomDialog?.findViewById(R.id.voiceCall)
        videoCall = createRoomDialog?.findViewById(R.id.videoCall)

        val selectedAdapter = SelectedUsersAdapter(this, createRoomViewModel)
        selectedView?.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        selectedView?.adapter = selectedAdapter

        val participationAdapter = ParticipationListAdapter(this, createRoomViewModel)
        groupMembers?.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        groupMembers?.adapter = participationAdapter

        createRoomViewModel.notifyAdapter?.observe(this, Observer {
            selectedAdapter.notifyChanges()
            participationAdapter.notifyChanges()

            if (createRoomViewModel.selectedIds.size == 0) {
                videoCall?.hide()
                voiceCall?.hide()
                selectedView?.hide()
            } else {
                videoCall?.show()
                voiceCall?.show()
                selectedView?.show()
            }
        })

        intent?.getStringExtra(Constants.IntentKeys.ROOMID)?.let {
            createRoomViewModel.getGroupMembers(it)
        }

        voiceCall?.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    Constants.Permission.GROUP_AUDIO_CALL_PERMISSION_LIST,
                    Constants.Permission.GROUP_AUDIO_CALL_PERMISSION
                )
            } else {
                createGroupCall(Constants.ChatTypes.VOICE_CALL)
            }
        }

        videoCall?.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    Constants.Permission.GROUP_VIDEO_CALL_PERMISSION_LIST,
                    Constants.Permission.GROUP_VIDEO_CALL_PERMISSION
                )
            } else {
                createGroupCall(Constants.ChatTypes.VIDEO_CALL)
            }

        }

        createRoomDialog?.show()
    }

    private fun createGroupCall(callType: String) {

        val channelName = UUID.randomUUID().toString();

        chatMessagesViewModel.createGroupCall(
            this,
            callType,
            createRoomViewModel.selectedIds,
            channelName,
            roomId
        )
            .observe(this,
                Observer {
                    if (it.error == "true") {
                        showSnack(it.message)
                        findViewById<LinearLayout>(R.id.rootView)
                    } else if (it.error == "false") {
                        startGroupChat(callType, channelName)
                    }
                })

    }

    private fun setListener() {
        audioRecordView.emojiView!!.setOnClickListener {
            audioRecordView.hideAttachmentOptionView()
            emojipopup?.toggle()
        }
        audioRecordView.cameraView!!.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    Constants.Permission.CAMERA_PERM_LIST,
                    Constants.Permission.CAMERA_STORAGE_PERMISSIONS
                )
            } else {
                openCamera(this)
            }

            layoutAttachment.hide()
        }
        audioRecordView.sendView!!.setOnClickListener {
//            val msg = audioRecordView.messageView!!.text.toString().trim { it <= ' ' }
//            audioRecordView.messageView!!.setText("")
            onSendButtonClicked()
        }

        audioRecordView.attachmentView!!.setOnClickListener {
//            audioRecordView.setupAttachmentOptions(layoutAttachment)

            startActivityForResult(
                Intent(this, GalleryActivity::class.java)
                    .putExtra("roomId", roomId)
                    .putExtra("kryptId", kryptCode)
                    .putExtra("isGroup", chatMessagesViewModel.getIsGroupChat()),
                Constants.RequestCode.GALLERY_INTENT
            )

        }

        audioRecordView.cancelTxt?.setOnClickListener {
            audioRecordView.isLocked = false
            audioRecordView.stopRecording(AudioRecordView.RecordingBehaviour.CANCELED)
        }

        groupCall?.setOnClickListener {
            initCreateRoomDialog()
        }

    }

    private fun initObservers() {

        chatMessagesViewModel.reply.observe(this, Observer {
            if (it.messageId != "") {

                replyCard.show()

                chatMessagesViewModel.getName(it)
                messageReply.text = chatMessagesViewModel.getMessage(it)

                when (it.messageType.toLowerCase()) {
                    MessageType.IMAGE.toString()
                        .toLowerCase() -> {
                        imageReply.show()
                        imageReply.loadImage(it.mediaUrl)
                    }
                    MessageType.VIDEO.toString()
                        .toLowerCase() -> {
                        imageReply.show()
                        imageReply.loadImage(it.mediaUrl)
                    }
                    else -> {
                        imageReply.hide()
                    }
                }
            } else {
                replyCard.hide()
            }
        })

        chatMessagesViewModel.replerName.observe(this, Observer {
            nameReply.text = it
        })

        cancelReply.setOnClickListener {
            chatMessagesViewModel.reply.value = ChatMessagesSchema()
        }
    }

    private fun setMenuInflater() {

        val popup = PopupMenu(this, menuIcon)
        popup.menuInflater.inflate(R.menu.chat_message_menu, popup.menu)

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.clearChat -> {
                    chatMessagesViewModel.clearChat()
                }
            }
            return@setOnMenuItemClickListener true
        }

        menuIcon?.setOnClickListener {
            popup.show()
        }

    }

//    fun onAttachmentClicked(view: View) {
//        mediaAttachmentLayout.toggleVisibility()
//    }

    private fun init() {

        initIntentData()
        chatMessagesViewModel.setRoomId(roomId)
        initAdapter()
        initKeyboardListener()

        chatMessagesViewModel.resetUnreadCount()
//        chatMessagesViewModel.getChatMessages()
        chatMessagesViewModel.presenceInfo.observe(this, Observer {
            if (!chatMessagesViewModel.getIsGroupChat()) {

            }
//                membersCount?.text = it
        })
        setCallUi()
        chatMessagesViewModel.saveCurrentChatUser()

        editTextMessage.addTextChangedListener {

        }

    }

    private fun getUserOnlineStatus() {

        chatMessagesViewModel.onlineStatus.observe(this, Observer {
            membersCount?.text = it
        })
    }

    override fun onResume() {
        super.onResume()
        chatMessagesViewModel.saveCurrentChatUser()
        NotificationUtils(this).removeNotification()
    }

    private fun initKeyboardListener() {
        var timer = Timer()
        val DELAY: Long = 2000

        audioRecordView.messageView?.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        LogUtil.e("TAG", "timer stopped")
                        chatMessagesViewModel.sendTypingStatus(false)
                    }
                }, DELAY)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                chatMessagesViewModel.sendTypingStatus(true)
                LogUtil.e("TAG", "typing started ")
                timer.cancel() //Terminates this timer,discarding any currently scheduled tasks.
                timer.purge() //Removes all cancelled tasks from this timer's task queue.
            }
        })

    }

    fun onSendButtonClicked() {
//        val content = audioRecordView.edi.text.toString()
//        chatBoxEditText.setText("")

        val msg = audioRecordView.messageView!!.text.toString().trim { it <= ' ' }
        audioRecordView.messageView!!.setText("")
        chatMessagesViewModel.sendMessage(
            content = msg,
            messageType = MessageType.TEXT
        )
    }

    private fun initAdapter() {

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.isSmoothScrollbarEnabled = true
        chatMessageAdapter = ChatMessageAdapter(this, false, chatMessagesViewModel)
        chatMessageLists.layoutManager = linearLayoutManager
        chatMessageLists.itemAnimator = null
        chatMessageLists?.adapter = chatMessageAdapter // mention the position in place of 0

        val itemTouchHelperCallBack = MessageSwipeController(this) {
            chatMessagesViewModel.setReply(it)
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallBack)
        itemTouchHelper.attachToRecyclerView(chatMessageLists)

        chatMessagesViewModel.notifySelection.observe(this, Observer {
            changeUi()
            chatMessageAdapter.notifySelection()
//            chatMessageLists.smoothScrollToPosition(0)
            if (chatMessagesViewModel.isAllSavedMessage) {

                bookmarkIcon?.setImageDrawable(getDrawable(R.drawable.vault_icon))
            } else {
                bookmarkIcon?.setImageDrawable(getDrawable(R.drawable.vault_icon))
            }

            if (chatMessagesViewModel.isAllMessageForwadable && chatMessagesViewModel.isMultiSelectedEnabled) {
                forward?.show()
            } else {
                forward?.hide()
            }
        })

        chatMessagesViewModel.isUserBlocked.observe(this, Observer {
            if (it) {
                messageContainerLayout?.hide()
                imageViewAudio?.hide()
                userBlockedMsg.show()
            } else {
                messageContainerLayout?.show()
                imageViewAudio?.show()
                userBlockedMsg.hide()
            }
        })

        chatMessagesViewModel.update.observe(this, Observer {
            LogUtil.d("messageList", "true")
            chatMessageAdapter.notifySelection()
            chatMessagesViewModel.updateSeenStatus()
            userImage?.loadImage(chatMessagesViewModel.getProfileImage())
            chatMessageLists.postDelayed({
                chatMessageLists.smoothScrollToPosition(0)

            }, 500)
        })

    }

    private fun changeUi() {

        if (chatMessagesViewModel.isMultiSelectedEnabled) {
            selectedLayout?.show()
            unSelectedLayout?.hide()
        } else {
            unSelectedLayout?.show()
            selectedLayout?.hide()
            forward?.hide()
        }
        setCallUi()
    }

    private fun setCallUi() {

        if (chatMessagesViewModel.isMultiSelectedEnabled) {
            audioCallIcon?.hide()
            videoCallIcon?.hide()
            groupCall?.hide()

        } else {
            if (chatMessagesViewModel.getIsGroupChat()) {
                groupCall?.show()
                audioCallIcon?.hide()
                videoCallIcon?.hide()
            } else {

                groupCall?.hide()
                audioCallIcon?.show()
                videoCallIcon?.show()
            }
        }

    }

    private fun initIntentData() {
        if (intent.extras != null) {
            if (intent.hasExtra(Constants.IntentKeys.KRYPTKEY)) {
                intent.getStringExtra(Constants.IntentKeys.KRYPTKEY)?.let { kryptCode = it }
            }
            if (intent.hasExtra(Constants.IntentKeys.ROOMID)) {
                intent.getStringExtra(Constants.IntentKeys.ROOMID)?.let { roomId = it }
            }
            name = intent.getStringExtra(Constants.IntentKeys.DISPLAY_NAME)
            chatName?.text = name
            if (intent.getBooleanExtra(Constants.IntentKeys.IS_ADDED_TO_CONTACTS, false)) {
                addContactLayout?.hide()
            } else {
                addContactLayout?.show()
            }
        }
    }

    fun showAddContactDialog(view: View) {
        showKryptCodeDialog()
    }

    fun onChatDetailsActivity(view: View) {
        startActivity(
            Intent(this, ChatDetailsActivity::class.java)
                .putExtra(Constants.IntentKeys.ROOMID, roomId)
        )
    }

    fun onBackButtonPressed(view: View) {
        onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        chatMessagesViewModel.unSubscribeMessageReceiver()
        chatMessagesViewModel.resetUnreadCount()
        chatMessagesViewModel.removeCurrentChatUser()
    }

    fun onAudioCallClicked(view: View) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                Constants.Permission.AUDIO_CALL_PERMISSION_LIST,
                Constants.Permission.AUDIO_CALL_PERMISSION
            )
        } else {
            createCall(Constants.ChatTypes.VOICE_CALL)
        }

    }

    fun onVideoCallClicked(view: View) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                Constants.Permission.VIDEO_CALL_PERMISSION_LIST,
                Constants.Permission.VIDEO_CALL_PERMISSION
            )
        } else {
            createCall(Constants.ChatTypes.VIDEO_CALL)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            Constants.Permission.AUDIO_CALL_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    createCall(Constants.ChatTypes.VOICE_CALL)

                }

            }
            Constants.Permission.VIDEO_CALL_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    createCall(Constants.ChatTypes.VIDEO_CALL)

                }
            }

            Constants.Permission.GROUP_AUDIO_CALL_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    createGroupCall(Constants.ChatTypes.VOICE_CALL)

                }

            }

            Constants.Permission.GROUP_VIDEO_CALL_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {

                    createGroupCall(Constants.ChatTypes.VIDEO_CALL)

                }
            }

            Constants.Permission.READ_STORAGE_PERMISSIONS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery(this)
            } else {

                showToast(getString(R.string.storage_permission_error))
            }

            Constants.Permission.READ_FILE_PERMISSIONS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileIntent(this)
            } else {
                showToast(getString(R.string.storage_permission_error))

            }

            Constants.Permission.READ_AUDIO_PERMISSIONS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openAudioIntent(this)
            } else {
                showToast(getString(R.string.storage_permission_error))

            }

            Constants.Permission.CAMERA_STORAGE_PERMISSIONS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED
            ) {
                Intent(this, CameraActivity::class.java).apply {
                    putExtra(Constants.IntentKeys.ISVIDEOAVAILABLE, true)
                    startActivityForResult(this, Constants.RequestCode.CAMERA_INTENT)
                }
            } else {
                getString(R.string.camera_permission_error)

            }

            Constants.Permission.RECORD_AUDIO -> {

                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    ) {

                        showToast(getString(R.string.audio_permission_success))

                    } else {
                        showToast(getString(R.string.audio_permission_error))

                    }

                }

            }
        }
    }

    private fun createCall(callType: String) {

        val intent = Intent(this, VideoCallActivity::class.java)
            .putExtra(Constants.NotificationIntentValues.CALL_TYPE, callType)
            .putExtra(
                Constants.NotificationIntentValues.CALL_FROM,
                Constants.ChatTypes.OUTGOING_CALL
            )
            .putExtra(
                Constants.NotificationIntentValues.CHANNEL_ID,
                sharedHelper.kryptKey
            )
            .putExtra(Constants.IntentKeys.ROOMID, roomId)
            .putExtra(Constants.NotificationIntentValues.TO_ID, kryptCode)
            .putExtra(
                Constants.NotificationIntentValues.FROM_ID,
                sharedHelper.kryptKey
            )
            .putExtra(
                Constants.NotificationIntentValues.NAME,
                ""
            )
            .putExtra(
                Constants.NotificationIntentValues.IMAGE,
                image
            )

        callViewModel.createCall(
            sharedHelper.kryptKey, kryptCode, callType
        )?.observe(this, Observer {

            if (it.error == "true") {
                showSnack(it.message)
            } else if (it.error == "false") {
                NotificationUtils(this).notificationHangupCall(
                    getChatName(),
                    "outgoing $callType call"
                )
                intent.putExtra(Constants.NotificationIntentValues.ID, it.data?.id)
                startActivity(intent)
            }
        })

    }

    private fun getChatName(): String {
        return if (name == "") {
            kryptCode
        } else {
            name
        }
    }

    private fun showSnack(content: String) {
        showToast(content)
    }

    override fun onStop() {
        super.onStop()
//        Toast.makeText(this,"onStop",Toast.LENGTH_SHORT).show()
        removeAllNotification()
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        Toast.makeText(this,"onDestroyed",Toast.LENGTH_SHORT).show()
//    }

    fun removeAllNotification() {
//        val nManager =
//            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//        nManager.cancelAll()
    }

    private fun showKryptCodeDialog() {
        val dialogView = View.inflate(this, R.layout.dialog_add_contact, null)
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)
        val window: Window = dialog.window!!
        window.setGravity(Gravity.BOTTOM)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show()

        val backButton = dialog.findViewById<ImageView>(R.id.backIcon)

        val contactName = dialog.findViewById<EditText>(R.id.contactName)
        val code = dialog.findViewById<EditText>(R.id.enterKrypt)
        code.setText(kryptCode)

        backButton.setOnClickListener { dialog.dismiss() }

        val submitButton = dialog.findViewById<TextView>(R.id.submitButton)
        submitButton.setOnClickListener {
            val name = contactName.text.toString().trim()
            if (name.trim().isNotEmpty()) {
                chatListViewModel.updateUserName(kryptCode, name)
                chatName?.text = name
                addContactLayout?.hide()
                dialog.dismiss()
            } else {
                showToast(getString(R.string.enter_valid_contact_name))

            }

        }

    }

    private fun initEmoji() {
        emojipopup = EmojiPopup.Builder.fromRootView(rootView)
            .setOnEmojiPopupShownListener { smileyIcon.setImageResource(R.drawable.keyboard) }
            .setOnEmojiPopupDismissListener { smileyIcon.setImageResource(R.drawable.smiley_icon) }
            .setOnSoftKeyboardCloseListener { emojipopup?.dismiss() }
            .build(editTextMessage)

        smileyIcon.setOnClickListener {
            emojipopup?.toggle()
        }
    }

    fun onSaveClicked(view: View) {
        chatMessagesViewModel.onSaveClicked()
    }

    fun onDeleteClicked(view: View) {

        if (chatMessagesViewModel.deleteForEveryOne) {
            deleteMessageDialog(this, {
                chatMessagesViewModel.onDeleteClicked()
            }, {
                //delete selected Message
                chatMessagesViewModel.deleteForEveryOne()
            })
        } else {
            deleteMessage(this) {
                chatMessagesViewModel.onDeleteClicked()
            }
        }

    }

    private val MyBroadcastReceiver: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    "PRESENCE" -> {
                        val kryptId = intent.getStringExtra("kryptId")
                        if (kryptId == kryptCode) {
                            presenceInfo = intent.getStringExtra("presenceInfo")
                            println("LXMPP Presence for $kryptId is $presenceInfo")
                            if (!chatMessagesViewModel.getIsGroupChat()) {

                            }
//                                membersCount?.text = presenceInfo
                        }
                    }
                    "TYPING" -> {
                        val kryptId = intent.getStringExtra("kryptId")
                        if (kryptId == kryptCode) {
                            if (!chatMessagesViewModel.getIsGroupChat())
                                if (intent.getBooleanExtra("typing", false)) {
                                    membersCount?.text = getString(R.string.typing)
                                    println("LXMPP Typing started for $kryptId")
                                } else {
//                                    membersCount?.text = presenceInfo
                                }
                        }
                    }
                    else -> println("LXMPP Unkown Notification")
                }
            }
        }

    fun checkOnline() {

        val kryptId = kryptCode.toUpperCase()
        socket?.on("online_$kryptId") {

            runOnUiThread {
//                val value = JSONObject(it[0].toString())
//                Log.d("socket recived ", value.toString())
//                membersCount?.text = "Online"

                chatMessagesViewModel.updateStatus(kryptCode, 1, "")
            }
        }

        socket?.on("offline_$kryptId") {

            runOnUiThread {
                val value = JSONObject(it[0].toString())
                LogUtil.d("socket recived ", value.toString())

                if (value.has("lastLoginTime") && value.getString("lastLoginTime") != "") {
//                    membersCount?.text = "Last Seen At ${
//                    getFormatedDate(
//                        value.getString("lastLoginTime"),
//                        "yyyy-MM-dd'T'HH:mm:ss",
//                        "hh:mm a"
//                    )
//                    }"

                    chatMessagesViewModel.updateStatus(
                        kryptCode,
                        0,
                        value.getString("lastLoginTime")
                    )
                }
            }
        }

        if (membersCount?.text.toString() == getString(R.string.updating))
            Handler(Looper.getMainLooper()).postDelayed({
                if (membersCount?.text.toString() == getString(R.string.updating))
                    membersCount?.text = "Offline"
            }, 3000)

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
            checkOnline()
        }

        socket?.let {
            if (!it.connected())
                socket?.connect()
        }
    }

    fun onGalleryClicked(view: View) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(
//                Constants.Permission.READ_STORAGE_PERM_LIST,
//                Constants.Permission.READ_STORAGE_PERMISSIONS
//            )
//
//        } else {
//
//
//            openGallery(this)
//        }
//        startActivityForResult(
//            Intent(this, GalleryActivity::class.java),
//            Constants.RequestCode.GALLERY_INTENT
//        )

        layoutAttachment.hide()
    }

    fun onDocumentClicked(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                Constants.Permission.READ_STORAGE_PERM_LIST,
                Constants.Permission.READ_FILE_PERMISSIONS
            )

        } else {
            openFileIntent(this)
        }

        layoutAttachment.hide()
    }

    fun onCameraClicked(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                Constants.Permission.CAMERA_PERM_LIST,
                Constants.Permission.CAMERA_STORAGE_PERMISSIONS
            )
        } else {
            openCamera(this)
        }

        layoutAttachment.hide()
    }

    fun onAudioClicked(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                Constants.Permission.READ_STORAGE_PERM_LIST,
                Constants.Permission.READ_AUDIO_PERMISSIONS
            )
        } else {
            openAudioIntent(this)
        }

        layoutAttachment.hide()
    }

    fun onRecordClicked(view: View) {

        if (checkPermissions()) {
            layoutAttachment.toggleVisibility()
            val recordDialogFragment = AudioRecordDialogFragment.newInstance()
            recordDialogFragment.onFileRecordFinished(object :
                AudioRecordDialogFragment.RecordedFileListener {
                override fun onFinish(file: File) {
                    uploadAudioToAWS(file)
                }

            })
            recordDialogFragment.isCancelable = false
            recordDialogFragment.show(supportFragmentManager, "Record Audio")
        } else {
            requestPermissions()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.RequestCode.GALLERY_INTENT//gallery
            -> if (resultCode == Activity.RESULT_OK) {
                handleGallery(data)
            }
            Constants.RequestCode.CAMERA_INTENT//camera
            -> if (resultCode == Activity.RESULT_OK) {

                data?.let {
                    if (!it.getStringExtra(Constants.IntentKeys.FILE).isNullOrEmpty()) {
                        if (it.getBooleanExtra(Constants.IntentKeys.ISVIDEOAVAILABLE, false)) {
                            it.getStringExtra(Constants.IntentKeys.FILE)?.let { file ->
                                uploadFileToAws(File(file))
                            }

                        } else {
                            it.getStringExtra(Constants.IntentKeys.FILE)?.let { file ->
                                uploadFileToAws(File(file))
                            }
                        }

                    }
                }
            }
            Constants.RequestCode.FILE_INTENT
            -> if (resultCode == Activity.RESULT_OK) {
                handleFileData(data)
            }
            Constants.RequestCode.AUDIO_INTENT
            -> if (resultCode == Activity.RESULT_OK) {
                handleAudioData(data)
            }

        }
    }

    private fun handleGallery(data: Intent?) {
        if (data?.getStringExtra("type") != null) {
//            val uri = Uri.parse(data.getStringExtra("path"))
//            if (uri != null) {

            if (data.getStringExtra("type") == MessageType.DOCUMENT.toString().toLowerCase()) {

                val file = File(data?.getStringExtra("path"))
                uploadDocumentToAWS(file)

            } else {
                val file = File(data?.getStringExtra("path"))

                uploadFileToAws(file)
            }

        }
    }

    private fun handleFileData(data: Intent?) {

        if (data != null) {
            val uri = data.data
            if (uri != null) {

                val file = File(getPath(this, uri))

                uploadDocumentToAWS(file)

            } else {
                showToast(getString(R.string.unable_to_select))

            }
        }
    }

    private fun handleVideoData(data: File) {
        uploadDocumentToAWS(data)
    }

    private fun handleAudioData(data: Intent?) {
        if (data != null) {
            val uri = data.data
            if (uri != null) {

                val file = File(getPath(this, uri))

                uploadAudioToAWS(copyFile(this, file))

            } else {
                showToast(getString(R.string.unable_to_select))
            }
        }
    }

    private fun uploadDocumentToAWS(file: File) {

        if (file.isValidFileSize()) {
            chatMessagesViewModel.uploadDocument(file)
        }
    }

    private fun uploadAudioToAWS(file: File) {

        if (file.isValidFileSize()) {
            chatMessagesViewModel.uploadAudio(file)
        }
    }

    private fun handleCamera(file: File) {
        if (file.exists()) {

            uploadFileToAws(file)
        } else {
            showToast(getString(R.string.unable_to_retrieve))
        }
    }

    private fun uploadFileToAws(file: File) {

        if (file.isValidFileSize())
            chatMessagesViewModel.uploadToLocal(file)
        else
            showToast(getString(R.string.file_size_limit_exceeded))
    }

    override fun onBackPressed() {
        if (chatMessagesViewModel.isMultiSelectedEnabled) {
            chatMessagesViewModel.unselectAll()
        } else {
            super.onBackPressed()
        }
    }

    private fun checkPermissions(): Boolean {
        val storage = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val audio =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        return storage == PackageManager.PERMISSION_GRANTED && audio == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {

    }

    fun onForwardClicked(view: View) {
        val bundle = Bundle()
        bundle.putStringArrayList("list", chatMessagesViewModel.selectedChatMessage)
        openActivity(ForwardActivity::class.java, bundle)
    }

    override fun requestRecordPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                Constants.Permission.RECORD_AUDIO
            )
        }
    }

    override fun onRecordingStarted() {

        LogUtil.d(TAG, "onRecordingStarted")
        audioRecordView.cancelTxt?.visibility = View.GONE

        try {
            file = createFile(
                filesDir,
                AUDIO_EXTENSION
            )

            myAudioRecorder = MediaRecorder().apply {

                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(file.absolutePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                //setAudioSamplingRate(48000)

                try {
                    prepare()
                } catch (e: IOException) {
                    LogUtil.e("Record", "prepare() failed")
                }

                start()
            }
        } catch (ise: IllegalStateException) {
            ise.printStackTrace()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
        time = System.currentTimeMillis() / 1000
    }

    override fun onRecordingLocked() {
        LogUtil.d("ChatActivity", "onRecordingLocked")
        audioRecordView.cancelTxt?.visibility = View.VISIBLE
    }

    override fun onRecordingCompleted() {
        LogUtil.d(TAG, "onRecordingCompleted")
        audioRecordView.cancelTxt?.visibility = View.GONE
        val recordTime = (System.currentTimeMillis() / 1000 - time).toInt()
        if (recordTime > 1) {
            try {
                myAudioRecorder.stop()
                myAudioRecorder.release()
            } catch (ise: IllegalStateException) {
            } catch (ioe: IOException) {
            }
            uploadAudioToAWS(file)
        }
    }

    override fun onRecordingCanceled() {
        LogUtil.d(TAG, "onRecordingCanceled")
        audioRecordView.cancelTxt?.visibility = View.GONE
        try {
            myAudioRecorder.stop()
            myAudioRecorder.release()
        } catch (ise: IllegalStateException) {
        } catch (ioe: IOException) {
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun createFile(baseFolder: File, extension: String): File =
        File(
            baseFolder, SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + extension
        )

    override fun onDestroy() {
        super.onDestroy()
        chatMessagesViewModel.mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                it.release()
            }
        }
    }

    private fun startGroupChat(callType: String, channelName: String) {

        createRoomDialog?.dismiss()

//        NotificationUtils(this).notificationHangupCall(
//            chatMessagesViewModel.roomName,
//            "outgoing $callType call"
//        )

        val intent = Intent(this, GroupCallActivity::class.java)
        intent.putExtra(Constants.NotificationIntentValues.CHANNEL_ID, channelName)
        intent.putExtra(Constants.NotificationIntentValues.CALL_TYPE, callType)
        intent.putExtra(
            Constants.NotificationIntentValues.CALL_FROM,
            Constants.ChatTypes.OUTGOING_CALL
        )
        intent.putExtra(Constants.IntentKeys.ROOMID, roomId)
        startActivity(intent)
    }
}
