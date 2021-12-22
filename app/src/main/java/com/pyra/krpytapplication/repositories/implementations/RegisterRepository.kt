package com.pyra.krpytapplication.repositories.implementations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.hakeemUser.network.ApiInput
import com.google.gson.Gson
import com.pyra.krpytapplication.model.LoginResponseModel
import com.pyra.krpytapplication.model.SignupResponseModel
import com.pyra.krpytapplication.repositories.interfaces.ApiResponseCallback
import com.pyra.network.Api
import org.json.JSONObject

class RegisterRepository private constructor() {

    companion object {
        private var repository: RegisterRepository? = null

        fun getInstance(): RegisterRepository {
            if (repository == null) {
                repository = RegisterRepository()
            }
            return repository as RegisterRepository
        }
    }


    fun registerUser(input: ApiInput): LiveData<SignupResponseModel>? {

        val apiResponse: MutableLiveData<SignupResponseModel> = MutableLiveData()

        Api.postMethod(input, object : ApiResponseCallback {
            override  fun setResponseSuccess(jsonObject: JSONObject) {
                val gson = Gson()
                val response: SignupResponseModel =
                    gson.fromJson(jsonObject.toString(), SignupResponseModel::class.java)
                apiResponse.value = response
            }

            override fun setErrorResponse(error: String) {
                val response = SignupResponseModel()
                response.error = true
                response.message = error
                apiResponse.value = response
            }
        })

        return apiResponse
    }

    fun loginUser(apiParams: ApiInput): MutableLiveData<LoginResponseModel> {

        val apiResponse: MutableLiveData<LoginResponseModel> = MutableLiveData()

        Api.postMethod(apiParams, object : ApiResponseCallback {
            override  fun setResponseSuccess(jsonObject: JSONObject) {
                val gson = Gson()
                val response: LoginResponseModel =
                    gson.fromJson(jsonObject.toString(), LoginResponseModel::class.java)
                apiResponse.value = response
            }

            override fun setErrorResponse(error: String) {
                val response = LoginResponseModel()
                response.error = true
                response.message = error
                apiResponse.value = response
            }
        })

        return apiResponse
    }
}