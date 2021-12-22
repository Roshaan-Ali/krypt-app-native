package com.pyra.krpytapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.utils.*
import com.pyra.krpytapplication.repositories.implementations.ChatListRepository
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema
import com.pyra.network.UrlHelper
import getApiParams
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class AddMemberViewModel(application: Application) : AndroidViewModel(application) {

    var chatListRepository: ChatListRepository
    var applicationInstance: Application? = null
    var sharedHelper: SharedHelper? = null

    var namedContacts = ArrayList<ChatListSchema>()
    var unNamedContacts = ArrayList<ChatListSchema>()
    var updateUnnameList: MutableLiveData<Boolean>? = MutableLiveData()
    var updateNameList: MutableLiveData<Boolean>? = MutableLiveData()

    var roomDetails: ChatListSchema? = null

    //create group
    var selectedList = ArrayList<ChatListSchema>()
    var selectedListUpdate: MutableLiveData<Boolean> = MutableLiveData()
    var groupCreated: MutableLiveData<Boolean> = MutableLiveData()

    var errorMessage: MutableLiveData<String> = MutableLiveData()

    init {
        applicationInstance = application
        chatListRepository =
            ChatListRepository.getInstance((applicationInstance as MyApp).getAppDatabase())
        sharedHelper = SharedHelper(application)
    }


    fun setRoomId(roomId: String) {

        Coroutine.iOWorker {
            chatListRepository.getProfileData(roomId).let {
                roomDetails = it
            }
        }
    }


    fun getNamedUser(roomId: String) {

        Coroutine.iOWorker {
            var listOfUsers = chatListRepository.getParticipants(roomId)

            var listofUser = ArrayList<String>()
            listOfUsers?.let {
                for (i in listOfUsers.indices) {
                    listofUser.add(listOfUsers[i].kryptId.toUpperCase(Locale.ROOT))
                }
            }
            Coroutine.mainWorker {
                chatListRepository.getNamedUser(listofUser)?.observeForever {


                    namedContacts = it as ArrayList<ChatListSchema>
                    updateNameList?.value = true
                }

            }

        }


    }

    fun getUnnamedUser(roomId: String) {
        Coroutine.iOWorker {

            var listOfUsers = chatListRepository.getParticipants(roomId)

            var listofUser = ArrayList<String>()
            listOfUsers?.let {
                for (i in listOfUsers.indices) {
                    listofUser.add(listOfUsers[i].kryptId.toUpperCase(Locale.ROOT).bareUsername())
                }
            }
            Coroutine.mainWorker {
                chatListRepository.getUnnamedUser(listofUser)?.observeForever {

                    unNamedContacts = it as ArrayList<ChatListSchema>
                    updateUnnameList?.value = true
                }
            }
        }


    }


    fun getUnNamedUserImage(position: Int): String? {
        return unNamedContacts[position].roomImage
    }

    fun getNamedUserImage(position: Int): String? {
        return namedContacts[position].roomImage
    }


    fun getNamedRoomName(at: Int): String? {
        return namedContacts[at].roomName
    }

    fun getNamedKryptId(at: Int): String? {
        return namedContacts[at].roomName
    }

    fun getNamedRoomId(at: Int): String? {
        return namedContacts[at].roomId
    }

    fun getNamedisGroupChat(at: Int): Boolean {
        return namedContacts[at].chatType != "PRIVATE"
    }

    fun getNamedDisplayName(at: Int): String? {
        return if (namedContacts[at].roomName == "") {
            namedContacts[at].kryptId
        } else {
            namedContacts[at].roomName
        }
    }

    fun getUnNamedKryptId(at: Int): String? {
        return unNamedContacts[at].kryptId
    }

    fun getUnNamedRoomId(at: Int): String? {
        return unNamedContacts[at].roomId
    }

    fun getUnNamedisGroupChat(at: Int): Boolean {
        return unNamedContacts[at].chatType != "PRIVATE"
    }

    fun removeCache() {
        SharedPref(applicationInstance?.baseContext!!).removeValues()
    }


    /////create group
    fun onItemAddContact(chatListSchema: ChatListSchema?) {
        chatListSchema?.let {

            var contains = false
            for (i in 0 until selectedList.size) {
                if (selectedList[i].roomId == chatListSchema.roomId) {
                    contains = true
                    break
                }
            }


            if (contains) {
//            selectedList.remove(chatListSchema)
            } else {
                if (selectedList.size <= 10)
                    selectedList.add(0, chatListSchema)
            }

            selectedListUpdate.value = true
        }

    }

    fun getSelectedName(position: Int): String? {
        return if (selectedList[position].roomName == "") {
            selectedList[position].kryptId
        } else {
            selectedList[position].roomName
        }
    }

    fun getSelectedImage(position: Int): String? {
        return selectedList[position].roomImage
    }

    fun removeSelectedUser(position: Int) {
        selectedList.removeAt(position)
        selectedListUpdate.value = true
    }


    fun addMembersToList(
        roomId: String
    ) {

        var userList = JSONArray()
        for (i in 0 until selectedList.size) {
            var jsonObject = JSONObject()
            jsonObject.put(
                Constants.ApiKeys.USERNAME,
                selectedList[i].kryptId + "@" + Constants.XMPPKeys.CHAT_DOMAIN
            )
            userList.put(jsonObject)
        }

        var jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.USERNAME, userList.toString())
        jsonObject.put(Constants.ApiKeys.GROUPNAME, roomId)
        jsonObject.put(Constants.ApiKeys.ROLE, "members")


        chatListRepository.joinGroupMembers(
            getApiParams(
                applicationInstance?.baseContext!!,
                jsonObject,
                UrlHelper.JOINGROUP
            )
        ).observeForever {

            if (it.error == "true") {
                errorMessage.value = it.message
                groupCreated.value = false
            } else {
                SelectedContactSingleton.getInstance()?.listofUsers = ArrayList()
                groupCreated.value = true
            }
        }


    }


}