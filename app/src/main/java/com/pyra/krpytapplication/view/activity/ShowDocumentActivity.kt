package com.pyra.krpytapplication.view.activity

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import kotlinx.android.synthetic.main.activity_document.*
import kotlinx.android.synthetic.main.activity_view_document.content
import kotlinx.android.synthetic.main.activity_view_document.doctitle
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

class ShowDocumentActivity : BaseActivity() {

    var path = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_document)

        getIntentValues()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                Constants.Permission.READ_WRITE_STORAGE_PERM_LIST,
                Constants.Permission.READ_WRITE_STORAGE_PERMISSIONS
            )
        } else {
            readDocument()
        }
    }

    private fun readDocument() {

        if (path == "") {
            return
        }

        val file = File(path)
        //Read text from file
        val text = java.lang.StringBuilder()

        try {
            val br = BufferedReader(FileReader(file))
            var line: String?
            while (br.readLine().also { line = it } != null) {
                text.append(line)
                text.append('\n')
            }
            br.close()
        } catch (e: IOException) {
            //You'll need to add proper error handling here
        }
        content.text = text
        doctitle.text = file.name
    }

    private fun getIntentValues() {

        intent.extras?.let {
            path = it.getString("filePath", "")
        }

    }

    fun onBackButtonPressed(view: View) {
        onBackPressed()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constants.Permission.READ_WRITE_STORAGE_PERMISSIONS -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                readDocument()
            } else {

                Snackbar.make(parentView, "Need Permission View document", Snackbar.LENGTH_LONG)
                    .show()
            }
        }

    }


}