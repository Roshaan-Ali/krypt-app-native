package com.pyra.krpytapplication.view.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.chat.XMPPOperations
import com.pyra.krpytapplication.viewmodel.ProfileViewModel
import isValidPassword
import kotlinx.android.synthetic.main.activity_change_password.*
import showHidePass
import showToast

class ChangePasswordActivity : BaseActivity() {

    var sharedHelper = SharedHelper(this)
    val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
    }

    fun showHidePass(view: View) {

        when (view.id) {
            R.id.hideShowLoginPassword -> {
                loginPassword.showHidePass(view)
            }
            R.id.hideShowConfirmLoginPassword -> {
                loginConfirmPassword.showHidePass(view)
            }
            R.id.hideShowDuressPassword -> {
                duressPassword.showHidePass(view)
            }
            R.id.hideShowConfirmDuressPassword -> {
                duressConfirmPassword.showHidePass(view)
            }
            R.id.hideShowVaultPassword -> {
                vaultPassword.showHidePass(view)
            }
            R.id.hideShowConfirmVaultPassword -> {
                confirmVaultPassword.showHidePass(view)
            }
        }
    }

    fun onSubmitButtonClicked(view: View) {

        isValidPassword(
            this,
            loginPassword.text.trim().toString(),
            getString(R.string.login_password)
        ).let { duressResult ->
            if (duressResult != "true") {

                showToast(duressResult)
                return
            }
        }

        isValidPassword(
            this,
            duressPassword.text.trim().toString(),
            getString(R.string.duress_password)
        ).let { duressResult ->
            if (duressResult != "true") {

                showToast(duressResult)

                return
            }
        }

        isValidPassword(
            this,
            vaultPassword.text.trim().toString(),
            getString(R.string.vault_password)
        ).let { vaultResult ->
            if (vaultResult != "true") {
                showToast(vaultResult)
                return
            }
        }

        when {
            duressPassword.text.trim().toString() == vaultPassword.text.trim().toString() ||
                    duressPassword.text.trim().toString() == sharedHelper.password ||
                    vaultPassword.text.trim().toString() == sharedHelper.password -> {

                showToast(getString(R.string.not_same))

            }
            loginPassword.text.trim().toString() != loginConfirmPassword.text.trim()
                .toString() -> {

                showToast(getString(R.string.retype_login_password))


            }
            duressPassword.text.trim().toString() != duressConfirmPassword.text.trim()
                .toString() -> {

                showToast(getString(R.string.retype_duress_password))

            }
            vaultPassword.text.trim().toString() != confirmVaultPassword.text.trim()
                .toString() -> {

                showToast(getString(R.string.retype_vault_password))

            }
            else -> {

                changePassword()

            }
        }

    }

    private fun changePassword() {

        XMPPOperations.changePassword(loginPassword.text.toString()) {
            if (it) {

                viewModel.changePassword(
                    SharedHelper(this).kryptKey,
                    SharedHelper(this).password,
                    loginPassword.text.toString()
                )?.observe(this,
                    Observer { error ->

                        if (error.error == "false") {
                            sharedHelper.password = loginPassword.text.trim().toString()
                            sharedHelper.duressPassword = duressPassword.text.trim().toString()
                            sharedHelper.vaultPassword = vaultPassword.text.trim().toString()
                            finish()
                        }

                    })
            }
        }

    }
}