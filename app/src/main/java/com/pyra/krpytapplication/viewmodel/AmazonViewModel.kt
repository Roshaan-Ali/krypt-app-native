package com.pyra.krpytapplication.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.repositories.implementations.AmazonRepository
import java.io.File

class AmazonViewModel(application: Application) : AndroidViewModel(application) {

    var repository: AmazonRepository = AmazonRepository.getInstance()
    var applicationIns: Application = application
    var errorMessage: MutableLiveData<String> = MutableLiveData()
    var sharedHelper: SharedHelper? = SharedHelper(applicationIns.applicationContext)

    var amazonUrl: MutableLiveData<String> = MutableLiveData()
    var amazonGroupUrl: MutableLiveData<String> = MutableLiveData()

    fun uploadImage(
        context: Context,
        file: File
    ) {
        repository.uploadToAWS(context, file).observeForever {
            it.error?.let { error ->
                if (!error) {
                    it.message?.let { url -> amazonUrl.value = url }
                } else {
                    it.message?.let { msg -> errorMessage.value = msg }

                }
            }
        }
    }


    fun uploadProfileImage(
        context: Context,
        file: File
    ) {
        repository.uploadToAWS(context, file).observeForever {
            it.error?.let { error ->
                if (!error) {
                    it.message?.let { url -> amazonGroupUrl.value = url }
                } else {
                    it.message?.let { msg -> errorMessage.value = msg }

                }
            }
        }
    }

}