import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.*
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.app.hakeemUser.network.ApiInput
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.MediaType
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.app.MyApp
import org.jivesoftware.smack.packet.Presence
import org.json.JSONObject
import java.io.*
import java.nio.channels.FileChannel
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

fun fetchThemeColor(id: Int, context: Context): Int {
    val typedValue = TypedValue()
    val a: TypedArray =
        context.obtainStyledAttributes(typedValue.data, intArrayOf(id))
    val color = a.getColor(0, 0)
    a.recycle()
    return color
}

fun getStringFromRawFile(context: Context): String {
    val inputStream: InputStream =
        context.resources.openRawResource(R.raw.name)
    val reader =
        BufferedReader(InputStreamReader(inputStream))
    val result = StringBuilder()
    var line: String?
    while (reader.readLine().also { line = it } != null) {
        result.append(line)
    }
    reader.close()
    return result.toString()
}

fun getNameList(context: Context): List<String>? {
    var response = ""
    val gson = GsonBuilder()
        .setLenient()
        .create()
    var codeModelList: List<String>? = ArrayList<String>()

    try {
        response = getStringFromRawFile(context)
        val type =
            object : TypeToken<List<String>?>() {}.type
        codeModelList = gson.fromJson<List<String>>(response, type)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return codeModelList
}


@SuppressLint("HardwareIds")
fun getImei(context: Context): String {
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val uid: String = Settings.Secure.getString(
        context.applicationContext.contentResolver,
        Settings.Secure.ANDROID_ID
    )
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                uid
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                telephonyManager.imei
            }
            else -> {
                telephonyManager.deviceId
            }
        }
    }
    return "dosa"
}

fun isValidPassword(
    context: Context,
    password: String,
    passwordType: String
): String {
    val specialCharPatten =
        Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE)
    val upperCasePatten = Pattern.compile("[A-Z ]")
    val lowerCasePatten = Pattern.compile("[a-z ]")
    val digitCasePatten = Pattern.compile("[0-9 ]")

    return if (password.length < 11) {
        "$passwordType " + context.resources
            .getString(R.string.password_length_should_be_eight_characters)
    } else if (!specialCharPatten.matcher(password).find()) {
        "$passwordType " + context.resources
            .getString(R.string.password_should_contain_atleast_one_special_character)
    } else if (!upperCasePatten.matcher(password).find()) {
        "$passwordType " + context.resources
            .getString(R.string.password_should_contain_atleast_one_uppercase_character)
    } else if (!digitCasePatten.matcher(password).find()) {
        "$passwordType " + context.resources
            .getString(R.string.password_should_contain_atleast_one_numeric)
    } else {
        "true"
    }
}

fun isValidKryptCode(kryptCode: String): Boolean {
    return (kryptCode.length > 0)
}

fun getRoomId(context: Context, otherKryptKey: String): String {
    val list = ArrayList<String>()
    list.add(SharedHelper(context).kryptKey.toUpperCase(Locale.ROOT))
    list.add(otherKryptKey.toUpperCase(Locale.ROOT))
    list.sort()
    return list[0] + list[1]
}

fun generateKryptCode(): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..10)
        .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("").toString().toLowerCase()
}

inline fun <T> T.guard(block: T.() -> Unit): T {
    if (this == null) block(); return this
}

fun getApiParams(
    context: Context,
    jsonObject: JSONObject?,
    methodName: String
): ApiInput {


    val apiInputs = ApiInput()
    apiInputs.context = context
    apiInputs.jsonObject = jsonObject
    apiInputs.url = methodName

    return apiInputs
}

fun openCamera(activity: Activity) {

    val sharedHelper = SharedHelper(activity)
    val file = getFileTostoreImage(activity)

    val uri: Uri
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        uri = FileProvider.getUriForFile(activity, activity.packageName + ".provider", file)
        sharedHelper.imageUploadPath = file.absolutePath
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        activity.startActivityForResult(takePictureIntent, Constants.RequestCode.CAMERA_INTENT)

    } else {
        uri = Uri.fromFile(file)
        sharedHelper.imageUploadPath = file.absolutePath
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        activity.startActivityForResult(takePictureIntent, Constants.RequestCode.CAMERA_INTENT)
    }
}

fun openGalleryforPhoto(activity: Activity) {
    val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    i.type = "image/*";
    activity.startActivityForResult(i, Constants.RequestCode.GALLERY_INTENT)
}

fun openGallery(activity: Activity) {
    val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    i.type = "image/* video/*";
    activity.startActivityForResult(i, Constants.RequestCode.GALLERY_INTENT)
}

fun openAudioIntent(activity: Activity) {
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = "audio/*"
    activity.startActivityForResult(intent, Constants.RequestCode.AUDIO_INTENT)
}

fun openFileIntent(activity: Activity) {

    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = "*/*"
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    try {
        activity.startActivityForResult(
            Intent.createChooser(intent, "Select a File to Upload"),
            Constants.RequestCode.FILE_INTENT
        );
    } catch (ex: android.content.ActivityNotFoundException) {
        Toast.makeText(
            activity, "Please install a File Manager.",
            Toast.LENGTH_SHORT
        ).show();
    }


//    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//    intent.addCategory(Intent.CATEGORY_OPENABLE)
//    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
//    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
//    intent.type = "*/*"
//    val mimeTypes = arrayOf(
//        "application/msword",
//        "application/pdf",
//        "application/vnd.ms-powerpoint",
//        "application/vnd.ms-excel",
//        "application/x-wav",
//        "application/rtf",
//        "audio/x-wav",
//        "text/plain",
//        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
//        "application/vnd.openxmlformats-officedocument.wordprocessingml.template"
//    )
//    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
//
//    activity.startActivityForResult(intent, Constants.RequestCode.FILE_INTENT)
}

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
fun getPath(context: Context, uri: Uri): String {

    return getRealPathFromUriNew(context, uri) ?: uri.toString()
}

private fun getFileTostoreImage(context: Context): File {

    val newFileName = getNewFileName(MediaType.IMAGE.value, "")

    val newFile = File(context.getExternalFilesDir(null)!!.absolutePath, "/$newFileName")


    if (!newFile.exists()) {
        newFile.createNewFile()
    }

    return newFile
}

@SuppressLint("NewApi")
fun getRealPathFromUriNew(context: Context, uri: Uri): String? {

    val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

    // DocumentProvider
    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]

            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }

            //  handle non-primary volumes
        } else if (isDownloadsDocument(uri)) {

            val id = DocumentsContract.getDocumentId(uri)
            if (id != null && id.startsWith("msf:")) {
                val file = File(
                    context.cacheDir,
                    "temp" + Objects.requireNonNull(
                        context.contentResolver.getType(uri)
                    )?.split("/")?.get(1)
                )
                try {
                    context.contentResolver.openInputStream(uri).use { inputStream ->
                        FileOutputStream(file).use { output ->
                            val buffer = ByteArray(4 * 1024) // or other buffer size
                            var read: Int
                            while (inputStream?.read(buffer).also { read = it!! } != -1) {
                                output.write(buffer, 0, read)
                            }
                            output.flush()
                            return file.absolutePath
                        }
                    }
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
                return null
            } else {
                if (id != null && id.startsWith("raw:")) {
                    return id.substring(4)
                }
                val contentUriPrefixesToTry = arrayOf(
                    "content://downloads/public_downloads",
                    "content://downloads/my_downloads",
                    "content://downloads/all_downloads"
                )
                for (contentUriPrefix in contentUriPrefixesToTry) {
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse(contentUriPrefix),
                        id!!.toLong()
                    )
                    try {
                        val path =
                            getDataColumn(
                                context,
                                contentUri,
                                null,
                                null
                            )
                        if (path != null) {
                            return path
                        }
                    } catch (e: Exception) {
                    }
                }
                // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
                val fileName =
                    getFileName(context, uri)
                val cacheDir =
                    getDocumentCacheDir(context)
                val file =
                    generateFileName(
                        fileName!!,
                        cacheDir
                    )
                var destinationPath: String? = null
                if (file != null) {
                    destinationPath = file.absolutePath
                    saveFileFromUri(context, uri, destinationPath);
                }
                return destinationPath
            }
        } else if (isMediaDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]

            var contentUri: Uri? = null
            when (type) {
                "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            val selection = "_id=?"
            val selectionArgs = arrayOf(split[1])

            return getDataColumn(context, contentUri, selection, selectionArgs)
        }// MediaProvider
        // DownloadsProvider
    } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {

        // Return the remote address
        return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
            context,
            uri,
            null,
            null
        )

    } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
        return uri.path
    }// File
    // MediaStore (and general)

    return null
}


/**
 * Get the value of the data column for this Uri. This is useful for
 * MediaStore Uris, and other file-based ContentProviders.
 *
 * @param context       The context.
 * @param uri           The Uri to query.
 * @param selection     (Optional) Filter used in the query.
 * @param selectionArgs (Optional) Selection arguments used in the query.
 * @return The value of the _data column, which is typically a file path.
 */
private fun getDataColumn(
    context: Context, uri: Uri?, selection: String?,
    selectionArgs: Array<String>?
): String? {

    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(column)

    try {
        cursor =
            context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(index)
        }
    } finally {
        cursor?.close()
    }
    return null
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
private fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 */
private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 */
private fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is Google Photos.
 */
private fun isGooglePhotosUri(uri: Uri): Boolean {
    return "com.google.android.apps.photos.content" == uri.authority
}

fun getRealPathFromURI(context: Context, contentURI: Uri): String? {
    val result: String?
    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = context.contentResolver.query(contentURI, filePathColumn, null, null, null)

    if (cursor == null) {
        result = contentURI.path
    } else {
        cursor.moveToFirst()
        val idx = cursor
            .getColumnIndex(filePathColumn[0])
        result = cursor.getString(idx)
        cursor.close()
    }
    return result
}

fun Presence.isUserOnline(): Boolean {
    var userState = false
    if (this.mode === Presence.Mode.dnd) {
        userState = false
    } else if (this.mode === Presence.Mode.away || this.mode === Presence.Mode.xa) {
        userState = false
    } else if (this.isAvailable) {
        userState = true
    }
    return userState
}


fun getDocumentCacheDir(context: Context): File {
    var dir = File(context.cacheDir, "documents")
    if (!dir.exists()) {
        dir.mkdirs();
    }

    return dir;
}


@RequiresApi(api = Build.VERSION_CODES.KITKAT)
fun getFileName(context: Context, uri: Uri): String? {
    var mimeType = context.getContentResolver().getType(uri);
    var filename: String? = null

    if (mimeType == null && context != null) {
        var path = getPath(context, uri);
        if (path == null) {
            filename = getName(uri.toString());
        } else {
            var file = File(path);
            filename = file.getName();
        }
    } else {
        var returnCursor = context.getContentResolver().query(
            uri, null,
            null, null, null
        );
        if (returnCursor != null) {
            var nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            filename = returnCursor.getString(nameIndex);
            returnCursor.close();
        }
    }

    return filename
}


fun getName(filename: String): String? {
    if (filename == null) {
        return null
    }
    var index = filename.lastIndexOf('/');
    return filename.substring(index + 1);
}


@Nullable
fun generateFileName(namePar: String, directory: File): File? {

    var name: String = namePar

    var file = File(directory, name)

    if (file.exists()) {
        var fileName = name;
        var extension = "";
        var dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0) {
            fileName = name.substring(0, dotIndex);
            extension = name.substring(dotIndex);
        }

        var index = 0;
        file.delete();

        while (file.exists()) {
            index++;
            name = fileName + extension;
            file = File(directory, name)
        }
    }

    try {
        if (!file.createNewFile()) {
            return null;
        }
    } catch (e: IOException) {

        return null;
    }


    return file;
}

private fun saveFileFromUri(context: Context, uri: Uri, destinationPath: String) {
    var iss: InputStream? = null;
    var bos: BufferedOutputStream? = null
    try {
        iss = context.getContentResolver().openInputStream(uri);
        bos = BufferedOutputStream(FileOutputStream(destinationPath, false));
        var buf = ByteArray(1024)
        iss?.read(buf);
        do {
            bos.write(buf);
        } while (iss?.read(buf) != -1);
    } catch (e: IOException) {
        e.printStackTrace();
    } finally {
        try {
            iss?.close()
            bos?.close()
        } catch (e: IOException) {
            e.printStackTrace();
        }
    }
}

fun getNewFileName(fileType: Int, docName: String): String {

    return when (fileType) {
        MediaType.IMAGE.value -> "IMG_" + "_" + System.currentTimeMillis().toString() + ".jpg"
        MediaType.VIDEO.value -> "VID_" + "_" + System.currentTimeMillis().toString() + ".mp4"
        MediaType.DOCUMENT.value -> "$docName"
        MediaType.AUDIO.value -> "AUD_" + "_" + System.currentTimeMillis().toString() + ".mp3"
        else -> "IMG_" + "_" + System.currentTimeMillis().toString() + ".jpg"
    }
}

fun Context.getNewFile(): File {

    val newFileName = getNewFileName(MediaType.IMAGE.value, "")
    val newFile = File(getExternalFilesDir(null)!!.absolutePath, "/$newFileName")

    if (!newFile.exists()) {
        newFile.createNewFile()
    }

    return newFile
}

fun Context.getTxtFile(fileName: String): File? {

    val newFile = File(getExternalFilesDir(null)!!.absolutePath, "/$fileName")

    if (!newFile.exists()) {
        newFile.createNewFile()
    } else {
        return null
    }

    return newFile
}

fun getViewIntent(uri: Uri): Intent {
    //Uri uri = Uri.parse(uripath);

    val intent = Intent(Intent.ACTION_VIEW)
    val url = uri.toString();
    if (url.contains(".doc") || url.contains(".docx")) {
        // Word document
        intent.setDataAndType(uri, "application/msword");
        // intent.setData(uri);
    } else if (url.contains(".pdf")) {
        // PDF file
        intent.setDataAndType(uri, "application/pdf");
        //intent.setData(uri);

    } else if (url.contains(".ppt") || url.contains(".pptx")) {
        // Powerpoint file
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
    } else if (url.contains(".xls") || url.contains(".xlsx")) {
        // Excel file
        intent.setDataAndType(uri, "application/vnd.ms-excel");
    } else if (url.contains(".zip") || url.contains(".rar")) {
        // WAV audio file
        intent.setDataAndType(uri, "application/x-wav");
    } else if (url.contains(".rtf")) {
        // RTF file
        intent.setDataAndType(uri, "application/rtf");
    } else if (url.contains(".wav") || url.contains(".mp3")) {
        // WAV audio file
        intent.setDataAndType(uri, "audio/x-wav");
    } else if (url.contains(".gif")) {
        // GIF file
        intent.setDataAndType(uri, "image/gif");
    } else if (url.contains(".jpg") || url.contains(".jpeg") || url.contains(".png")) {
        // JPG file
        intent.setDataAndType(uri, "image/jpeg");
    } else if (url.contains(".txt")) {
        // Text file
        intent.setDataAndType(uri, "text/plain");
    } else if (url.contains(".3gp") || url.contains(".mpg") || url.contains(".mpeg") ||
        url.contains(".mpe") || url.contains(".mp4") || url.contains(".avi") ||
        url.contains(".webm") || url.contains(".m4v") || url.contains(".mkv")
    ) {
        // Video files
        intent.setDataAndType(uri, "video/*");
    } else {
        intent.setDataAndType(uri, "*/*");
    }

    //   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //  intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    //   intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    return intent
}

@Throws(IOException::class)
fun copyFile(context: Context, src: File): File {

    val filename = src.name

    val newFile = File(context.getExternalFilesDir(null)!!.absolutePath, "/$filename")

    if (!newFile.exists()) {
        newFile.createNewFile()
    }


    val inStream = FileInputStream(src)
    val outStream = FileOutputStream(newFile)
    val inChannel: FileChannel = inStream.channel
    val outChannel: FileChannel = outStream.channel
    inChannel.transferTo(0, inChannel.size(), outChannel)
    inStream.close()
    outStream.close()

    return newFile
}

fun getDocumentIcon(extention: String?): Int {

    extention?.let {
        if (extention.contains(".doc") || extention.contains(".docx")) {
            return R.drawable.word_icon
        } else if (extention.contains(".pdf")) {
            return R.drawable.pdf_icon
        } else if (extention.contains(".ppt") || extention.contains(".pptx")) {
            return R.drawable.ppt_icon
        } else if (extention.contains(".xls") || extention.contains(".xlsx")) {
            return R.drawable.xls_icon
        } else if (extention.contains(".zip") || extention.contains(".rar")) {
//        return R.drawable.ppt_icon-
        } else if (extention.contains(".wav") || extention.contains(".mp3")) {
            return R.drawable.audio_icon
        } else if (extention.contains(".jpg") || extention.contains(".jpeg") || extention.contains(".png")) {
            return R.drawable.jpg_icon
        } else if (extention.contains(".txt")) {
            return R.drawable.plain_icon
        } else if (extention.contains(".3gp") || extention.contains(".mpg") || extention.contains(".mpeg") ||
            extention.contains(".mpe") || extention.contains(".mp4") || extention.contains(".avi") ||
            extention.contains(".webm") || extention.contains(".m4v") || extention.contains(".mkv")
        ) {
            return R.drawable.mov_icon
        } else {
            return R.drawable.plain_icon
        }
    }

    return R.drawable.word_icon


}

fun isAppIsInBackground(context: Context): Boolean {
    var isInBackground = true
    try {
        val am =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses =
            am.runningAppProcesses
        for (processInfo in runningProcesses) {
            if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (activeProcess in processInfo.pkgList) {
                    if (activeProcess == context.packageName) {
                        isInBackground = false
                    }
                }
            }
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return isInBackground
}

fun isWifiTurnOn(context: Activity): Boolean {

    val wifiManager: WifiManager =
        MyApp.getInstance().baseContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    return wifiManager.isWifiEnabled
//    if (wifiManager.isWifiEnabled) {
//        ret
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            val panelIntent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
//            context.startActivityForResult(panelIntent, 500)
//        } else {
//            wifiManager.isWifiEnabled = false
//        }
//
//    }
}


fun isMyServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val manager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

private var density = 1f

fun dp(value: Float, context: Context): Int {

    if (density == 1f) {
        checkDisplaySize(context)
    }
    return if (value == 0f) {
        0
    } else Math.ceil((density * value).toDouble()).toInt()
}


private fun checkDisplaySize(context: Context) {
    try {
        density = context.resources.displayMetrics.density
    } catch (e: Exception) {
        e.printStackTrace()
    }

}

fun Context.showToast(string: String) {
    val toast = Toast.makeText(this, string, Toast.LENGTH_LONG)
    toast.setGravity(Gravity.CENTER, 0, 0)
    toast.show()
}

fun EditText.showHidePass(view: View) {
    if (this.transformationMethod == PasswordTransformationMethod.getInstance()) {
        (view as ImageView).setImageResource(R.drawable.eye_icon_hide)
        this.transformationMethod = HideReturnsTransformationMethod.getInstance()
    } else {
        (view as ImageView).setImageResource(R.drawable.eye_icon_show)
        this.transformationMethod = PasswordTransformationMethod.getInstance()
    }
}

fun setColorToBackground(background: Drawable, color: String) {

    when (background) {
        is ShapeDrawable -> {
            background.paint.color = Color.parseColor(color)
        }
        is GradientDrawable -> {
            background.setColor(Color.parseColor(color))
        }
        is ColorDrawable -> {
            background.color = Color.parseColor(color)
        }
        is LayerDrawable -> {
            val layer =
                background.findDrawableByLayerId(R.id.chat_bubble_layer) as RotateDrawable
            (layer.drawable as GradientDrawable).setColor(Color.parseColor(color))
        }
    }
}

fun takeScreenShot(activity: Activity): Bitmap {
    val view = activity.window.decorView
    view.isDrawingCacheEnabled = true
    view.buildDrawingCache()
    val b1 = view.drawingCache
    val frame = Rect()
    activity.window.decorView.getWindowVisibleDisplayFrame(frame)
    val statusBarHeight: Int = frame.top
    val width = activity.windowManager.defaultDisplay.width
    val height = activity.windowManager.defaultDisplay.height
    val b: Bitmap = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight)
    view.destroyDrawingCache()
    return b
}

fun fastBlur(sentBitmap: Bitmap, radius: Int): Bitmap? {
    val bitmap = sentBitmap.copy(sentBitmap.config, true)
    if (radius < 1) {
        return null
    }
    val w = bitmap.width
    val h = bitmap.height
    val pix = IntArray(w * h)
    Log.e("pix", w.toString() + " " + h + " " + pix.size)
    bitmap.getPixels(pix, 0, w, 0, 0, w, h)
    val wm = w - 1
    val hm = h - 1
    val wh = w * h
    val div = radius + radius + 1
    val r = IntArray(wh)
    val g = IntArray(wh)
    val b = IntArray(wh)
    var rsum: Int
    var gsum: Int
    var bsum: Int
    var x: Int
    var y: Int
    var i: Int
    var p: Int
    var yp: Int
    var yi: Int
    var yw: Int
    val vmin = IntArray(Math.max(w, h))
    var divsum = div + 1 shr 1
    divsum *= divsum
    val dv = IntArray(256 * divsum)
    i = 0
    while (i < 256 * divsum) {
        dv[i] = i / divsum
        i++
    }
    yi = 0
    yw = yi
    val stack = Array(div) { IntArray(3) }
    var stackpointer: Int
    var stackstart: Int
    var sir: IntArray
    var rbs: Int
    val r1 = radius + 1
    var routsum: Int
    var goutsum: Int
    var boutsum: Int
    var rinsum: Int
    var ginsum: Int
    var binsum: Int
    y = 0
    while (y < h) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        boutsum = rsum
        goutsum = boutsum
        routsum = goutsum
        binsum = routsum
        ginsum = binsum
        rinsum = ginsum
        i = -radius
        while (i <= radius) {
            p = pix[yi + Math.min(wm, Math.max(i, 0))]
            sir = stack[i + radius]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            rbs = r1 - Math.abs(i)
            rsum += sir[0] * rbs
            gsum += sir[1] * rbs
            bsum += sir[2] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            i++
        }
        stackpointer = radius
        x = 0
        while (x < w) {
            r[yi] = dv[rsum]
            g[yi] = dv[gsum]
            b[yi] = dv[bsum]
            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum
            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]
            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]
            if (y == 0) {
                vmin[x] = Math.min(x + radius + 1, wm)
            }
            p = pix[yw + vmin[x]]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]
            rsum += rinsum
            gsum += ginsum
            bsum += binsum
            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer % div]
            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]
            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]
            yi++
            x++
        }
        yw += w
        y++
    }
    x = 0
    while (x < w) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        boutsum = rsum
        goutsum = boutsum
        routsum = goutsum
        binsum = routsum
        ginsum = binsum
        rinsum = ginsum
        yp = -radius * w
        i = -radius
        while (i <= radius) {
            yi = Math.max(0, yp) + x
            sir = stack[i + radius]
            sir[0] = r[yi]
            sir[1] = g[yi]
            sir[2] = b[yi]
            rbs = r1 - Math.abs(i)
            rsum += r[yi] * rbs
            gsum += g[yi] * rbs
            bsum += b[yi] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            if (i < hm) {
                yp += w
            }
            i++
        }
        yi = x
        stackpointer = radius
        y = 0
        while (y < h) {

            // Preserve alpha channel: ( 0xff000000 & pix[yi] )
            pix[yi] = -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum
            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]
            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]
            if (x == 0) {
                vmin[y] = Math.min(y + r1, hm) * w
            }
            p = x + vmin[y]
            sir[0] = r[p]
            sir[1] = g[p]
            sir[2] = b[p]
            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]
            rsum += rinsum
            gsum += ginsum
            bsum += binsum
            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer]
            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]
            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]
            yi += w
            y++
        }
        x++
    }
    Log.e("pix", w.toString() + " " + h + " " + pix.size)
    bitmap.setPixels(pix, 0, w, 0, 0, w, h)
    return bitmap
}