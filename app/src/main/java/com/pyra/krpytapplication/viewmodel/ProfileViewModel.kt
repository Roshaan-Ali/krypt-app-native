package com.pyra.krpytapplication.viewmodel

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.app.hakeemUser.network.ApiInput
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.Coroutine
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.utils.jidString
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.model.CommonResponseModel
import com.pyra.krpytapplication.model.GetUserDetailsResponse
import com.pyra.krpytapplication.repositories.implementations.ProfileRepository
import com.pyra.krpytapplication.roomDb.AppDataBase
import com.pyra.krpytapplication.roomDb.entity.BlockListSchema
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema
import com.pyra.krpytapplication.roomDb.entity.GroupParticipationSchema
import com.pyra.network.UrlHelper
import getImei
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private var profileRepository: ProfileRepository
    private var applicationInstance: Application? = null
    private var sharedHelper: SharedHelper? = null

    var profileImage: MutableLiveData<String> = MutableLiveData()
    var status: MutableLiveData<String> = MutableLiveData()
    var success: MutableLiveData<Boolean> = MutableLiveData()
    var image: String = ""
    var participationList: List<GroupParticipationSchema> = ArrayList()
    var notifiAdapter: MutableLiveData<Void>? = MutableLiveData<Void>()
    var profileData = ChatListSchema()
    var refreshView: MutableLiveData<Void> = MutableLiveData()

    var profileName: ObservableField<String> = ObservableField()
    var kryptId: ObservableField<String> = ObservableField()
    var userStatus: ObservableField<String> = ObservableField()
    var memberCount: ObservableField<String> = ObservableField()
    var isGroup: ObservableField<Boolean> = ObservableField()
    var canAddMember: ObservableField<Boolean> = ObservableField()
    var roomImage: ObservableField<String> = ObservableField()

    var isGroupAdmin = false
    var errorMessage = MutableLiveData<String>()
    var isUpdated = MutableLiveData<Boolean>()

    var updateBlockList = MutableLiveData<Boolean>()

    var updateBlockedUser: MutableLiveData<Void>? = MutableLiveData<Void>()
    var blockedUsers = ArrayList<BlockListSchema>()

    fun getUser(roomId: String): LiveData<ChatListSchema>? =
        AppDataBase.getInstance(getApplication())?.chatListDao()?.getUser(roomId)?.asLiveData()

    fun updateUser(roomId: String, roomName: String) {
        viewModelScope.launch {
            AppDataBase.getInstance(getApplication())?.chatListDao()?.updateUser(roomId, roomName)
        }
    }

    init {
        applicationInstance = application
        profileRepository =
            ProfileRepository.getInstance((application as MyApp).getAppDatabase())
        sharedHelper = SharedHelper(application)
    }

    fun getUserDeatilsResponse(username: String, url: String): LiveData<GetUserDetailsResponse> {
        val apiInputs = ApiInput()
        apiInputs.context = applicationInstance?.applicationContext
        apiInputs.jsonObject = JSONObject().apply {
            put("userName", username)
        }
        apiInputs.url = url

        return profileRepository.getUserDetails(apiInputs)
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

    fun getProperties(imei: String) {

        val jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.IMEI, imei)
        jsonObject.put(Constants.ApiKeys.USER_NAME, sharedHelper?.kryptKey)
        profileRepository.getProfileDetails(getApiParams(jsonObject, UrlHelper.GETPROPERTIES))
            ?.observeForever {
                it.error?.let { error ->
                    if (!error) {

                        it.data?.properties?.let { list ->

                            list.forEachIndexed { _, properties ->
                                if (properties.key == "image") {
                                    properties.value?.let { image ->
                                        sharedHelper?.userImage = image
                                    }
                                    profileImage.value = properties.value
                                } else if (properties.key == "status") {
                                    val stat: Status = stringToStatus(properties.value)
                                    sharedHelper?.status = statusToString(stat)
                                    status.value = properties.value
                                }
                            }
                        }

                    }
                }
            }
    }

    private fun stringToStatus(string: String?): Status {
        when (string) {
            applicationInstance?.getString(R.string.available) -> return Status.AVAILABLE
            applicationInstance?.getString(R.string.at_work) -> return Status.AT_WORK
            applicationInstance?.getString(R.string.in_a_meeting) -> return Status.IN_A_MEETING
            applicationInstance?.getString(R.string.busy) -> return Status.BUSY
        }
        return Status.AVAILABLE
    }

    fun statusToString(status: Status): String {
        when (status) {
            Status.AVAILABLE -> return applicationInstance?.getString(R.string.available)!!
            Status.AT_WORK -> return applicationInstance?.getString(R.string.at_work)!!
            Status.IN_A_MEETING -> return applicationInstance?.getString(R.string.in_a_meeting)!!
            Status.BUSY -> return applicationInstance?.getString(R.string.busy)!!
        }
    }

    fun updateImage(image: String) {

        val propertiesValue = JSONArray()

        var jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.KEY, Constants.ApiKeys.IMAGE)
        jsonObject.put(Constants.ApiKeys.VALUE, image)

        propertiesValue.put(jsonObject)

        jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.KEY, Constants.ApiKeys.STATUS)
        jsonObject.put(Constants.ApiKeys.VALUE, sharedHelper?.status)

        propertiesValue.put(jsonObject)

        val requestPayload = JSONObject()
        requestPayload.put(Constants.ApiKeys.PROPERTIES, propertiesValue.toString())
        requestPayload.put(
            Constants.ApiKeys.IMEI,
            getImei(applicationInstance?.applicationContext!!)
        )
        requestPayload.put(Constants.ApiKeys.USER_NAME, sharedHelper?.kryptKey)

        profileRepository.updateProperties(getApiParams(requestPayload, UrlHelper.UPDATEPROPERTIES))
            ?.observeForever {

                if (it.error == "false") {
                    success.value = true
                }
            }
    }

    fun getParticipationList(roomId: String) {

        Coroutine.iOWorker {

            val details = profileRepository.getRoomProfile(roomId)
            Coroutine.mainWorker {
                if (details?.groupType == "PRIVATE") {
                    profileRepository.getParticipation(roomId)?.observeForever {

                        participationList = it
                        memberCount.set(
                            StringBuilder().append(participationList.size).append(" ").append(
                                applicationInstance?.getString(R.string.members)
                            ).toString()
                        )

                        for (i in participationList.indices) {
                            if (participationList[i].kryptId.equals(
                                    sharedHelper?.kryptKey,
                                    true
                                ) && participationList[i].role == "admin"
                            ) {
                                isGroupAdmin = true
                                break
                            }
                        }
                        canAddMember.set(isGroupAdmin && profileData.chatType != "PRIVATE")
                        notifiAdapter?.value = null

                    }
                } else {
                    profileRepository.getParticipationList(roomId)?.observeForever {
                        participationList = it
                        for (i in participationList.indices) {
                            if (participationList[i].userName == null || participationList[i].userName == "") {
                                if (participationList[i].kryptId.equals(
                                        sharedHelper?.kryptKey,
                                        true
                                    )
                                ) {
                                    participationList[i].userName = "You"
                                } else {
                                    participationList[i].userName = participationList[i].kryptId
                                }

                            }
                        }
                        memberCount.set(
                            StringBuilder().append(participationList.size).append(" ").append(
                                applicationInstance?.getString(R.string.members)
                            ).toString()
                        )

                        for (i in participationList.indices) {
                            if (participationList[i].kryptId.equals(
                                    sharedHelper?.kryptKey,
                                    true
                                ) && participationList[i].role == "admin"
                            ) {
                                isGroupAdmin = true
                                break
                            }
                        }
                        canAddMember.set(isGroupAdmin && profileData.chatType != "PRIVATE")
                        notifiAdapter?.value = null
                    }

                }
            }

        }
    }

    fun getParticipationName(position: Int): String? {
        return if (participationList[position].userName == "") {
            participationList[position].kryptId
        } else {
            participationList[position].userName
        }
    }

    fun getParticipationImage(position: Int): String? {
        return participationList[position].userImage
    }

    fun getProfileDetails(roomId: String) {

        Coroutine.mainWorker {
            var data = profileRepository.getRoomProfile(roomId)
            data?.let {
                profileData = it
                profileName.set(profileData.roomName)
                isGroup.set(profileData.chatType != "PRIVATE")
                refreshView.value = null
                kryptId.set(profileData.kryptId)
                if (profileData.userStatus == "") {
                    userStatus.set("Available")
                } else {
                    userStatus.set(profileData.userStatus)
                }

                getBlockedUser()
            }
        }

    }

    fun getRoomImage(): String {
        return profileData.roomImage
    }

    fun isMemberRemoveable(position: Int): Boolean {
        return if (!isGroupAdmin) {
            false
        } else {
            participationList[position].kryptId.toUpperCase() != sharedHelper?.kryptKey?.toUpperCase()
        }
    }

    fun removeUser(position: Int) {

        val jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.GROUPNAME, participationList[position].roomId)
        jsonObject.put(Constants.ApiKeys.USERNAME, participationList[position].kryptId.jidString())
        jsonObject.put(Constants.ApiKeys.ROLE, participationList[position].role)

        profileRepository.removeUserFromGroup(
            getApiParams(
                jsonObject,
                UrlHelper.REMOVEUSERFROMGROUP
            )
        )
            ?.observeForever {
                it.error.let { error ->
                    if (error == "false") {
                        removeUserFromGroupDB(
                            participationList[position].roomId,
                            participationList[position].kryptId
                        )
                    }
                }
            }
    }

    fun removeUserFromGroupDB(roomId: String, userId: String) {
        Coroutine.iOWorker {
            profileRepository.removeUserFromGroupDb(
                roomId.toUpperCase(),
                userId.toUpperCase()
            )
        }

    }

    fun updateGroupProfileImage(imageUrl: String?) {
        val jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.USERNAME, sharedHelper?.kryptKey?.jidString())
        jsonObject.put(Constants.ApiKeys.GROUPNAME, profileData.roomId)
        jsonObject.put(Constants.ApiKeys.DESCRIPTION, "Group Image Updated")
        jsonObject.put(Constants.ApiKeys.GROUPTITLENAME, profileData.roomName)
        jsonObject.put(Constants.ApiKeys.OLDGROUPTITLENAME, profileData.roomName)
        jsonObject.put(Constants.ApiKeys.GROUPTYPE, profileData.groupType.toUpperCase())
        jsonObject.put(Constants.ApiKeys.IMAGE, imageUrl)

        updateGroupInfo(jsonObject)

    }

    private fun updateGroupInfo(jsonObject: JSONObject) {

        profileRepository.updateGroupProfile(
            getApiParams(
                jsonObject,
                UrlHelper.UPDATEGROUPPROPERTY
            )
        )?.observeForever {
            if (it.error == "true") {
                isUpdated.value = false
                errorMessage.value = it.message
            } else if (it.error == "false") {
                isUpdated.value = true
                updateToDb(jsonObject)
            }
        }

    }

    private fun updateToDb(jsonObject: JSONObject) {
        Coroutine.iOWorker {
            profileRepository.updateGroupInfo(
                jsonObject.get(Constants.ApiKeys.IMAGE).toString(),
                jsonObject.get(Constants.ApiKeys.GROUPTITLENAME).toString(),
                jsonObject.get(Constants.ApiKeys.GROUPNAME).toString()
            )
        }
    }

    fun updateGroupProfileTitle(newTitle: String?) {

        var jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.USERNAME, sharedHelper?.kryptKey?.jidString())
        jsonObject.put(Constants.ApiKeys.GROUPNAME, profileData.roomId)
        jsonObject.put(Constants.ApiKeys.DESCRIPTION, "Group Name Updated")
        jsonObject.put(Constants.ApiKeys.GROUPTITLENAME, newTitle)
        jsonObject.put(Constants.ApiKeys.OLDGROUPTITLENAME, profileData.roomName)
        jsonObject.put(Constants.ApiKeys.GROUPTYPE, profileData.groupType.toUpperCase())
        if (profileData.roomImage != "")
            jsonObject.put(Constants.ApiKeys.IMAGE, profileData.roomImage)

        updateGroupInfo(jsonObject)
    }

    private fun getBlockedUser() {
        Coroutine.mainWorker {
            kryptId.get()?.let {
                profileRepository.getBlockedUser(it)?.observeForever { count ->
                    updateBlockList.value = count != 0
                }
            }

        }

    }

    fun blockAction() {

        updateBlockList.value?.let {
            blockOnBlockUsers(it, kryptId.get().toString())
        }

    }

    private fun blockOnBlockUsers(block: Boolean, kryptId: String) {

        val jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.FROMUSER, sharedHelper?.kryptKey.toString())
        jsonObject.put(Constants.ApiKeys.TOUSER, kryptId)
        if (block) {
            jsonObject.put(Constants.ApiKeys.STATUS, "false")
        } else {
            jsonObject.put(Constants.ApiKeys.STATUS, "true")
        }

        profileRepository.blockCall(getApiParams(jsonObject, UrlHelper.BLOCKUSERACCOUNT))
            ?.observeForever {
                if (it.error == "false") {
                    if (block) {
                        profileRepository.unblockUser(kryptId)
                    } else {
                        profileRepository.blockUser(kryptId, profileData.roomImage)
                    }
                }
            }
    }

    fun getBlockedUsers() {
        Coroutine.mainWorker {
            profileRepository.getAllBlockedUsers()?.observeForever {
                blockedUsers = it as ArrayList<BlockListSchema>
                updateBlockedUser?.value = null
            }
        }
    }

    fun getBlockedUserName(position: Int): String? {
        return if (blockedUsers[position].roomName.isNullOrEmpty()) {
            blockedUsers[position].kryptId
        } else {
            blockedUsers[position].roomName
        }
    }

    fun getBlockedUserImage(position: Int): String? {
        return blockedUsers[position].roomImage
    }

    fun unblockUser(position: Int) {
        blockOnBlockUsers(true, blockedUsers[position].kryptId)
    }

    fun deleteAllChatMessages() {
        Coroutine.iOWorker {
            profileRepository.deleteAllMessages()
        }
    }

    fun updateLoginTime() {

        SharedHelper(applicationInstance?.baseContext!!).loggedInTime =
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.ENGLISH
            ).format(Calendar.getInstance().time)

    }

    fun leaveGroup(roomId: String): MutableLiveData<CommonResponseModel>? {

        val jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.GROUPNAME, roomId)
        jsonObject.put(Constants.ApiKeys.USERNAME, sharedHelper?.kryptKey?.jidString())
        if (isGroupAdmin) {
            jsonObject.put(Constants.ApiKeys.ROLE, "admins")
        } else {
            jsonObject.put(Constants.ApiKeys.ROLE, "members")
        }

        return profileRepository.leaveGroup(
            getApiParams(
                jsonObject,
                UrlHelper.QUITFROMGROUP
            )
        )

    }

    fun removeFromGroupDB(roomId: String) {
        profileRepository.removeFromGroup(roomId.toUpperCase())
    }

    fun changePassword(
        kryptKey: String,
        password: String,
        newPassword: String
    ): MutableLiveData<CommonResponseModel>? {

        val jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.USER_NAME, kryptKey)
        jsonObject.put(Constants.ApiKeys.PASSWORD, password)
        jsonObject.put(Constants.ApiKeys.NEWPASSWORD, newPassword)

        return profileRepository.changePassword(
            getApiParams(
                jsonObject,
                UrlHelper.UPDATEPASSWORD
            )
        )
    }

    fun setOnNotificationChanged(b: Boolean, roomId: String) {
        Coroutine.iOWorker {
            profileRepository.chageNotification(b, roomId.toUpperCase())
        }
    }

}