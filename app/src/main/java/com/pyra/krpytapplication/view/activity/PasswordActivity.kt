package com.pyra.krpytapplication.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.LogUtil
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.utils.openNewTaskActivity
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.repositories.interfaces.AuthenticationListner
import com.pyra.krpytapplication.viewmodel.ChatListViewModel
import com.pyra.krpytapplication.viewmodel.RegisterViewModel
import getImei
import isValidPassword
import kotlinx.android.synthetic.main.activity_password.*
import org.jetbrains.anko.doAsync
import showHidePass
import showToast
import java.text.SimpleDateFormat
import java.util.*

class PasswordActivity : AppCompatActivity() {

    lateinit var kryptCode: String
    var isBackEnabled = false
    var registerViewModel: RegisterViewModel? = null
    var chatListViewModel: ChatListViewModel? = null

    private val sharedHelper by lazy {
        SharedHelper(this@PasswordActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (SharedHelper(this).theme == "light") {
            setTheme(R.style.lightTheme)
        } else {
            setTheme(R.style.darkTheme)
        }
        setContentView(R.layout.activity_password)

        registerViewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)
        chatListViewModel = ViewModelProvider(this).get(ChatListViewModel::class.java)
        kryptCode = intent.getStringExtra("kryptCode") ?: ""
        isBackEnabled = intent.getBooleanExtra("isBackEnabled", false)
    }

    fun showHidePass(view: View) {

        if (view.id == R.id.hideShowPassword) {
            passwordField.showHidePass(view)
        }
    }

    fun onLoginButtonClicked(view: View) {
        val password = passwordField.text.toString()
        println("Login pressed with kryptCode $kryptCode and Password $password")

        isValidPassword(this, password, getString(R.string.password)).let {

            if (it == "true") {

                if (password == SharedHelper(this).duressPassword) {
                    clearAllData()
                    openNewTaskActivity(KryptCodeActivity::class.java)
                }

                registerViewModel?.loginUser(this, getImei(this), password, kryptCode)

                registerViewModel?.success?.observe(this, Observer {
                    if (it == "success") {
                        registerViewModel?.success?.value = ""
                        doAsync {
                            MyApp.xmppOperations.connectToXMPPServer(
                                kryptCode,
                                password,
                                object : AuthenticationListner {
                                    override fun isSuccess(status: Boolean) {
                                        if (status) {
                                            SharedHelper(this@PasswordActivity).loggedIn = true
                                            SharedHelper(this@PasswordActivity).showKryptScreen =
                                                false
                                            if (isBackEnabled) {
                                                updateLoginTime()

                                                val intent = Intent(
                                                    this@PasswordActivity,
                                                    DuressPasswordActivity::class.java
                                                )
                                                intent.putExtra(
                                                    Constants.IntentKeys.CHANGEPASSWORDFLOW,
                                                    false
                                                )
                                                intent.flags =
                                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                when (sharedHelper.autoLockType) {
                                                    "seconds" -> {
                                                        val calendar = Calendar.getInstance()
                                                        calendar.add(
                                                            Calendar.SECOND,
                                                            sharedHelper.autoLockTime
                                                        )
                                                        sharedHelper.autoLogoutSavedTime =
                                                            "${calendar.time.time}"
                                                    }

                                                    "minutes" -> {
                                                        val calendar = Calendar.getInstance()
                                                        calendar.add(
                                                            Calendar.MINUTE,
                                                            sharedHelper.autoLockTime
                                                        )
                                                        LogUtil.e(
                                                            "passwordActivity",
                                                            sharedHelper.autoLockTime.toString()
                                                        )
                                                        sharedHelper.autoLogoutSavedTime =
                                                            "${calendar.time.time}"
                                                    }

                                                    "hours" -> {
                                                        val calendar = Calendar.getInstance()
                                                        calendar.add(
                                                            Calendar.HOUR_OF_DAY,
                                                            sharedHelper.autoLockTime
                                                        )
                                                        sharedHelper.autoLogoutSavedTime =
                                                            "${calendar.time.time}"
                                                    }

                                                    "days" -> {
                                                        val calendar = Calendar.getInstance()
                                                        calendar.add(
                                                            Calendar.DAY_OF_YEAR,
                                                            sharedHelper.autoLockTime
                                                        )
                                                        sharedHelper.autoLogoutSavedTime =
                                                            "${calendar.time.time}"
                                                    }

                                                }
                                                startActivity(intent)
                                            } else
                                                when (sharedHelper.autoLockType) {
                                                    "seconds" -> {
                                                        val calendar = Calendar.getInstance()
                                                        calendar.add(
                                                            Calendar.SECOND,
                                                            sharedHelper.autoLockTime
                                                        )
                                                        sharedHelper.autoLogoutSavedTime =
                                                            "${calendar.time.time}"
                                                    }

                                                    "minutes" -> {
                                                        val calendar = Calendar.getInstance()
                                                        calendar.add(
                                                            Calendar.MINUTE,
                                                            sharedHelper.autoLockTime
                                                        )
                                                        LogUtil.e(
                                                            "passwordActivity",
                                                            sharedHelper.autoLockTime.toString()
                                                        )
                                                        sharedHelper.autoLogoutSavedTime =
                                                            "${calendar.time.time}"
                                                    }

                                                    "hours" -> {
                                                        val calendar = Calendar.getInstance()
                                                        calendar.add(
                                                            Calendar.HOUR_OF_DAY,
                                                            sharedHelper.autoLockTime
                                                        )
                                                        sharedHelper.autoLogoutSavedTime =
                                                            "${calendar.time.time}"
                                                    }

                                                    "days" -> {
                                                        val calendar = Calendar.getInstance()
                                                        calendar.add(
                                                            Calendar.DAY_OF_YEAR,
                                                            sharedHelper.autoLockTime
                                                        )
                                                        sharedHelper.autoLogoutSavedTime =
                                                            "${calendar.time.time}"
                                                    }

                                                }
                                            finish()
                                        }
                                    }

                                })
                        }
                    }
                })

                registerViewModel?.error?.observe(this, Observer {
                    showToast(it)
                })

                registerViewModel?.attempts?.observe(this, Observer {

                    if (it >= 6) {
                        chatListViewModel?.clearDb()
                        chatListViewModel?.removeCache()
                        chatListViewModel?.clearLocalStorage()
                        registerViewModel?.attempts?.value = 0
                    }
                })


            } else {
                showToast(getString(R.string.invalid_password))

            }
        }

    }

    override fun onBackPressed() {
        if (isBackEnabled)
            super.onBackPressed()
    }

    private fun updateLoginTime() {

        SharedHelper(this).loggedInTime =
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.ENGLISH
            ).format(Calendar.getInstance().time)
    }

    private fun clearAllData() {
        chatListViewModel?.clearDb()
        chatListViewModel?.removeCache()
        chatListViewModel?.clearLocalStorage()
    }
}
