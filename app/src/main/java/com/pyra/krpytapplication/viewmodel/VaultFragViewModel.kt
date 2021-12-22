package com.pyra.krpytapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.pyra.krpytapplication.utils.Coroutine
import com.pyra.krpytapplication.utils.longDateToDisplayTimeString
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.repositories.implementations.VaultFragRepository
import com.pyra.krpytapplication.roomDb.entity.ChatMessagesSchema
import java.io.File
import java.math.RoundingMode
import java.text.DecimalFormat

class VaultFragViewModel(application: Application) : AndroidViewModel(application) {

    val app = application
    val repository = VaultFragRepository.getInstance((app as MyApp).getAppDatabase())
    var chatMessageImage: List<ChatMessagesSchema> = ArrayList()
    var chatMessageVideo: List<ChatMessagesSchema> = ArrayList()
    var chatMessageDocument: List<ChatMessagesSchema> = ArrayList()

    var notifyItemImage: MutableLiveData<Void> = MutableLiveData()
    var notifyItemVideo: MutableLiveData<Void> = MutableLiveData()
    var notifyItemDocument: MutableLiveData<Void> = MutableLiveData()

    var selectedImageList = ArrayList<String>()
    var selectedVideoList = ArrayList<String>()
    var selectedDocumentList = ArrayList<String>()

    var currentTab = 0

    fun getDownloadedImageList() {
        Coroutine.iOWorker {
            val list = repository.getDownloadedImageList()
            chatMessageImage = checkFileExist(list as ArrayList)
            Coroutine.mainWorker {
                notifyItemImage.value = null
            }
        }

    }

    fun getDownloadedVideoList() {
        Coroutine.iOWorker {
            val list = repository.getDownloadedVideoList()
            chatMessageVideo = checkFileExist(list as ArrayList)
            Coroutine.mainWorker { notifyItemVideo.value = null }
        }
    }

    fun getDownloadedDocumentList() {
        Coroutine.iOWorker {
            val list = repository.getDownloadedDocumentList()
            chatMessageDocument = checkFileExist(list as ArrayList)
            Coroutine.mainWorker { notifyItemDocument.value = null }
        }
    }

    private fun checkFileExist(list: ArrayList<ChatMessagesSchema>): ArrayList<ChatMessagesSchema> {

        var i = 0

        while (i < list.size) {
            if (list[i].localMediaPath == "") {
                list.removeAt(i)
                continue
            } else {
                if (File(list[i].localMediaPath).exists()) {
                    i++
                    continue
                } else {
                    list.removeAt(i)
                    continue
                }
            }
        }
        return list
    }

    fun getImageCount(isVideo: Boolean): Int {
        return if (isVideo) {
            chatMessageVideo.size
        } else {
            chatMessageImage.size
        }

    }

    fun getLocalPath(position: Int, isVideo: Boolean): String {
        return if (isVideo) {
            chatMessageVideo[position].localMediaPath
        } else {
            chatMessageImage[position].localMediaPath
        }

    }


    fun getThumpImage(position: Int, isVideo: Boolean): String? {
        return if (isVideo) {
            chatMessageVideo[position].localMediaPath
        } else {
            chatMessageImage[position].localMediaPath
        }

    }

    fun getDocumentListSize(): Int {
        return chatMessageDocument.size
    }

    fun getDocFileName(position: Int): String? {
        return chatMessageDocument[position].mediaDocumentName
    }

    fun getDocFileType(position: Int): String? {
        return chatMessageDocument[position].mediaDocumentType
    }

    fun getDate(position: Int): String? {
        return chatMessageDocument[position].messageTime.longDateToDisplayTimeString()
    }

    fun getFile(position: Int): File {
        return File(chatMessageDocument[position].localMediaPath)
    }

     fun getFilePath(position: Int): String {
        return chatMessageDocument[position].localMediaPath
    }

    fun getMediaUrl(position: Int): String {
        return chatMessageDocument[position].localMediaPath
    }

    fun getDocFileSize(position: Int): String? {

        val file = File(chatMessageDocument[position].localMediaPath)
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        return df.format(file.length() / 1024000f).toString() + " mb"
    }

    fun selectMultiVideo(position: Int) {
        if (selectedVideoList.contains(chatMessageVideo[position].messageId.toUpperCase())) {
            selectedVideoList.remove(chatMessageVideo[position].messageId.toUpperCase())
        } else {
            selectedVideoList.add(chatMessageVideo[position].messageId.toUpperCase())
        }
        currentTab = 1
        notifyItemVideo.value = null
    }

    fun selectMultiImage(position: Int) {
        if (selectedImageList.contains(chatMessageImage[position].messageId.toUpperCase())) {
            selectedImageList.remove(chatMessageImage[position].messageId.toUpperCase())
        } else {
            selectedImageList.add(chatMessageImage[position].messageId.toUpperCase())
        }
        currentTab = 0
        notifyItemImage.value = null
    }

    fun selectMultiDocument(position: Int) {
        if (selectedDocumentList.contains(chatMessageDocument[position].messageId.toUpperCase())) {
            selectedDocumentList.remove(chatMessageDocument[position].messageId.toUpperCase())
        } else {
            selectedDocumentList.add(chatMessageDocument[position].messageId.toUpperCase())
        }
        currentTab = 2
        notifyItemDocument.value = null
    }

    fun getIsMultipleImageSelectionEnabled(): Boolean = selectedImageList.size != 0
    fun getIsMultipleVideoSelectionEnabled(): Boolean = selectedVideoList.size != 0
    fun getIsMultipleDocumentSelectionEnabled(): Boolean = selectedDocumentList.size != 0

    fun getIsSelected(position: Int, video: Boolean): Boolean {
        return if (video) {
            selectedVideoList.contains(chatMessageVideo[position].messageId.toUpperCase())
        } else {
            selectedImageList.contains(chatMessageImage[position].messageId.toUpperCase())
        }
    }

    fun showDeleteView(): Boolean {
        return selectedImageList.size != 0 || selectedVideoList.size != 0 || selectedDocumentList.size != 0
    }

    fun removeSelection() {
        Coroutine.mainWorker {
            selectedImageList.clear()
            selectedVideoList.clear()
            selectedDocumentList.clear()
            notifyItemImage.value = null
            notifyItemVideo.value = null
            notifyItemDocument.value = null
        }
    }

    fun getSelectedCount(): String? {
        return when (currentTab) {
            0 -> {
                selectedImageList.size.toString()
            }
            1 -> {
                selectedVideoList.size.toString()
            }
            2 -> {
                selectedDocumentList.size.toString()
            }
            else -> {
                "0"
            }
        }
    }

    fun deleteSelectedItem() {

        Coroutine.iOWorker {
            when (currentTab) {
                0 -> {
                    repository.deleteSelectedItems(selectedImageList)
                    removeSelection()
                    getDownloadedImageList()
                }
                1 -> {
                    repository.deleteSelectedItems(selectedVideoList)
                    removeSelection()
                    getDownloadedVideoList()
                }
                2 -> {
                    repository.deleteSelectedItems(selectedDocumentList)
                    removeSelection()
                    getDownloadedDocumentList()
                }
                else -> {
                    removeSelection()
                }
            }
        }

    }

    fun isDocSelected(position: Int): Boolean {
        return selectedDocumentList.contains(chatMessageDocument[position].messageId.toUpperCase())
    }


}