package com.pyra.krpytapplication.view.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.utils.loadImage
import com.pyra.krpytapplication.utils.openNewTaskActivity
import com.pyra.krpytapplication.view.adapter.SelectedListAdapter
import com.pyra.krpytapplication.viewmodel.ChatListViewModel
import getRealPathFromURI
import getRealPathFromUriNew
import kotlinx.android.synthetic.main.activity_new_group_details.*
import kotlinx.android.synthetic.main.contact_lists.contactList
import openCamera
import openGalleryforPhoto
import showToast
import java.io.File

class NewGroupDetailsActivity : BaseActivity() {
    private lateinit var adapter: SelectedListAdapter
    var chatListViewModel: ChatListViewModel? = null
    var uploadFile: File? = null
    var sharedHelper = SharedHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_group_details)
        chatListViewModel = ViewModelProvider(this).get(ChatListViewModel::class.java)

        initAdapter()
        initListener()
    }

    private fun initListener() {

        nextButton.setOnClickListener {
            if (groupName.text.toString().trim().isNotEmpty()) {
                chatListViewModel?.uploadGroupImage(groupName.text.toString().trim(), uploadFile)
                progress.visibility = View.VISIBLE
                nextButton.isEnabled = false
            }

            chatListViewModel?.groupCreated?.observe(this, Observer {
                if (it)
                    openNewTaskActivity(MainActivity::class.java)
                else {
                    progress.visibility = View.GONE
                    nextButton.isEnabled = true
                }
            })

            chatListViewModel?.errorMessage?.observe(this, Observer {

                showToast(it)

            })
        }
    }

    fun onProfileImageClicked(view: View) {
        showEditImageDialog()
    }

    private fun initAdapter() {
        chatListViewModel?.getSelectedUsers()
        adapter = SelectedListAdapter(this, chatListViewModel)
        val contactListLayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        contactList.layoutManager = contactListLayoutManager
        contactList.adapter = adapter
    }

    private fun showEditImageDialog() {
        val dialogView = View.inflate(this, R.layout.dialog_edit_profile_image, null)
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        val window: Window = dialog.getWindow()!!
        window.setGravity(Gravity.BOTTOM)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show()

        dialog.findViewById<TextView>(R.id.takePhotoText).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    Constants.Permission.CAMERA_PERM_LIST,
                    Constants.Permission.CAMERA_STORAGE_PERMISSIONS
                )

            } else {
                openCamera(this)
            }
            dialog.dismiss()
        }
        dialog.findViewById<TextView>(R.id.galleryText).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    Constants.Permission.READ_STORAGE_PERM_LIST,
                    Constants.Permission.READ_STORAGE_PERMISSIONS
                )

            } else {
                openGalleryforPhoto(this)
            }
            dialog.dismiss()
        }
        dialog.findViewById<TextView>(R.id.trashText).setOnClickListener {
            uploadFile = null
            profileImage.setImageDrawable(getDrawable(R.drawable.camera_icon))
            dialog.dismiss()
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

    private fun handleGallery(data: Intent?) {
        if (data != null) {
            val uri = data.data
            if (uri != null) {

                profileImage.loadImage(getRealPathFromUriNew(this, uri))

                uploadFile = File(getRealPathFromURI(this, uri)!!)

//                uploadFileToAws()
            } else {
                showToast(getString(R.string.unable_to_select))

            }
        }
    }

    private fun handleCamera() {
        uploadFile = File(sharedHelper.imageUploadPath)
        if (uploadFile!!.exists()) {

            profileImage.loadImage(sharedHelper.imageUploadPath)
        } else {
            showToast(getString(R.string.unable_to_retrieve))

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
        }
    }

}
