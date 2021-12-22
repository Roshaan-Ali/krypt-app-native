package com.pyra.krpytapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.hakeemUser.network.ApiInput
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.model.CommonResponseModel
import com.pyra.krpytapplication.model.CreateCallResponseModel
import com.pyra.krpytapplication.repositories.implementations.CallRepository
import com.pyra.krpytapplication.repositories.implementations.ChatListRepository
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema
import com.pyra.network.UrlHelper
import org.json.JSONObject

class CallViewModel(application: Application) : AndroidViewModel(application) {

    var sharedHelper: SharedHelper? = null
    var callRepository: CallRepository? = null
    var applicationInstance: Application? = null
    var chatRepository: ChatListRepository? = null
    var userName: MutableLiveData<String> = MutableLiveData()
    var userImage: MutableLiveData<String> = MutableLiveData()

    init {
        sharedHelper = SharedHelper(application)
        callRepository = CallRepository.getInstance()
        applicationInstance = application
        chatRepository = ChatListRepository.getInstance((application as MyApp).getAppDatabase())
    }


    private fun getApiParams(jsonObject: JSONObject?, url: String): ApiInput {

        val header: MutableMap<String, String> = HashMap()
//        header[Constants.ApiKeys.ROLE] = Constants.ApiValues.USER
//        header[Constants.ApiKeys.AUTHORIZATION] = sharedHelper!!.token

        val apiInputs = ApiInput()
        apiInputs.context = applicationInstance?.applicationContext
        apiInputs.jsonObject = jsonObject
        apiInputs.url = url
        apiInputs.headers = header

        return apiInputs
    }

    fun createCall(from: String, to: String, callType: String): LiveData<CreateCallResponseModel>? {

        var jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.FROMUSERNAME, from)
        jsonObject.put(Constants.ApiKeys.TOUSERNAME, to)
        jsonObject.put(Constants.ApiKeys.CALLTYPE, callType)

        return callRepository?.createCall(getApiParams(jsonObject, UrlHelper.CREATECALL))
    }

    fun acceptCall(from: String, to: String): LiveData<CommonResponseModel>? {

        var jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.FROMUSERNAME, from)
        jsonObject.put(Constants.ApiKeys.TOUSERNAME, to)

        return callRepository?.acceptCall(getApiParams(jsonObject, UrlHelper.ACCEPTCALL))
    }

    fun endCall(from: String, to: String): LiveData<CommonResponseModel>? {

        var jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.FROMUSERNAME, from)
        jsonObject.put(Constants.ApiKeys.TOUSERNAME, to)
        return callRepository?.endCall(getApiParams(jsonObject, UrlHelper.ENDCALL))
    }

    fun updateDeviceToken(
        os: String,
        imei: String,
        token: String,
        kryptKey: String?
    ) {

        var jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.OS, os)
        jsonObject.put(Constants.ApiKeys.IMEI, imei)
        jsonObject.put(Constants.ApiKeys.DEVICETOKEN, token)
        jsonObject.put(Constants.ApiKeys.USER_NAME, kryptKey)
        callRepository?.updateDeviceToken(getApiParams(jsonObject, UrlHelper.UPDATEDEVICETOKEN))
    }

    fun getUserDetails(toUserName: String) {
        chatRepository?.getUserDetail(toUserName.toUpperCase())?.observeForever {

            if (it == null) {
                userName.value = ""
                userImage.value = ""
            } else {
                userName.value = it.roomName
                userImage.value = it.roomImage
            }

        }

    }

    fun getRoomDetails(toUserName: String): LiveData<ChatListSchema>? {
       return chatRepository?.getUserDetail(toUserName.toUpperCase());
    }


}