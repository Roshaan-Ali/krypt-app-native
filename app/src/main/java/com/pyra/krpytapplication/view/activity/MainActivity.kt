package com.pyra.krpytapplication.view.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.*
import com.pyra.krpytapplication.domain.OnClickButtonListener
import com.pyra.krpytapplication.notification.NotificationUtils
import com.pyra.krpytapplication.videocallutils.events.ConstantApp
import com.pyra.krpytapplication.view.fragment.*
import com.pyra.krpytapplication.viewmodel.CallViewModel
import com.pyra.krpytapplication.viewmodel.ChatListViewModel
import getImei
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_password.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_chat.rootLayout
import showHidePass
import showToast
import kotlin.properties.Delegates

class MainActivity : BaseActivity(), OnClickButtonListener {
    private val callViewModel: CallViewModel by viewModels()
    var sharedHelper: SharedHelper? = null

    private val chatListViewModel: ChatListViewModel by viewModels()

    private lateinit var profileFragment: ProfileFragment
    private lateinit var vaultFragment: VaultFragment
    private lateinit var moreMenuFragment: MoreMenuFragment

    var selectedItemId: Int = 0

    private val vaultPassDialogFragment: VaultPassDialogFragment by lazy {
        VaultPassDialogFragment()
    }
    private val chatFragment: ChatFragment by lazy {
        ChatFragment()
    }

    val fm: FragmentManager = supportFragmentManager
    var active: Fragment = chatFragment

    var lastSelectedTab: Int by Delegates.observable(R.id.chat) { _, _, _ -> }
    var isVerified = false

    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedHelper = SharedHelper(this)
        updateDeviceToken()

        chatSelected()
        fm.beginTransaction().add(R.id.nav_host, chatFragment, "ChatFragment").commit()
        FirebaseAnalytics.getInstance(this)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val intent = Intent()
//            val packageName = packageName
//            val pm = getSystemService(POWER_SERVICE) as PowerManager
//            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
//                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
//                intent.data = Uri.parse("package:$packageName")
//                startActivity(intent)
//            }
//        }
    }

    private fun chatSelected() {
        bottomView.background = ContextCompat.getDrawable(this, R.drawable.round_bottom_view_bg)
        chatLayout.background = ContextCompat.getDrawable(this, R.drawable.selected_round_bg)
        chatIcon.setIconColor(this, R.color.yellow)

        profileLayout.background = ContextCompat.getDrawable(this, R.drawable.unselected_round_bg)
        profileIcon.setIconColor(this, R.color.white)

        vaultLayout.background = ContextCompat.getDrawable(this, R.drawable.unselected_round_bg)
        vaultIcon.setIconColor(this, R.color.white)

        moreMenuLayout.background = ContextCompat.getDrawable(this, R.drawable.unselected_round_bg)
        moreMenuIcon.setIconColor(this, R.color.white)
    }

    private fun profileSelected() {
        bottomView.background = ContextCompat.getDrawable(this, R.drawable.round_bottom_view_bg)
        chatLayout.background = ContextCompat.getDrawable(this, R.drawable.unselected_round_bg)
        chatIcon.setIconColor(this, R.color.white)

        profileLayout.background = ContextCompat.getDrawable(this, R.drawable.selected_round_bg)
        profileIcon.setIconColor(this, R.color.yellow)

        vaultLayout.background = ContextCompat.getDrawable(this, R.drawable.unselected_round_bg)
        vaultIcon.setIconColor(this, R.color.white)

        moreMenuLayout.background = ContextCompat.getDrawable(this, R.drawable.unselected_round_bg)
        moreMenuIcon.setIconColor(this, R.color.white)
    }

    private fun vaultSelected() {
        bottomView.background = null
        chatLayout.background = ContextCompat.getDrawable(this, R.drawable.unselected_round_bg)
        chatIcon.setIconColor(this, R.color.white)

        profileLayout.background = ContextCompat.getDrawable(this, R.drawable.unselected_round_bg)
        profileIcon.setIconColor(this, R.color.white)

        vaultLayout.background = ContextCompat.getDrawable(this, R.drawable.selected_round_bg)
        vaultIcon.setIconColor(this, R.color.yellow)

        moreMenuLayout.background = ContextCompat.getDrawable(this, R.drawable.unselected_round_bg)
        moreMenuIcon.setIconColor(this, R.color.white)
    }

    private fun moreMenuSelected() {
        bottomView.background = ContextCompat.getDrawable(this, R.drawable.round_bottom_view_bg)
        chatLayout.background = ContextCompat.getDrawable(this, R.drawable.unselected_round_bg)
        chatIcon.setIconColor(this, R.color.white)

        profileLayout.background = ContextCompat.getDrawable(this, R.drawable.unselected_round_bg)
        profileIcon.setIconColor(this, R.color.white)

        vaultLayout.background = ContextCompat.getDrawable(this, R.drawable.unselected_round_bg)
        vaultIcon.setIconColor(this, R.color.white)

        moreMenuLayout.background = ContextCompat.getDrawable(this, R.drawable.selected_round_bg)
        moreMenuIcon.setIconColor(this, R.color.yellow)
    }

    fun onMenuClick(view: View) {
        when (view.id) {
            R.id.chatLayout -> {
                LogUtil.d("MainNavigation","Chat")
                chatSelected()
                fm.beginTransaction().hide(active).show(chatFragment).commit()
                active = chatFragment
                lastSelectedTab = R.id.chat
            }

            R.id.profileLayout -> {
                LogUtil.d("MainNavigation","Profile")
                profileSelected()
                if (this@MainActivity::profileFragment.isInitialized) {
                    fm.beginTransaction().hide(active).show(profileFragment).commit()
                    active = profileFragment
                    lastSelectedTab = R.id.profile
                } else {
                    profileFragment = ProfileFragment()
                    fm.beginTransaction().add(R.id.nav_host, profileFragment, "ProfielFragment")
                        .hide(active).commit()
                    active = profileFragment
                    lastSelectedTab = R.id.profile
                }

            }

            R.id.vaultLayout -> {
                LogUtil.d("MainNavigation","Vault")
                if (sharedHelper?.vaultPasswordEnabled!!) {
                    if (isVerified) {
                        vaultSelected()
                        if (this@MainActivity::vaultFragment.isInitialized) {
                            fm.beginTransaction().hide(active).show(vaultFragment).commit()
                            active = vaultFragment
                            lastSelectedTab = R.id.vault
                        } else {
                            vaultFragment = VaultFragment()
                            fm.beginTransaction()
                                .add(R.id.nav_host, vaultFragment, "VaultFragment")
                                .hide(active).commit()
                            active = vaultFragment
                            lastSelectedTab = R.id.vault
                        }

                        isVerified = false

                    } else {
                        showVaultPasswordDialogDialog()
//                        if (!vaultPassDialogFragment.isAdded) {
//                            vaultPassDialogFragment.show(
//                                supportFragmentManager,
//                                "vaultPassDialogFragment"
//                            )
//                        }
                    }
                } else {
                    vaultSelected()
                    if (this@MainActivity::vaultFragment.isInitialized) {
                        fm.beginTransaction().hide(active).show(vaultFragment).commit()
                        active = vaultFragment
                        lastSelectedTab = R.id.vault
                    } else {
                        vaultFragment = VaultFragment()
                        fm.beginTransaction()
                            .add(R.id.nav_host, vaultFragment, "VaultFragment")
                            .hide(active).commit()
                        active = vaultFragment
                        lastSelectedTab = R.id.vault
                    }

                }
            }
            R.id.moreMenuLayout -> {
                LogUtil.d("MainNavigation","MoreMenu")
                moreMenuSelected()
                if (this@MainActivity::moreMenuFragment.isInitialized) {
                    fm.beginTransaction().hide(active).show(moreMenuFragment).commit()
                    active = moreMenuFragment
                    lastSelectedTab = R.id.moreMenuFragment
                } else {
                    moreMenuFragment = MoreMenuFragment()
                    fm.beginTransaction()
                        .add(R.id.nav_host, moreMenuFragment, "MoreMenuFragment")
                        .hide(active).commit()
                    active = moreMenuFragment
                    lastSelectedTab = R.id.moreMenuFragment
                }
            }
        }
    }

    private fun getGroupDetails() {
        chatListViewModel.getGroupDetails()
        chatListViewModel.getProfileImages()
    }

    override fun onResume() {
        super.onResume()
//        updateLoginTime()
        getGroupDetails()
        NotificationUtils(this).removeNotification()

        askPermissions()
    }

    private fun askPermissions() {

        checkSelfPermission(
            Manifest.permission.RECORD_AUDIO,
            ConstantApp.PERMISSION_REQ_ID_RECORD_AUDIO
        )

        checkSelfPermission(
            Manifest.permission.CAMERA,
            ConstantApp.PERMISSION_REQ_ID_CAMERA
        )

        checkSelfPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ConstantApp.PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE
        )
    }

    fun checkSelfPermission(permission: String, requestCode: Int) {
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
        }
    }

    private fun updateDeviceToken() {
        LogUtil.d("krypt ", sharedHelper?.kryptKey)
        callViewModel.updateDeviceToken(
            "android",
            getImei(this),
            sharedHelper?.firebaseToken!!,
            sharedHelper?.kryptKey
        )
    }

    override fun onClickListener() {
        openActivity(ContactActivity::class.java)
    }

    private fun showVaultPasswordDialogDialog() {
        val dialogView = View.inflate(this, R.layout.dialog_password, null)
        val dialog = Dialog(this)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.password)
        val submitButton = dialogView.findViewById<Button>(R.id.submitButton)
        val hideShowPassword = dialogView.findViewById<ImageView>(R.id.hideShowPassword)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)
        val window: Window = dialog.window!!
        window.setGravity(Gravity.CENTER)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        window.setDimAmount(0.0f)
        Blurry.with(this).radius(10).sampling(2).onto(rootLayout)

        dialog.setOnDismissListener {
            Blurry.delete(rootLayout)
        }

        //window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.show()

        val backButton = dialog.findViewById<RelativeLayout>(R.id.backIcon)
        backButton.setOnClickListener { dialog.dismiss() }

        hideShowPassword.setOnClickListener {
            passwordEditText.showHidePass(it)
        }

        submitButton.clickWithDebounce {
            if (passwordEditText.text.trim().toString() != "") {
                when {
                    passwordEditText.text.trim()
                        .toString() == SharedHelper(this).vaultPassword -> {
                        isVerified = true
                        selectedItemId = R.id.vault
                        passwordEditText.setText("")
                        dialog.dismiss()
                    }
                    passwordEditText.text.trim()
                        .toString() == SharedHelper(this).duressPassword -> {
                        chatListViewModel.clearDb()
                        chatListViewModel.removeCache()
                        chatListViewModel.clearLocalStorage()
                        openNewTaskActivity(KryptCodeActivity::class.java)
                        dialog.dismiss()
                    }
                    else -> {

                        dialog.window?.decorView?.let {

                            showToast(this.getString(R.string.invalid_password))

                        }

                    }
                }

            } else
                dialog.window?.decorView?.let {
                    showToast(this.getString(R.string.invalid_password))
                }
        }

    }

}
