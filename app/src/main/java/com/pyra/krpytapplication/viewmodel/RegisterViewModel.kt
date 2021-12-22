package com.pyra.krpytapplication.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.model.SignupResponseModel
import com.pyra.krpytapplication.repositories.implementations.RegisterRepository
import com.pyra.network.UrlHelper
import getApiParams
import org.json.JSONObject

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    var repository: RegisterRepository? = null
    var sharedHelper: SharedHelper? = null
    var success: MutableLiveData<String>? = MutableLiveData()
    var error: MutableLiveData<String>? = MutableLiveData()
    var attempts: MutableLiveData<Int>? = MutableLiveData()

    init {
        repository = RegisterRepository.getInstance()
        sharedHelper = SharedHelper(application)
    }

    fun registerUser(
        context: Context,
        imei: String,
        krypt: String,
        password: String
    ): LiveData<SignupResponseModel>? {

        val jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.USER_NAME, krypt)
        jsonObject.put(Constants.ApiKeys.IMEI, imei)
        jsonObject.put(Constants.ApiKeys.PASSWORD, password)
        return repository?.registerUser(getApiParams(context, jsonObject, UrlHelper.CREATE_ACCOUNT))
    }

    fun loginUser(
        context: Context,
        imei: String,
        password: String,
        kryptCode: String
    ) {

        val jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.IMEI, imei)
        jsonObject.put(Constants.ApiKeys.PASSWORD, password)
        jsonObject.put(Constants.ApiKeys.USER_NAME, kryptCode)
        repository?.loginUser(getApiParams(context, jsonObject, UrlHelper.LOGIN))?.observeForever {
            it.error?.let { err ->
                if (err) {
                    it.message?.let { msg -> error?.value = msg }
                    it.data?.passwordAttempt?.let { atmp -> attempts?.value = atmp }
                } else {
                    it.data?.name?.let { sharedHelper?.userName = it }
                    it.data?.token?.let { sharedHelper?.token = it }
                    sharedHelper?.kryptKey = kryptCode
                    sharedHelper?.password = password

                    success?.value = "success"
                }
            }

        }

    }

}