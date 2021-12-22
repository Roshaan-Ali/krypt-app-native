package com.pyra.krpytapplication.repositories.implementations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.hakeemUser.network.ApiInput
import com.google.gson.Gson
import com.pyra.krpytapplication.model.SearchUserResult
import com.pyra.krpytapplication.repositories.interfaces.ApiResponseCallback
import com.pyra.network.Api
import kotlinx.coroutines.*
import org.json.JSONObject

class SearchRepository private constructor() {

    var responseModel: MutableLiveData<SearchUserResult> = MutableLiveData()

    companion object {

        var repository: SearchRepository? = null

        fun getInstance(): SearchRepository {
            if (repository == null) {
                repository = SearchRepository()
            }
            return repository as SearchRepository
        }
    }

    fun getSearchResult(apiInput: ApiInput): LiveData<SearchUserResult>{

        Api.postMethod(apiInput, object : ApiResponseCallback {

            override  fun setResponseSuccess(jsonObject: JSONObject) {
                val gson = Gson()
                val response: SearchUserResult =
                    gson.fromJson(jsonObject.toString(), SearchUserResult::class.java)
                responseModel.value = response
            }

            override fun setErrorResponse(error: String) {
                val response = SearchUserResult()
                response.error = true
                response.message = error
                responseModel.value = response

            }
        })
        return responseModel
    }
}