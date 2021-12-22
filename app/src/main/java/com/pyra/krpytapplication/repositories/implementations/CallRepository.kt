package com.pyra.krpytapplication.repositories.implementations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pyra.network.Api
import com.app.hakeemUser.network.ApiInput
import com.google.gson.Gson
import com.pyra.krpytapplication.model.CommonResponseModel
import com.pyra.krpytapplication.model.CreateCallResponseModel
import com.pyra.krpytapplication.repositories.interfaces.ApiResponseCallback
import org.json.JSONObject

class CallRepository private constructor() {

    companion object {

        var callRepository: CallRepository? = null

        fun getInstance(): CallRepository {
            if (callRepository == null) {
                callRepository = CallRepository()
            }
            return callRepository as CallRepository
        }
    }

    fun createCall(apiInput: ApiInput): LiveData<CreateCallResponseModel>? {

        var responseModel: MutableLiveData<CreateCallResponseModel>? = MutableLiveData()

        Api.postMethod(apiInput, object : ApiResponseCallback {

            override  fun setResponseSuccess(jsonObject: JSONObject) {
                var gson = Gson()
                var response: CreateCallResponseModel =
                    gson.fromJson(jsonObject.toString(), CreateCallResponseModel::class.java)
                responseModel?.value = response
            }

            override fun setErrorResponse(error: String) {
                var response = CreateCallResponseModel()
                response.error = "true"
                response.message = error
                responseModel?.value = response

            }
        })
        return responseModel
    }

    fun endCall(apiInput: ApiInput): LiveData<CommonResponseModel>? {

        var responseModel: MutableLiveData<CommonResponseModel>? = MutableLiveData()

        Api.postMethod(apiInput, object : ApiResponseCallback {

            override  fun setResponseSuccess(jsonObject: JSONObject) {
                var gson = Gson()
                var response: CommonResponseModel =
                    gson.fromJson(jsonObject.toString(), CommonResponseModel::class.java)
                responseModel?.value = response
            }

            override fun setErrorResponse(error: String) {
                var response = CommonResponseModel()
                response.error = "true"
                response.message = error
                responseModel?.value = response

            }
        })
        return responseModel
    }

    fun acceptCall(apiInput: ApiInput): LiveData<CommonResponseModel>? {

        var responseModel: MutableLiveData<CommonResponseModel>? = MutableLiveData()

        Api.postMethod(apiInput, object : ApiResponseCallback {

            override  fun setResponseSuccess(jsonObject: JSONObject) {
                var gson = Gson()
                var response: CommonResponseModel =
                    gson.fromJson(jsonObject.toString(), CommonResponseModel::class.java)
                responseModel?.value = response
            }

            override fun setErrorResponse(error: String) {
                var response = CommonResponseModel()
                response.error = "true"
                response.message = error
                responseModel?.value = response

            }
        })
        return responseModel
    }

    fun updateDeviceToken(apiParams: ApiInput) {

        Api.postMethod(apiParams, object : ApiResponseCallback {

            override  fun setResponseSuccess(jsonObject: JSONObject) {

            }

            override fun setErrorResponse(error: String) {


            }
        })
    }


}