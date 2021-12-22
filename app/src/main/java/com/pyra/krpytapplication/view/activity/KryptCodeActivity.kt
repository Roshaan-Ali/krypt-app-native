package com.pyra.krpytapplication.view.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.openActivity
import com.pyra.krpytapplication.utils.subEnded
import com.pyra.krpytapplication.viewmodel.ChatListViewModel
import isValidKryptCode
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.activity_krypt_code.*
import showToast

class KryptCodeActivity : BaseActivity() {

    var imeManager: InputMethodManager? = null
    val kryptKeyboard =
        "com.krypt.chat/com.pyra.krpytapplication.incognitokeyboard.KryptIncognitoKeyboard"

    val chatListViewModel: ChatListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_krypt_code)
        checkKryptKeyboardEnabled()

        getIntentValues()
    }

    private fun getIntentValues() {

        intent.extras?.getBoolean("isSubEnded")?.let {
            if (it) {
                showSubEbdedDialog()
                deleteLocalStorage()
            }
        }
    }

    private fun deleteLocalStorage() {
        chatListViewModel.clearDb()
        chatListViewModel.removeCache()
        chatListViewModel.clearLocalStorage()
    }

    private fun showSubEbdedDialog() {
        subEnded(this)
    }

    fun checkKryptKeyboardEnabled() {
        imeManager = applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val list: String = imeManager!!.getEnabledInputMethodList().toString()
        if (!list.contains(kryptKeyboard)) {
            startActivityForResult(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS), 0)
        }
    }

    fun enableKryptKeyboard() {
        val defaultKeyboard =
            Settings.Secure.getString(this.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)

        if (!defaultKeyboard.equals(kryptKeyboard, ignoreCase = true)) {
            imeManager!!.showInputMethodPicker()
        }
    }

    fun onGenerateButtonClicked(view: View) {

        showTermsDialog()

    }

    fun onKryptButtonClicked(view: View) {
        enableKryptKeyboard()

        showKryptCodeDialog()
    }

    private fun showTermsDialog() {
        val dialogView = View.inflate(this, R.layout.dialog_terms_of_services, null)
        val dialog = Dialog(this)
        val termsCheckBox = dialogView.findViewById<CheckBox>(R.id.termsCheckBox)
        val cancelBtn = dialogView.findViewById<Button>(R.id.cancelBtn)
        val proceedBtn = dialogView.findViewById<Button>(R.id.proceedBtn)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)
        val window: Window = dialog.window!!
        window.setGravity(Gravity.CENTER)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.show()

        cancelBtn.setOnClickListener { dialog.dismiss() }

        proceedBtn.setOnClickListener {
            if (termsCheckBox.isChecked) {
                enableKryptKeyboard()
                openActivity(CreatePasswordActivity::class.java)
                dialog.dismiss()
            } else {
                showToast("Accept terms and condition to continue")
            }
        }
    }

    private fun showKryptCodeDialog() {
        val dialogView = View.inflate(this, R.layout.dialog_enter_krypt_code, null)
        val dialog = Dialog(this)
        val kryptCodeEditText = dialogView.findViewById<EditText>(R.id.kryptCode)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)
        val window: Window = dialog.window!!
        window.setGravity(Gravity.CENTER)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        window.setDimAmount(0.0f)
        Blurry.with(this).radius(10).sampling(2).onto(rootView)

        dialog.setOnDismissListener {
            Blurry.delete(rootView)
        }

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.show()

        val backButton = dialog.findViewById<RelativeLayout>(R.id.backIcon)
        backButton.setOnClickListener { dialog.dismiss() }

        val submitButton = dialog.findViewById<Button>(R.id.submitButton)
        submitButton.setOnClickListener {
            val kryptCode = kryptCodeEditText.text.toString()
            if (isValidKryptCode(kryptCode)) {
                dialog.dismiss()
                val intent = Intent(this, PasswordActivity::class.java)
                intent.putExtra("isBackEnabled", true)
                intent.putExtra("kryptCode", kryptCode)
                openActivity(intent)
            } else {
                print("Failed")
                showToast(getString(R.string.invalid_krypt_code))
            }
        }

    }

}
