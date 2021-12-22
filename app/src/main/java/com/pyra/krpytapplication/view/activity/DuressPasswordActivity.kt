package com.pyra.krpytapplication.view.activity

import android.os.Bundle
import android.view.View
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.utils.openNewTaskActivity
import isValidPassword
import kotlinx.android.synthetic.main.activity_duress_password.*
import showHidePass
import showToast
import java.util.*

class DuressPasswordActivity : BaseActivity() {

    var sharedHelper = SharedHelper(this)

    var isChangePasswordFlow = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_duress_password)

        intent?.extras?.let {
            isChangePasswordFlow = it.getBoolean(Constants.IntentKeys.CHANGEPASSWORDFLOW)
        }
    }

    fun showHidePass(view: View) {

        when (view.id) {
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
            duressPassword.text.trim().toString() != duressConfirmPassword.text.trim()
                .toString() -> {

                showToast(getString(R.string.retype_duress_password))

            }
            vaultPassword.text.trim().toString() != confirmVaultPassword.text.trim()
                .toString() -> {
                showToast(getString(R.string.retype_vault_password))

            }
            else -> {
                sharedHelper.duressPassword = duressPassword.text.trim().toString()
                sharedHelper.vaultPassword = vaultPassword.text.trim().toString()

                if (isChangePasswordFlow) {
                    finish()
                } else {
                    when (sharedHelper.autoLockType) {
                        "seconds" -> {
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.SECOND, sharedHelper.autoLockTime)
                            sharedHelper.autoLogoutSavedTime = "${calendar.time.time}"
                        }

                        "minutes" -> {
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.MINUTE, sharedHelper.autoLockTime)
                            sharedHelper.autoLogoutSavedTime = "${calendar.time.time}"
                        }

                        "hours" -> {
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.HOUR_OF_DAY, sharedHelper.autoLockTime)
                            sharedHelper.autoLogoutSavedTime = "${calendar.time.time}"
                        }

                        "days" -> {
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.DAY_OF_YEAR, sharedHelper.autoLockTime)
                            sharedHelper.autoLogoutSavedTime = "${calendar.time.time}"
                        }

                    }
                    openNewTaskActivity(MainActivity::class.java)
                }
            }
        }

    }
}