package com.pyra.krpytapplication.utils

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.view.Display
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.view.activity.KryptCodeActivity
import org.jxmpp.jid.Jid
import java.io.File

fun <T> Context.openActivity(it: Class<T>) {
    val intent = Intent(this, it)
    startActivity(intent)
}

fun <T> Context.openActivity(it: Class<T>, bundle: Bundle) {
    val intent = Intent(this, it)
    intent.putExtras(bundle)
    startActivity(intent)
}

fun <T> Context.openNewTaskActivity(it: Class<T>) {
    val intent = Intent(this, it)
    intent.flags =
        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
}

fun <T> Context.openNewTaskActivity(it: Class<T>, bundle: Bundle) {
    val intent = Intent(this, it)
    intent.putExtras(bundle)
    intent.flags =
        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
}

fun Context.moveToKryptCodeActivity() {
    //
    MyApp.getInstance().getRequestQueue()?.cancelAll {
        true
    }
    Handler().postDelayed({
        val intent = Intent(this, KryptCodeActivity::class.java)
        SharedHelper(this).apply {
            loggedIn = false
            showKryptScreen = true
        }
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }, 1000)
}

fun Context.isNetworkConnected(): Boolean {
    val connectivityManager: ConnectivityManager? =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    var isConnected = false
    if (connectivityManager != null) {
        val activeNetwork = connectivityManager.activeNetworkInfo
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }
    return isConnected
}

fun ImageView.loadImage(imageUrl: String?) {
    if (imageUrl == null) {
        this.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.man))
        return
    }

    Glide.with(this.context)
        .load(imageUrl)
        .apply(
            RequestOptions()
                .placeholder(R.drawable.man)
                .error(R.drawable.man)
        )
        .into(this)
}

fun ImageView.loadChatImage(imageUrl: String?, thumbNail: String?) {
    if (imageUrl == null) {
        return
    }

    Glide
        .with(this.context)
        .load(imageUrl)
        .apply(
            RequestOptions()
                .placeholder(R.color.white)
                .error(R.color.white)
        )
        .thumbnail(
            Glide.with(this.context)
                .load(thumbNail)
        )
        .into(this)
}

fun Context.openActivity(intent: Intent) {
    startActivity(intent)
}

fun Context.saveToGlideCache(url: String) {
    val rm: RequestManager = Glide.with(this)
    rm.load(url).submit()
}

fun AppCompatActivity.replaceFragment(fragment: Fragment, frameId: Int) {
    val transaction = this.supportFragmentManager.beginTransaction()
    transaction.replace(frameId, fragment)
    transaction.commit()
}

fun View.hide() {
    this.visibility = View.GONE
}

fun ImageView.setIconColor(context: Context, color: Int) {
    this.setColorFilter(ContextCompat.getColor(context, color))
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.isVisible(): Boolean {
    return this.visibility == View.VISIBLE
}

fun View.toggleVisibility() {
    if (this.isVisible()) {
        this.visibility = View.INVISIBLE
    } else {
        this.visibility = View.VISIBLE
    }
}

fun String.jidString(): String {
    return if (this.contains("@")) this else this + "@" + Constants.XMPPKeys.CHAT_DOMAIN
}

fun Jid.bareUsername(): String {
    return this.split("@").get(0)
}

fun String.bareUsername(): String {
    return this.split("@").get(0)
}

fun File.isValidFileSize(): Boolean {
    println("Image file size = " + this.length())
    return this.length() <= 50000000
}

fun File.isCompresableFileSize(): Boolean {
    return this.length() >= 4000000
}

fun File.getFileType(): Int {

    val extension = MimeTypeMap.getFileExtensionFromUrl(this.absolutePath)
    var extention = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    extention?.let {
        return when {
            it.contains("image", true) -> {
                MediaType.IMAGE.value
            }
            it.contains("video", true) -> {
                MediaType.VIDEO.value
            }
            it.contains("audio", true) -> {
                MediaType.AUDIO.value
            }
            else -> {
                MediaType.NONE.value
            }
        }
    }

    return MediaType.NONE.value
}

fun File.getDocumentType(): String {

    val extension = MimeTypeMap.getFileExtensionFromUrl(this.absolutePath)
    var extention = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

    extention?.let {
        return it
    }

    return extension
}

fun File.getFileExtension(): String {

    val extension = MimeTypeMap.getFileExtensionFromUrl(this.absolutePath)
    var extention = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

    return ".$extension"
}

fun Context.isDisplayOn(): Boolean {

    var disMan = this.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    for (display in disMan.displays) {
        if (display.state == Display.STATE_ON) {
            return true
        }
    }
    return false
}
