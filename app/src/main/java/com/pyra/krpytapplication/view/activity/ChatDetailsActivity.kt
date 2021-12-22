package com.pyra.krpytapplication.view.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.*
import com.pyra.krpytapplication.databinding.ActivityChatDetailsBinding
import com.pyra.krpytapplication.notification.NotificationUtils
import com.pyra.krpytapplication.view.adapter.GroupMembersAdapter
import com.pyra.krpytapplication.view.fragment.EditProfileBottomSheet
import com.pyra.krpytapplication.viewmodel.AmazonViewModel
import com.pyra.krpytapplication.viewmodel.CallViewModel
import com.pyra.krpytapplication.viewmodel.ProfileViewModel
import getRealPathFromURI
import getRealPathFromUriNew
import kotlinx.android.synthetic.main.activity_chat_details.*
import kotlinx.android.synthetic.main.chat_details_content_layout.*
import kotlinx.android.synthetic.main.fragment_profile.*
import openCamera
import openGalleryforPhoto
import showToast
import java.io.File
import java.util.*

class ChatDetailsActivity : BaseActivity() {

    lateinit var groupMembersAdapter: GroupMembersAdapter
    lateinit var chatDetailsBinding: ActivityChatDetailsBinding
    lateinit var amazonViewModel: AmazonViewModel
    lateinit var profileViewModel: ProfileViewModel
    var roomId = ""
    var uploadFile: File? = null
    lateinit var sharedHelper: SharedHelper
    lateinit var profileImage: ImageView
    lateinit var progressBar: ProgressBar
    lateinit var imageDialog: Dialog

    private var isDeleteAvailable = false

    private val popup by lazy {
        PopupMenu(this, menuIcon)
    }
    private val callViewModel: CallViewModel by viewModels()
    private val deleteItem by lazy {
        popup.menu.findItem(R.id.delete)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat_details)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        amazonViewModel = ViewModelProvider(this).get(AmazonViewModel::class.java)
        sharedHelper = SharedHelper(this)
        popup.menuInflater.inflate(R.menu.chat_details_menu, popup.menu)
        initListener()
        getIntentValues()
        initAdapter()
        getProfileDetails()
        getProfileData()
        initObserver()
        chatDetailsBinding.profileViewModel = profileViewModel
        chatDetailsBinding.executePendingBindings()
    }

    private fun initListener() {

        audioCallLayout?.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    Constants.Permission.AUDIO_CALL_PERMISSION_LIST,
                    Constants.Permission.AUDIO_CALL_PERMISSION
                )
            } else {
                createCall(Constants.ChatTypes.VOICE_CALL)
            }
        }

        videoCallLayout?.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    Constants.Permission.VIDEO_CALL_PERMISSION_LIST,
                    Constants.Permission.VIDEO_CALL_PERMISSION
                )
            } else {
                createCall(Constants.ChatTypes.VIDEO_CALL)
            }
        }

        addMember.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AddGroupMemberActivity::class.java
                ).putExtra(Constants.IntentKeys.ROOMID, roomId)
            )
//            openActivity(AddGroupMemberActivity::class.java)
        }

        switch1.setOnCheckedChangeListener { a, b ->
            profileViewModel.setOnNotificationChanged(b, roomId)
            if (b) {
                notification.setText(R.string.on)
            } else {
                notification.setText(R.string.off)
            }
        }

        addUserIcon.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AddGroupMemberActivity::class.java
                ).putExtra(Constants.IntentKeys.ROOMID, roomId)
            )
        }

        exitGroup.setOnClickListener {
            leaveGroup()
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
            .putExtra(Constants.NotificationIntentValues.TO_ID, profileViewModel.kryptId.get())
            .putExtra(
                Constants.NotificationIntentValues.FROM_ID,
                sharedHelper.kryptKey
            )
            .putExtra(
                Constants.NotificationIntentValues.NAME,
                profileViewModel.profileName.get()
            )
            .putExtra(
                Constants.NotificationIntentValues.IMAGE,
                profileViewModel.image
            )


        callViewModel.createCall(
            sharedHelper.kryptKey, profileViewModel.kryptId.get()!!, callType
        )?.observe(this, Observer {

            val displayName = if (profileViewModel.profileName.get() == "") {
                profileViewModel.kryptId.get() ?: ""
            } else {
                profileViewModel.profileName.get() ?: ""
            }

            if (it.error == "true") {
                showSnack(it.message)
            } else if (it.error == "false") {
                NotificationUtils(this).notificationHangupCall(
                    displayName,
                    "outgoing $callType call"
                )
                intent.putExtra(Constants.NotificationIntentValues.ID, it.data?.id)
                startActivity(intent)
            }
        })

    }

    private fun showSnack(message: String) {
        Snackbar.make(chatDetailsBinding.parent, message, Snackbar.LENGTH_LONG).show()
    }

    private fun getProfileData() {
        profileViewModel.getParticipationList(roomId)
    }

    private fun getProfileDetails() {
        profileViewModel.getProfileDetails(roomId)
    }

    private fun initObserver() {
        profileViewModel.notifiAdapter?.observe(this, Observer {
            groupMembersAdapter.notifyDataSetChanged()
        })

        profileViewModel.refreshView.observe(this, Observer {
            chatDetailsBinding.profileImage.loadImage(profileViewModel.getRoomImage())
        })

        profileViewModel.updateBlockList.observe(this, Observer { isBlocked ->
            setMenuInflater(isBlocked)
        })

        profileViewModel.getUser(roomId)?.observe(this, Observer {
            it?.roomName?.let { name ->
                //titleTxt.text = it.roomName
                deleteItem.isVisible = !it.roomName.isNullOrEmpty()
                switch1.isChecked = it.showNotification!!

                if (it.showNotification!!) {
                    notification.setText(R.string.on)
                } else {
                    notification.setText(R.string.off)
                }
            }
        })

    }

    private fun getIntentValues() {

        intent.extras?.let {
            it.getString(Constants.IntentKeys.ROOMID)?.let { value ->
                roomId = value
            }
        }

    }

    private fun initAdapter() {

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        groupMembersAdapter = GroupMembersAdapter(this, profileViewModel) { position ->
            removeUserDialog(this) {
                profileViewModel.removeUser(position)
            }
        }
        memberLists.layoutManager = linearLayoutManager
        memberLists.adapter = groupMembersAdapter
    }

    fun onBackButtonPressed(view: View) {
        onBackPressed()
    }

    fun onEditNameClicked(view: View) {

        val bottomSheetDialogFragment = EditProfileBottomSheet.newInstance()
        bottomSheetDialogFragment.arguments = bundleOf(Constants.IntentKeys.ROOMID to roomId)
        bottomSheetDialogFragment.show(supportFragmentManager, "BOTTOM_SHEET")

//        val dialog = showEditNameDialog(this)
//        val submit = dialog.findViewById<CardView>(R.id.buttonBg)
//        val backIcon = dialog.findViewById<ImageView>(R.id.backIcon)
//        val name = dialog.findViewById<EditText>(R.id.kryptCode)
//
//        name.setText(profileViewModel.profileName.get())
//
//        backIcon.setOnClickListener {
//            dialog.dismiss()
//        }
//
//        submit.setOnClickListener {
//            if (name.text.toString().trim().isNotEmpty()) {
//                profileViewModel.updateGroupProfileTitle(name.text.toString().trim())
//                profileViewModel.isUpdated.observe(this, Observer {
//                    if (dialog != null && dialog.isShowing)
//                        dialog.dismiss()
//                    initAdapter()
//                    getProfileDetails()
//                })
//            }
//
//        }
    }

    fun onImageCliked(view: View) {

        profileViewModel.isGroup.get()?.let {
            if (it) {

                imageDialog = showImageDialog(this)
                val editIcon = imageDialog.findViewById<ImageView>(R.id.editIcon)

                profileImage = imageDialog.findViewById(R.id.profileImage)
                progressBar = imageDialog.findViewById(R.id.progressBar)


                profileImage.loadImage(profileViewModel.profileImage.value)

                editIcon.setOnClickListener {
                    showPictureDialog(this)
                }

            }
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
                handleCamera()
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {

            Constants.Permission.READ_STORAGE_PERMISSIONS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGalleryforPhoto(this)
            } else {
                showToast(getString(R.string.storage_permission_error))
            }

            Constants.Permission.CAMERA_STORAGE_PERMISSIONS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera(this)
            } else {
                showToast(getString(R.string.camera_permission_error))

            }

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

        }
    }

    private fun handleGallery(data: Intent?) {
        if (data != null) {
            val uri = data.data
            if (uri != null) {

                userImage.loadImage(getRealPathFromUriNew(this, uri))

                uploadFile = File(getRealPathFromURI(this, uri)!!)

                uploadFileToAws()
            } else {
                showToast(getString(R.string.unable_to_select))
            }
        }
    }

    private fun handleCamera() {
        uploadFile = File(sharedHelper.imageUploadPath)
        if (uploadFile!!.exists()) {

            userImage.loadImage(sharedHelper.imageUploadPath)
            uploadFileToAws()
        } else {
            showToast(getString(R.string.unable_to_retrieve))

        }
    }

    fun uploadFileToAws() {

        uploadFile?.let { file ->
            progressBar.visibility = View.VISIBLE
            profileImage.loadImage(uploadFile?.absolutePath)
            amazonViewModel.uploadProfileImage(this, file)
        }

        amazonViewModel.amazonGroupUrl.observe(this, Observer {
            if (it != "") {
                progressBar.visibility = View.GONE
                profileViewModel.updateGroupProfileImage(it)
                profileViewModel.isUpdated.observe(this, Observer {
                    if (imageDialog.isShowing)
                        imageDialog.dismiss()
                    initAdapter()
                    getProfileDetails()
                })
            }
        })

        amazonViewModel.errorMessage.observe(this, Observer {
            progressBar.visibility = View.GONE
            if (it != "") {
                showToast(it)

            }
        })

    }

    private fun setMenuInflater(blocked: Boolean) {


        val item: MenuItem = popup.menu.findItem(R.id.block)

        if (blocked) {
            item.title = getString(R.string.unblock)
        } else {
            item.title = getString(R.string.block)
        }

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.block -> {
                    profileViewModel.blockAction()
                }
            }
            return@setOnMenuItemClickListener true
        }

        deleteItem.setOnMenuItemClickListener {

            var dialog = getDeleteContactDialog(this)

            dialog.findViewById<TextView>(R.id.delete).setOnClickListener {
                profileViewModel.updateUser(roomId, "")
                dialog.dismiss()
            }

            dialog.findViewById<TextView>(R.id.cancel).setOnClickListener { dialog.dismiss() }

            return@setOnMenuItemClickListener true
        }

        menuIcon.setOnClickListener {
            popup.show()
        }

    }

    private fun leaveGroup() {

        exitGroupDialog(this) {
            profileViewModel.leaveGroup(roomId)?.observe(this, Observer {
                it.error.let { error ->
                    if (error == "false") {
                        profileViewModel.removeFromGroupDB(
                            roomId
                        )

                        openNewTaskActivity(MainActivity::class.java)
                    }
                }
            })
        }

    }
}
