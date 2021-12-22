package com.pyra.krpytapplication.repositories.implementations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.hakeemUser.network.ApiInput
import com.google.gson.Gson
import com.pyra.krpytapplication.utils.Coroutine
import com.pyra.krpytapplication.model.CommonResponseModel
import com.pyra.krpytapplication.model.GetProfileResponseModel
import com.pyra.krpytapplication.model.GetUserDetailsResponse
import com.pyra.krpytapplication.repositories.interfaces.ApiResponseCallback
import com.pyra.krpytapplication.roomDb.AppDataBase
import com.pyra.krpytapplication.roomDb.dao.BlockListDao
import com.pyra.krpytapplication.roomDb.dao.ChatListDao
import com.pyra.krpytapplication.roomDb.dao.ChatMessagesDao
import com.pyra.krpytapplication.roomDb.dao.GroupParticipationDao
import com.pyra.krpytapplication.roomDb.entity.BlockListSchema
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema
import com.pyra.krpytapplication.roomDb.entity.GroupParticipationSchema
import com.pyra.network.Api
import org.json.JSONObject
import java.util.*

class ProfileRepository private constructor(appDataBase: AppDataBase?) {

    init {
        groupParticipationDao = appDataBase?.groupParticipationDao()
        chatListDao = appDataBase?.chatListDao()
        chatMessagesDao = appDataBase?.chatMessagesDao()
        blockListDao = appDataBase?.blockListDao()
    }

    companion object {

        private var repository: ProfileRepository? = null
        private var groupParticipationDao: GroupParticipationDao? = null
        private var chatListDao: ChatListDao? = null
        private var blockListDao: BlockListDao? = null
        private var chatMessagesDao: ChatMessagesDao? = null

        fun getInstance(appDataBase: AppDataBase?): ProfileRepository {
            if (repository == null) {
                repository = ProfileRepository(appDataBase)
            }
            return repository as ProfileRepository
        }
    }

    fun updateProperties(apiInput: ApiInput): LiveData<CommonResponseModel>? {
        val responseModel: MutableLiveData<CommonResponseModel>? = MutableLiveData()

        Api.postMethod(apiInput, object : ApiResponseCallback {

            override fun setResponseSuccess(jsonObject: JSONObject) {
                val gson = Gson()
                val response: CommonResponseModel =
                    gson.fromJson(jsonObject.toString(), CommonResponseModel::class.java)
                responseModel?.value = response
            }

            override fun setErrorResponse(error: String) {
                val response = CommonResponseModel()
                response.error = "true"
                response.message = error
                responseModel?.value = response

            }
        })
        return responseModel
    }

    fun getProfileDetails(apiInput: ApiInput): MutableLiveData<GetProfileResponseModel>? {
        val responseModel: MutableLiveData<GetProfileResponseModel>? = MutableLiveData()

        Api.postMethod(apiInput, object : ApiResponseCallback {

            override fun setResponseSuccess(jsonObject: JSONObject) {
                val gson = Gson()
                val response: GetProfileResponseModel =
                    gson.fromJson(jsonObject.toString(), GetProfileResponseModel::class.java)
                responseModel?.value = response
            }

            override fun setErrorResponse(error: String) {
                val response = GetProfileResponseModel()
                response.error = true
                response.message = error
                responseModel?.value = response

            }
        })
        return responseModel
    }

    fun removeUserFromGroup(apiInput: ApiInput): MutableLiveData<CommonResponseModel>? {
        val responseModel: MutableLiveData<CommonResponseModel>? = MutableLiveData()

        Api.postMethod(apiInput, object : ApiResponseCallback {

            override fun setResponseSuccess(jsonObject: JSONObject) {
                val gson = Gson()
                val response: CommonResponseModel =
                    gson.fromJson(jsonObject.toString(), CommonResponseModel::class.java)
                responseModel?.value = response
            }

            override fun setErrorResponse(error: String) {
                val response = CommonResponseModel()
                response.error = "true"
                response.message = error
                responseModel?.value = response

            }
        })
        return responseModel
    }

    fun getParticipationList(roomId: String): LiveData<List<GroupParticipationSchema>>? {
        return groupParticipationDao?.getParticipations(roomId.toUpperCase())
    }

    fun getParticipation(roomId: String): LiveData<List<GroupParticipationSchema>>? {
        return groupParticipationDao?.getParticipationsPrivateGrp(roomId.toUpperCase())
    }

    suspend fun getRoomProfile(roomId: String): ChatListSchema? {
        return chatListDao?.getProfileData(roomId.toUpperCase(Locale.ROOT))
    }

    fun removeUserFromGroupDb(roomId: String, userId: String) {
        groupParticipationDao?.removeUserFromGroupDb(roomId, userId)
    }

    fun updateGroupProfile(apiParams: ApiInput): MutableLiveData<CommonResponseModel>? {
        val responseModel: MutableLiveData<CommonResponseModel>? = MutableLiveData()

        Api.postMethod(apiParams, object : ApiResponseCallback {

            override fun setResponseSuccess(jsonObject: JSONObject) {
                val gson = Gson()
                val response: CommonResponseModel =
                    gson.fromJson(jsonObject.toString(), CommonResponseModel::class.java)
                responseModel?.value = response
            }

            override fun setErrorResponse(error: String) {
                val response = CommonResponseModel()
                response.error = "true"
                response.message = error
                responseModel?.value = response
            }
        })
        return responseModel
    }

    fun updateGroupInfo(image: String, name: String, id: String) {
        chatListDao?.updateGroupInfo(image, name, id.toUpperCase())
    }

    fun getBlockedUser(kryptId: String): LiveData<Int>? {
        return blockListDao?.getBlockedUser(kryptId.toUpperCase())
    }

    fun isUserBlocked(kryptId: String): Int? {
        return blockListDao?.isUserBlocked(kryptId.toUpperCase())
    }

    fun unblockUser(kryptId: String?) {

        Coroutine.iOWorker {
            blockListDao?.removeUser(kryptId.toString().toUpperCase())
        }
    }

    fun blockUser(kryptId: String?, roomImage: String) {

        Coroutine.iOWorker {
            val blockListSchema = BlockListSchema()
            blockListSchema.kryptId = kryptId.toString().toUpperCase()
            blockListSchema.roomName = kryptId.toString().toUpperCase()
            blockListSchema.roomImage = roomImage
            blockListDao?.blockUser(blockListSchema)
        }

    }

    fun getAllBlockedUsers(): LiveData<List<BlockListSchema>>? {
        return blockListDao?.getAllBlockedUser()
    }

    fun blockCall(apiParams: ApiInput): MutableLiveData<CommonResponseModel>? {

        val responseModel: MutableLiveData<CommonResponseModel>? = MutableLiveData()

        Api.postMethod(apiParams, object : ApiResponseCallback {

            override fun setResponseSuccess(jsonObject: JSONObject) {
                val gson = Gson()
                val response: CommonResponseModel =
                    gson.fromJson(jsonObject.toString(), CommonResponseModel::class.java)
                responseModel?.value = response
            }

            override fun setErrorResponse(error: String) {
                val response = CommonResponseModel()
                response.error = "true"
                response.message = error
                responseModel?.value = response

            }
        })
        return responseModel
    }

    fun deleteAllMessages() {
//        chatMessagesDao?.clearAllMessage()
        chatMessagesDao?.updateAsDeleteAllMessage()
        chatListDao?.removeAllMessage()
    }

    fun leaveGroup(apiParams: ApiInput): MutableLiveData<CommonResponseModel>? {

        val responseModel: MutableLiveData<CommonResponseModel>? = MutableLiveData()

        Api.postMethod(apiParams, object : ApiResponseCallback {

            override fun setResponseSuccess(jsonObject: JSONObject) {
                val gson = Gson()
                val response: CommonResponseModel =
                    gson.fromJson(jsonObject.toString(), CommonResponseModel::class.java)
                responseModel?.value = response
            }

            override fun setErrorResponse(error: String) {
                val response = CommonResponseModel()
                response.error = "true"
                response.message = error
                responseModel?.value = response

            }
        })
        return responseModel
    }

    fun removeFromGroup(roomId: String) {
        Coroutine.iOWorker {
            chatListDao?.deleteGroup(roomId)
            groupParticipationDao?.quitGroup(roomId)
//            chatMessagesDao?.clearMessage(roomId)
            chatMessagesDao?.updateAsDeleteRoomMessage(roomId)
        }

    }

    fun changePassword(apiParams: ApiInput): MutableLiveData<CommonResponseModel>? {

        val responseModel: MutableLiveData<CommonResponseModel>? = MutableLiveData()

        Api.postMethod(apiParams, object : ApiResponseCallback {

            override fun setResponseSuccess(jsonObject: JSONObject) {
                val gson = Gson()
                val response: CommonResponseModel =
                    gson.fromJson(jsonObject.toString(), CommonResponseModel::class.java)
                responseModel?.value = response
            }

            override fun setErrorResponse(error: String) {
                val response = CommonResponseModel()
                response.error = "true"
                response.message = error
                responseModel?.value = response

            }
        })
        return responseModel
    }

    fun getUserDetails(apiParams: ApiInput): MutableLiveData<GetUserDetailsResponse> {
        val responseModel: MutableLiveData<GetUserDetailsResponse> = MutableLiveData()

        Api.postMethod(apiParams, object : ApiResponseCallback {

            override fun setResponseSuccess(jsonObject: JSONObject) {
                val gson = Gson()
                val response: GetUserDetailsResponse =
                    gson.fromJson(jsonObject.toString(), GetUserDetailsResponse::class.java)
                responseModel.value = response
            }

            override fun setErrorResponse(error: String) {
                val response =
                    GetUserDetailsResponse(error = "true", message = error, data = emptyList())
                responseModel.value = response
            }
        })

        return responseModel
    }

    fun chageNotification(b: Boolean, roomId: String) {
        chatListDao?.changeNotification(roomId, b)
    }

}