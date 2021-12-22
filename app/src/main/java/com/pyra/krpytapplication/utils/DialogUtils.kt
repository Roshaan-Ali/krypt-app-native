package com.pyra.krpytapplication.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.view.activity.CameraActivity
import jp.wasabeef.blurry.Blurry
import openGalleryforPhoto
import java.util.*
import kotlin.math.roundToInt


fun showPictureDialog(activity: Activity) {

    val alertBuilder = AlertDialog.Builder(activity)
    alertBuilder.setTitle(activity.getString(R.string.choose_your_option))
    val items =
        arrayOf(activity.getString(R.string.gallery), activity.getString(R.string.camera))

    alertBuilder.setItems(items) { _, which ->
        when (which) {
            0 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(
                    Constants.Permission.READ_STORAGE_PERM_LIST,
                    Constants.Permission.READ_STORAGE_PERMISSIONS
                )

            } else {
                openGalleryforPhoto(activity)
            }
            1 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(
                    Constants.Permission.CAMERA_PERM_LIST,
                    Constants.Permission.CAMERA_STORAGE_PERMISSIONS
                )

            } else {
                Intent(activity, CameraActivity::class.java).apply {
                    activity.startActivityForResult(this, Constants.RequestCode.CAMERA_INTENT)
                }
            }
        }
    }

    val alert = alertBuilder.create()
    val window = alert.window
    if (window != null) {
        window.attributes.windowAnimations = R.style.DialogAnimation
    }
    alert.show()
}


fun showPasswordDialog(
    context: Context,
    vault: () -> Unit,
    duress: () -> Unit
) {

    val dialogView = View.inflate(context, R.layout.dialog_password, null)
    val dialog = Dialog(context)

    val password = dialogView.findViewById<EditText>(R.id.password)
    val buttonBg = dialogView.findViewById<CardView>(R.id.buttonBg)

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(dialogView)
    val window: Window = dialog.window!!
    window.setGravity(Gravity.BOTTOM)
    window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)


    buttonBg.setOnClickListener {
        if (password.text.trim().toString() != "") {

            when {
                password.text.trim().toString() == SharedHelper(context).vaultPassword -> {

                    dialog.dismiss()
                    vault()
                }
                password.text.trim().toString() == SharedHelper(context).duressPassword -> {
                    duress()
                    dialog.dismiss()
                }
                else -> {
                    context.getString(R.string.invalid_password)

                }
            }

        } else

            context.getString(R.string.invalid_password)
    }


    dialog.show()

}

fun removeUserDialog(
    context: Context,
    onOkClicked: () -> Unit
): Dialog {

    val dialogView = View.inflate(context, R.layout.dialog_remove_user, null)
    val dialog = Dialog(context)

    val ok = dialogView.findViewById<TextView>(R.id.ok)
    val cancel = dialogView.findViewById<TextView>(R.id.cancel)

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(dialogView)
    val window: Window = dialog.window!!
    window.setGravity(Gravity.BOTTOM)
    window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    ok.setOnClickListener {
        onOkClicked()
        dialog.dismiss()
    }

    cancel.setOnClickListener {
        dialog.dismiss()
    }

    dialog.show()
    return dialog
}


fun deleteAllChat(
    context: Context,
    onOkClicked: () -> Unit
): Dialog {

    val dialogView = View.inflate(context, R.layout.dialog_delete_all_chat, null)
    val dialog = Dialog(context)

    val ok = dialogView.findViewById<Button>(R.id.ok)
    val cancel = dialogView.findViewById<TextView>(R.id.cancel)
    val close = dialogView.findViewById<RelativeLayout>(R.id.backIcon)

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(dialogView)
    val window: Window = dialog.window!!
    window.setGravity(Gravity.CENTER)
    window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    val width = (context.resources.displayMetrics.widthPixels * 0.8).roundToInt()
    var height = (context.resources.displayMetrics.heightPixels * 0.25).roundToInt()
    //window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

    ok.setOnClickListener {
        onOkClicked()
        dialog.dismiss()
    }

    cancel.setOnClickListener {
        dialog.dismiss()
    }
    close.setOnClickListener {
        dialog.dismiss()
    }

    dialog.show()
    return dialog
}

fun showLoader(context: Context): Dialog {

    val loaderDialog = Dialog(context)
    loaderDialog.setCancelable(false)
    loaderDialog.setCanceledOnTouchOutside(false)
    loaderDialog.window?.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT
    )
    loaderDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    val inflater = LayoutInflater.from(context)
    val view = inflater?.inflate(R.layout.dialog_loader, null)
    if (view != null) {
        loaderDialog.setContentView(view)
    }

//    loaderDialog!!.anim_view.playAnimation()

    return loaderDialog
}

fun showEditNameDialog(context: Context): Dialog {

    val dialogView = View.inflate(context, R.layout.dialog_edit_groupname, null)
    val dialog = Dialog(context)


    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(dialogView)
    val window: Window = dialog.window!!
    window.setGravity(Gravity.BOTTOM)
    window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    dialog.show()
    return dialog
}


fun showImageDialog(context: Context): Dialog {

    val dialogView = View.inflate(context, R.layout.dialog_edit_group_image, null)
    val dialog = Dialog(context)


    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(dialogView)
    val window: Window = dialog.window!!
    window.setGravity(Gravity.BOTTOM)
    window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    dialog.show()
    return dialog
}

fun dismissLoader(loaderDialog: Dialog?) {
    if (loaderDialog != null) {
        if (loaderDialog.isShowing) {
            loaderDialog.dismiss()
        }
    }
}


fun getChatDeleteDialog(context: Context): Dialog {

    val dialogView = View.inflate(context, R.layout.dialog_delete_chat, null)
    val dialog = Dialog(context)


    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(dialogView)
    val window: Window = dialog.window!!
    window.setGravity(Gravity.CENTER)
    window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    dialog.show()
    return dialog
}

fun getDeleteContactDialog(context: Context): Dialog {

    val dialogView = View.inflate(context, R.layout.dialog_delete_contact, null)
    val dialog = Dialog(context)


    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(dialogView)
    val window: Window = dialog.window!!
    window.setGravity(Gravity.CENTER)
    window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    var width = (context.resources.displayMetrics.widthPixels * 0.8).roundToInt()
    window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

    dialog.show()
    return dialog
}


fun getMessageBurnDialog(context: Context, rootLayout: ViewGroup): Dialog {
    val dialogView = View.inflate(context, R.layout.dialog_burnmessage, null)
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(dialogView)
    dialog.setCancelable(true)
    dialog.setCanceledOnTouchOutside(true)
    val window: Window = dialog.window!!
    window.setGravity(Gravity.CENTER)
    window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    window.setDimAmount(0.0f)
    Blurry.with(context).radius(10).sampling(2).onto(rootLayout)
    dialog.setOnDismissListener {
        Blurry.delete(rootLayout)
    }

    val width = (context.resources.displayMetrics.widthPixels * 0.85).roundToInt()
    var height = (context.resources.displayMetrics.heightPixels * 0.25).roundToInt()
    //window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

    dialog.show()
    return dialog
}

fun getChangeThemeDialog(context: Context, onClick: () -> Unit): Dialog {

    var sharedHelper = SharedHelper(context)

    val dialogView = View.inflate(context, R.layout.dialog_change_theme, null)
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(dialogView)
    dialog.setCancelable(true)
    dialog.setCanceledOnTouchOutside(true)
    val window: Window = dialog.window!!
    window.setGravity(Gravity.CENTER)
    window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    var width = (context.resources.displayMetrics.widthPixels * 0.8).roundToInt()
    window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

    dialog.findViewById<View>(R.id.lightTheme).setOnClickListener {
        if (sharedHelper.theme == "dark" || sharedHelper.theme == "") {
            sharedHelper.theme = "light"
            onClick()
            dialog.dismiss()
        } else {
            dialog.dismiss()
        }
    }

    dialog.findViewById<View>(R.id.darkTheme).setOnClickListener {
        if (sharedHelper.theme == "light" || sharedHelper.theme == "") {
            sharedHelper.theme = "dark"
            onClick()
            dialog.dismiss()
        } else {
            dialog.dismiss()
        }

    }

    dialog.show()
    return dialog
}

fun getCreateRoomDialog(
    context: Context
): BottomSheetDialog? {

    val dialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(R.layout.dialog_create_roomcall)
    val window = dialog.window
    window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//    val mBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(
//        dialog.findViewById<ConstraintLayout>(R.id.rootView) as View
//    )
//    mBehavior.peekHeight = (context.resources.displayMetrics.heightPixels * 0.4).toInt()

    return dialog
}


fun exitGroupDialog(
    context: Context,
    onOkClicked: () -> Unit
): Dialog {

    val dialogView = View.inflate(context, R.layout.dialog_remove_user, null)
    val dialog = Dialog(context)

    val ok = dialogView.findViewById<TextView>(R.id.ok)
    val cancel = dialogView.findViewById<TextView>(R.id.cancel)
    val title2 = dialogView.findViewById<TextView>(R.id.title2)

    title2.text = context.getString(R.string.left_grp_msg)
    ok.text = context.getString(R.string.leave)

    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(dialogView)
    val window: Window = dialog.window!!
    window.setGravity(Gravity.BOTTOM)
    window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    ok.setOnClickListener {
        onOkClicked()
        dialog.dismiss()
    }

    cancel.setOnClickListener {
        dialog.dismiss()
    }

    dialog.show()
    return dialog

}


inline fun deleteMessageDialog(
    context: Context, crossinline deleteforme: () -> Unit,
    crossinline deleteForEveryOne: () -> Unit
) {
    val items = arrayOf(
        context.getString(R.string.delete_for_me), context.getString(R.string.cancel).toUpperCase(
            Locale.ROOT
        ), context.getString(R.string.delete_for_everyone)
    )
    MaterialAlertDialogBuilder(context)
        .setTitle(context.resources.getString(R.string.delete_message))
        .setItems(items) { dialog, which ->
            when (which) {
                0 -> {
                    deleteforme()
                }
                1 -> {
                    dialog.dismiss()
                }

                2 -> {
                    deleteForEveryOne()
                }
            }
        }.show()
}


inline fun deleteMessage(context: Context, crossinline deleteforme: () -> Unit) {
    MaterialAlertDialogBuilder(context)
        .setTitle(context.resources.getString(R.string.delete_message))
        .setPositiveButton(context.getString(R.string.delete_for_me)) { dialog, which ->
            deleteforme()
        }

        .setNegativeButton(context.getString(R.string.cancel)) { dialog, which ->

            dialog.dismiss()
        }
        .show()

}

fun subEnded(context: Context) {


    MaterialAlertDialogBuilder(context)
        .setMessage(context.getString(R.string.account_deactivated))
        .show()

}

