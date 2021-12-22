package com.pyra.krpytapplication.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.hakeemUser.network.ApiInput
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.Coroutine
import com.pyra.krpytapplication.utils.LogUtil
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.model.SearchUserData
import com.pyra.krpytapplication.model.SearchUserResult
import com.pyra.krpytapplication.repositories.implementations.ChatListRepository
import com.pyra.krpytapplication.repositories.implementations.SearchRepository
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema
import com.pyra.network.UrlHelper
import getRoomId
import org.json.JSONObject

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    var sharedHelper: SharedHelper? = null
    var repository: SearchRepository? = null
    var chatListRepository: ChatListRepository

    var applicationInstance: Application? = null

    var searchedData = ArrayList<SearchUserData>()
    var notifyData = MutableLiveData<Boolean>()
    var errorMessage = MutableLiveData<String>()
    var progress = MutableLiveData<Boolean>()

    init {
        sharedHelper = SharedHelper(application)
        repository = SearchRepository.getInstance()
        applicationInstance = application
        chatListRepository =
            ChatListRepository.getInstance((applicationInstance as MyApp).getAppDatabase())
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

    fun getSearchResult(name: String) {

        var jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.NAME, name)


        repository?.getSearchResult(getApiParams(jsonObject, UrlHelper.SEARCHUSER))
            ?.observeForever {
                if (it.error) {

                } else {

                    it.data?.let { users ->
                        if (users.username != null) {
                            searchedData = ArrayList()
                            searchedData.add(users)
                            notifyData.value = true
                        } else {
                            searchedData = ArrayList()
                            notifyData.value = true
                        }

                    }

                }

            }
    }

    fun getSearchResultAndAddUser(code: String): LiveData<SearchUserResult>? {

        var jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.NAME, code)

        return repository?.getSearchResult(getApiParams(jsonObject, UrlHelper.SEARCHUSER))

    }

    fun addToView(code: String, name: String) {

        val entity = ChatListSchema()

        entity.chatType = "PRIVATE"

        entity.kryptId = code
        entity.roomName = name
        entity.showNotification = true
        entity.roomId = getRoomId(applicationInstance?.baseContext!!, code)

        searchedData[0].properties?.let { listValue ->
            if (listValue.size != 0) {
                listValue[0].value?.let {
                    entity.roomImage = it
                }
            }
        }

        Coroutine.iOWorker {
            try {
                chatListRepository.insertData(entity)
            } catch (e: SQLiteException) {
                println("You've already inserted it once")
            }

        }

    }


    fun insertAddContacts(data: SearchUserData, name: String) {
        LogUtil.e("username ", "true")
        if (!data.username.isNullOrEmpty()) {
            val entity = ChatListSchema(
                chatType = "PRIVATE",
                kryptId = data.username ?: "",
                roomName = name,
                showNotification = true,
                roomId = getRoomId(getApplication(), data.username ?: "")
            )
            data.properties?.let { listValue ->
                if (listValue.size != 0) {
                    listValue[0].value?.let {
                        entity.copy(roomImage = it)
                    }
                }
            }

            Coroutine.iOWorker {
                try {
                    chatListRepository.insertData(entity)
                } catch (e: SQLiteException) {
                    println("You've already inserted it once")
                }

            }

        }

    }


    fun emptyData() {
        searchedData = ArrayList()
        notifyData.value = true

    }

    fun addToView(position: Int) {

        val entity = ChatListSchema()

        entity.chatType = "PRIVATE"
        entity.showNotification = true

        searchedData[position].username?.let {
            entity.kryptId = it
            entity.roomId = getRoomId(applicationInstance?.baseContext!!, it)
        }
        searchedData[position].properties?.let { listValue ->
            if (listValue.size != 0) {
                listValue[0].value?.let {
                    entity.roomImage = it
                }
            }
        }

        Coroutine.iOWorker {
            try {
                chatListRepository.insertData(entity)
            } catch (e: SQLiteException) {
                println("You've already inserted it once")
            }

        }

    }

    fun getSearchUserName(at: Int): String? {
        return searchedData[at].username
    }

    fun getDisplayName(at: Int): String? {
        return if (searchedData[at].name == "") {
            searchedData[at].username
        } else {
            searchedData[at].name
        }

    }

    fun getImage(at: Int): String? {
        if (searchedData[at].properties?.size != 0)
            searchedData[at].properties?.get(0)?.value?.let {
                return it
            }
        return ""
    }

    fun getIsAddContactName(at: Int): Boolean {
        return searchedData[at].name != ""

    }


}