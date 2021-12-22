package com.pyra.krpytapplication.repositories.implementations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.hakeemUser.network.ApiInput
import com.google.gson.Gson
import com.pyra.krpytapplication.utils.Coroutine
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.chat.XMPPOperations
import com.pyra.krpytapplication.model.CommonResponseModel
import com.pyra.krpytapplication.model.GetUserProfile
import com.pyra.krpytapplication.model.GroupDetailsResponse
import com.pyra.krpytapplication.repositories.interfaces.ApiResponseCallback
import com.pyra.krpytapplication.roomDb.AppDataBase
import com.pyra.krpytapplication.roomDb.dao.*
import com.pyra.krpytapplication.roomDb.entity.BlockListSchema
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema
import com.pyra.krpytapplication.roomDb.entity.ChatMessagesSchema
import com.pyra.krpytapplication.roomDb.entity.GroupParticipationSchema
import com.pyra.network.Api
import org.jetbrains.anko.doAsync
import org.jivesoftware.smack.packet.Presence
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashSet

class ChatListRepository private constructor(appDataBase: AppDataBase?) {

    init {
        chatListDao = appDataBase?.chatListDao()
        chatMessageDao = appDataBase?.chatMessagesDao()
        clearDb = appDataBase?.clearDbDao()
        groupParticipationDao = appDataBase?.groupParticipationDao()
        blockListDao = appDataBase?.blockListDao()
    }

    companion object {

        private var chatListDao: ChatListDao? = null
        private var chatMessageDao: ChatMessagesDao? = null
        private var clearDb: ClearDbDao? = null
        private var groupParticipationDao: GroupParticipationDao? = null
        private var blockListDao: BlockListDao? = null
        private var listRepository: ChatListRepository? = null

        fun getInstance(appDataBase: AppDataBase?): ChatListRepository {
            if (listRepository == null) {
                listRepository = ChatListRepository(appDataBase)
            }
            return listRepository as ChatListRepository
        }
    }

    fun getNamedUser(): LiveData<List<ChatListSchema>>? {
        return chatListDao?.getNamedUser()
    }

    fun getNamedUser(listofUser: ArrayList<String>): LiveData<List<ChatListSchema>>? {
        return chatListDao?.getNamedUser(listofUser)
    }

    fun getUnnamedUser(): LiveData<List<ChatListSchema>>? {
        return chatListDao?.getUnnamedUser()
    }

    fun getUnnamedUser(listofUser: ArrayList<String>): LiveData<List<ChatListSchema>>? {
        return chatListDao?.getUnnamedUser(listofUser)
    }

    fun insertData(entity: ChatListSchema) {
        doAsync {
            chatListDao?.insertChatList(entity)
        }
    }

    fun updateUserName(kryptid: String, userName: String) {
        chatListDao?.updateUserName(kryptid.toUpperCase(Locale.ROOT), userName)
    }

    fun getChatRoomName(roomId: String): String? {
        return chatListDao?.getRoomName(roomId.toUpperCase(Locale.ROOT))
    }

    fun getRoomNameByKryptId(kryptId: String): String? {
        return chatListDao?.getRoomNameByKryptId(kryptId.toUpperCase(Locale.ROOT))
    }

    fun getChatRoomImage(roomId: String): LiveData<String>? {
        return chatListDao?.getRoomImage(roomId.toUpperCase(Locale.ROOT))
    }

    fun updateLastMessage(message: ChatMessagesSchema) {
        doAsync {
            chatListDao?.updateLastMessage(
                message.message,
                message.messageTime,
                message.messageType,
                message.messageStatus,
                message.roomId.toUpperCase(Locale.ROOT)
            )
        }
    }

    fun updateMessageCount(message: ChatMessagesSchema) {
        chatListDao?.updateUnreadMessageCount(message.roomId.toUpperCase(Locale.ROOT))
    }

    fun insertOrReplace(entity: ChatListSchema) {
        chatListDao?.insertOrReplaceChatList(entity)
    }

    fun getChatList(): LiveData<List<ChatListSchema>>? {
        return chatListDao?.getChatList()
    }

    fun getChatCount(roomId: String): Int {
        return chatListDao?.getCountOfUser(roomId.toUpperCase(Locale.ROOT))!!
    }

    fun clearDb() {
        clearDb?.clearChatList()
        clearDb?.clearChatMessageList()
        clearDb?.clearParticipation()
        clearDb?.clearBlockUser()
        clearDb?.clearBurnMessages()
    }

    fun getUserDetail(toUserName: String): LiveData<ChatListSchema>? {
        return chatListDao?.getUserDetail(toUserName)
    }

    fun getPresence(kryptId: String): LiveData<Presence> {
        return MyApp.xmppOperations.getPresence(kryptId)
    }


    suspend fun clearMessage(roomId: String) {
        chatListDao?.removeMessage(roomId.toUpperCase(Locale.ROOT))
    }

    fun getLastActivity(kryptId: String): LiveData<String> {
        return MyApp.xmppOperations.getLastActivity(kryptId)
    }

    fun getSearchedList(searchString: String): LiveData<List<ChatListSchema>>? {
        val searchableString = "%$searchString%"
        return chatListDao?.getSearchedList(searchableString)
    }

    fun insertPaticipations(groupParticipationSchema: GroupParticipationSchema) {
        groupParticipationDao?.insertOrReplaceChatList(groupParticipationSchema)
    }

    suspend fun getProfileData(roomId: String): ChatListSchema? {
        return chatListDao?.getProfileData(roomId.toUpperCase(Locale.ROOT))
    }

    suspend fun getRoomData(kryptKey: String): ChatListSchema? {
        return chatListDao?.getRoomData(kryptKey.toUpperCase(Locale.ROOT))
    }

    fun getGroupRoom(): List<ChatListSchema>? {
        return chatListDao?.getChatRoom()
    }

    fun getUsersList(): List<ChatListSchema>? {
        return chatListDao?.getUsersList()
    }

    fun createGroup(apiParams: ApiInput): LiveData<CommonResponseModel> {
        val apiResponse: MutableLiveData<CommonResponseModel> = MutableLiveData()

        Api.postMethod(apiParams, object : ApiResponseCallback {
            override fun setResponseSuccess(jsonObject: JSONObject) {
                val gson = Gson()
                val response: CommonResponseModel =
                    gson.fromJson(jsonObject.toString(), CommonResponseModel::class.java)
                apiResponse.value = response
            }

            override fun setErrorResponse(error: String) {
                val response = CommonResponseModel()
                response.error = "true"
                response.message = error
                apiResponse.value = response
            }
        })

        return apiResponse
    }

    fun joinGroupMembers(apiParams: ApiInput): LiveData<CommonResponseModel> {
        val apiResponse: MutableLiveData<CommonResponseModel> = MutableLiveData()

        Api.postMethod(apiParams, object : ApiResponseCallback {
            override fun setResponseSuccess(jsonObject: JSONObject) {
                val gson = Gson()
                val response: CommonResponseModel =
                    gson.fromJson(jsonObject.toString(), CommonResponseModel::class.java)
                apiResponse.value = response
            }

            override fun setErrorResponse(error: String) {
                val response = CommonResponseModel()
                response.error = "true"
                response.message = error
                apiResponse.value = response
            }
        })
        return apiResponse
    }

    fun getGroupDetails(apiParams: ApiInput): LiveData<GroupDetailsResponse> {

        val apiResponse: MutableLiveData<GroupDetailsResponse> = MutableLiveData()

        Api.postMethod(apiParams, object : ApiResponseCallback {
            override fun setResponseSuccess(jsonObject: JSONObject) {
                val gson = Gson()
                val response: GroupDetailsResponse =
                    gson.fromJson(jsonObject.toString(), GroupDetailsResponse::class.java)
                apiResponse.value = response
            }

            override fun setErrorResponse(error: String) {
                val response = GroupDetailsResponse()
                response.error = "true"
                response.message = error
                apiResponse.value = response
            }
        })

        return apiResponse
    }

    suspend fun getGroupExist(roomid: String): Boolean {
        val count = chatListDao?.getRoomExist(roomid.toUpperCase())
        return count != 0
    }

    suspend fun getParticipationExist(roomID: String, userId: String): Boolean {
        val count =
            groupParticipationDao?.getParticipationExist(roomID.toUpperCase(), userId.toUpperCase())
        return count != 0
    }

    suspend fun getParticipants(roomId: String): List<GroupParticipationSchema>? {
        return groupParticipationDao?.getParticipants(roomId.toUpperCase())
    }

    fun markGrpAsRemoved(presentGrpList: List<String>) {
        chatListDao?.markGrpAsRemoved(presentGrpList)
        groupParticipationDao?.removeUsersFromGroup(presentGrpList)
    }

    fun removeParticipants(roomId: String, presentMemberList: List<String>) {
        groupParticipationDao?.removeParticipants(roomId, presentMemberList)
    }

    fun updateGroupInfo(image: String?, name: String, id: String) {
        chatListDao?.updateGroupInfo(image ?: "", name, id.toUpperCase())
    }

    fun updateUserProperties(id: String, image: String, status: String) {
        chatListDao?.updateUserImage(status, image ?: "", id.toUpperCase())
    }

    suspend fun getPrivateMessageUsers(): List<String>? {
        return chatListDao?.getPrivateChatmembers()
    }

    fun getProfilePictures(apiParams: ApiInput): MutableLiveData<GetUserProfile>? {
        val apiResponse: MutableLiveData<GetUserProfile> = MutableLiveData()

        Api.postMethod(apiParams, object : ApiResponseCallback {
            override fun setResponseSuccess(jsonObject: JSONObject) {
                val gson = Gson()
                val response: GetUserProfile =
                    gson.fromJson(jsonObject.toString(), GetUserProfile::class.java)
                apiResponse.value = response
            }

            override fun setErrorResponse(error: String) {
                val response = GetUserProfile()
                response.error = true
                response.message = error
                apiResponse.value = response
            }
        })

        return apiResponse
    }

    fun getRoomDetails(roomId: String): ChatListSchema? {
        return chatListDao?.getRoomDataUsingRoomID(roomId)
    }

    fun deleteChats(selectedRoomIds: ArrayList<String>) {
        chatListDao?.deleteChat(selectedRoomIds)
    }

    fun deleteMessages(selectedRoomIds: ArrayList<String>) {
//        chatMessageDao?.deleteRoomsMessage(selectedRoomIds)
        chatMessageDao?.updateRoomsMessageAdDeleted(selectedRoomIds)
    }

    suspend fun removeContacts(selectedContactList: ArrayList<String>) {
        chatListDao?.removeContacts(selectedContactList)
    }

    fun clearAllMessages() {
        chatListDao?.clearAllChatsMesasges(System.currentTimeMillis().toString())
//        chatMessageDao?.clearAllMessage()
        chatMessageDao?.updateAsDeleteAllMessage()
    }

    suspend fun isUserBlocked(kryptId: String): Int? {
        return blockListDao?.isBlocked(kryptId.toUpperCase())
    }

    fun insertBlockedUser(kryptId: String) {
        val blockListSchema = BlockListSchema()
        blockListSchema.kryptId = kryptId.toUpperCase()
        blockListSchema.kryptId = kryptId.toUpperCase()
        blockListSchema.roomImage = ""
        blockListDao?.blockUser(blockListSchema)
    }

    fun removeUnBlockedUser(kryptList: ArrayList<String>) {
        blockListDao?.removeUnBlockedList(kryptList as List<String>)
    }

    fun burnMessage() {
//        chatMessageDao?.clearAllMessage()
        chatMessageDao?.updateAsDeleteAllMessage()
        chatListDao?.removeAllMessage()

    }

    fun burnSentMessage() {
        Coroutine.iOWorker {
//            chatMessageDao?.clearSentMessage()
            chatMessageDao?.updateAsDeleteSentMessage()
            val list = chatListDao?.getUsers()
            val participationList = groupParticipationDao?.getUsers()

            val userList = HashSet<String>()

            list?.let {
                for (i in list.indices) {
                    userList.add(list[i])
                }
            }

            participationList?.let {
                for (i in participationList.indices) {
                    userList.add(participationList[i])
                }
            }

            userList.forEach {
                XMPPOperations.burnMessage(it)
            }

        }
    }

    fun getOnlineStatus(kryptCode: String): LiveData<ChatListSchema>? {
        return chatListDao?.getonlineStatus(kryptCode.toUpperCase())
    }

    fun updateStatus(kryptId: String, status: Int, lastSeen: String) {
        chatListDao?.updateOnline(kryptId, status, lastSeen)
    }

}