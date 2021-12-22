package com.pyra.krpytapplication.view.fragment

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.LogUtil
import com.pyra.krpytapplication.view.activity.CameraActivity
import com.pyra.krpytapplication.view.activity.GalleryImageActivity
import showToast
import java.io.File

open class BaseFragment(layout: Int) : Fragment(layout) {

    fun openGallery() {
        Intent(requireContext(), GalleryImageActivity::class.java).apply {
            startActivityForResult(this, Constants.RequestCode.GALLERY_INTENT)
        }
    }

    fun imageOption() {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(activity?.getString(R.string.choose_your_option))
        val items =
            arrayOf(activity?.getString(R.string.gallery), activity?.getString(R.string.camera))

        builder.setItems(items) { _, which ->
            when (which) {
                0 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        Constants.Permission.READ_STORAGE_PERM_LIST,
                        Constants.Permission.READ_STORAGE_PERMISSIONS
                    )

                } else {
                    val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    i.type = "image/*";
                    activity?.startActivityForResult(i, Constants.RequestCode.GALLERY_INTENT)
                }
                1 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                        Constants.Permission.CAMERA_PERM_LIST,
                        Constants.Permission.CAMERA_STORAGE_PERMISSIONS
                    )

                } else {
                    Intent(activity, CameraActivity::class.java).apply {
                        activity?.startActivityForResult(this, Constants.RequestCode.CAMERA_INTENT)
                    }
                }
            }

        }
        val option = builder.create()
        val window = option.window
        if (window != null) {
            window.attributes.windowAnimations = R.style.DialogAnimation
        }
        option.show()
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
                LogUtil.e("result", "true")
                if (!data?.getStringExtra(Constants.IntentKeys.FILE).isNullOrEmpty()) {
                    handleCamera(File(data?.getStringExtra(Constants.IntentKeys.FILE)!!))
                }
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {

            Constants.Permission.READ_STORAGE_PERMISSIONS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                i.type = "image/*";
                startActivityForResult(i, Constants.RequestCode.GALLERY_INTENT)
            } else {
                requireContext().showToast(getString(R.string.storage_permission_error))

            }

            Constants.Permission.CAMERA_STORAGE_PERMISSIONS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED
            ) {

                Intent(activity, CameraActivity::class.java).apply {
                    putExtra(Constants.IntentKeys.ISVIDEOAVAILABLE, false)
                    startActivityForResult(
                        this,
                        Constants.RequestCode.CAMERA_INTENT
                    )
                }

            } else {
                requireContext().showToast(getString(R.string.camera_permission_error))


            }
        }

    }

    open fun handleGallery(data: Intent?) {

    }

    open fun handleCamera(file: File) {

    }

}