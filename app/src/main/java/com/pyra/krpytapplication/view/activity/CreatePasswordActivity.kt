package com.pyra.krpytapplication.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.viewmodel.RegisterViewModel
import generateKryptCode
import getImei
import isValidPassword
import kotlinx.android.synthetic.main.activity_create_password.*
import kotlinx.android.synthetic.main.activity_create_password.password
import showHidePass
import showToast

class CreatePasswordActivity : BaseActivity() {

    var registerViewModel: RegisterViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_password)

        registerViewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)
    }

    fun showHidePass(view: View) {

        if (view.id == R.id.hideShowPassword) {
            password.showHidePass(view)
        } else if (view.id == R.id.hideShowConfirmPassword) {
            confirmPassword.showHidePass(view)
        }
    }

    fun onSubmitButtonClicked(view: View) {

        isValidPassword(this, password.text.toString(), getString(R.string.password)).let {
            when {
                it != "true" -> {
                    showToast(it)

                }
                password.text.toString() != confirmPassword.text.toString() -> {
                    showToast(getString(R.string.passwords_do_not_match))
                }
                else -> {
                    val imei = getImei(this)
                    val password = password.text.toString()
                    val kryptCode = generateKryptCode()
                    println("IMEI $imei")
                    //            XMPPOperations().registerNewUser(kryptCode,password,imei)
                    registerUser(imei, password, kryptCode)
                    //            openActivity(MainActivity::class.java)
                }
            }
        }

    }

    private fun registerUser(imei: String, password: String, kryptCode: String) {

        registerViewModel?.registerUser(this, imei, kryptCode, password)?.observe(this, Observer {

            it.error?.let { error ->
                if (error) {
                    it.message?.let { msg ->
                        showSnack(msg)
                    }
                } else {
                    val sharedHelper = SharedHelper(this)

                    sharedHelper.kryptKey = kryptCode
                    sharedHelper.status = getString(R.string.available)
                    sharedHelper.imei = imei
                    sharedHelper.password = password
                    sharedHelper.loggedIn = true
                    sharedHelper.token = it.token ?: ""
                    val intent = Intent(this, DuressPasswordActivity::class.java)
                    intent.putExtra(Constants.IntentKeys.CHANGEPASSWORDFLOW, false)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        })
    }

    fun showSnack(content: String) {
        showToast(content)
    }
}
