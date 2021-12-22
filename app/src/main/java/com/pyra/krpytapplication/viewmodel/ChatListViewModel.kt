package com.pyra.krpytapplication.viewmodel

import android.app.Application
import android.content.Intent
import android.database.sqlite.SQLiteException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.work.*
import com.pyra.krpytapplication.utils.*
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.chat.ChatRoomConnection
import com.pyra.krpytapplication.chat.GetOfflineMessagesWorker
import com.pyra.krpytapplication.model.BlockedUsers
import com.pyra.krpytapplication.model.ChatUsers
import com.pyra.krpytapplication.model.GroupDetails
import com.pyra.krpytapplication.repositories.implementations.AmazonRepository
import com.pyra.krpytapplication.repositories.implementations.ChatListRepository
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema
import com.pyra.krpytapplication.roomDb.entity.GroupParticipationSchema
import com.pyra.network.UrlHelper
import getApiParams
import getNameList
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatListViewModel(application: Application) : AndroidViewModel(application) {

    var chatListRepository: ChatListRepository
    var applicationInstance: Application? = null
    var sharedHelper: SharedHelper? = null
    var chatList = ArrayList<ChatListSchema>()

    var namedContacts = ArrayList<ChatListSchema>()
    var unNamedContacts = ArrayList<ChatListSchema>()
    var updateUnnameList: MutableLiveData<Boolean>? = MutableLiveData()
    var updateNameList: MutableLiveData<Boolean>? = MutableLiveData()
    var update: MutableLiveData<Boolean> = MutableLiveData()
    var chatListCount: MutableLiveData<Int> = MutableLiveData()
    var amazonRepository = AmazonRepository.getInstance()

    //create group
    var selectedList = ArrayList<ChatListSchema>()
    var selectedListUpdate: MutableLiveData<Boolean> = MutableLiveData()
    var groupCreated: MutableLiveData<Boolean> = MutableLiveData()

    var errorMessage: MutableLiveData<String> = MutableLiveData()

    var isMultiSelectionEnabled = false
    private var selectedRoomIds = ArrayList<String>()
    var selectedRoomName = ""

    var selectedContactList = ArrayList<String>()

    var selectedDetail = ChatListSchema()
    var notifyContact: MutableLiveData<Void>? = MutableLiveData<Void>()

    init {
        applicationInstance = application
        chatListRepository =
            ChatListRepository.getInstance((applicationInstance as MyApp).getAppDatabase())
        sharedHelper = SharedHelper(application)
    }

    fun getNamedUser() {

        chatListRepository.getNamedUser()?.observeForever {
            namedContacts = it as ArrayList<ChatListSchema>
            updateNameList?.value = true
        }
    }

    fun getUnnamedUser() {
        chatListRepository.getUnnamedUser()?.observeForever {
            unNamedContacts = it as ArrayList<ChatListSchema>
            updateUnnameList?.value = true
        }
    }

    fun insertData(entity: ChatListSchema) {
        Coroutine.iOWorker {
            try {
                chatListRepository.insertData(entity)
            } catch (e: SQLiteException) {
                println("You've already inserted it once")
            }
        }
    }

    fun insertOrReplace(entity: ChatListSchema) {
        Coroutine.iOWorker {
            chatListRepository.insertOrReplace(entity)
        }
    }

    fun updateUserName(kryptKey: String, userName: String) {
        Coroutine.iOWorker {
            chatListRepository.updateUserName(kryptKey, userName)
        }

    }

    fun clearDb() {
        Coroutine.iOWorker {
            chatListRepository.clearDb()
        }

    }

    fun burnMessage() {
        Coroutine.iOWorker {
            chatListRepository.burnMessage()
        }
    }

    fun getNumberOfChatList(): Int {
        return chatList.size
    }

    fun getChatListDisplayName(at: Int): String? {
        return if (chatList[at].roomName == "") {
            chatList[at].kryptId
        } else {
            chatList[at].roomName
        }
    }

    fun isAlreadyAddedToContacts(at: Int): Boolean {
        if (chatList[at].roomName == "") {
            println("RoomName: " + chatList[at].roomName + " IS: " + false)
        } else {
            println("RoomName: " + chatList[at].roomName + " IS: " + true)
        }
        return chatList[at].roomName != ""
    }

    fun getChatListAsync() {
        chatListRepository.getChatList()?.observeForever(Observer {
            chatList = it as ArrayList<ChatListSchema>
            chatListCount.value = it.size
            update.value = true
        })
    }

    fun getChatList() {
        chatListRepository.getChatList()?.observeForever(Observer {
            chatList = it as ArrayList<ChatListSchema>
            chatListCount.value = it.size
            update.value = true
        })
    }

    fun getChatListImage(at: Int): String? {
        return chatList[at].roomImage
    }

    fun getChatListKryptId(at: Int): String {
        return chatList[at].kryptId
    }

    fun getChatListRoomId(at: Int): String {
        return chatList[at].roomId
    }

    fun isGroupChat(at: Int): Boolean {
        return chatList[at].chatType != "PRIVATE"
    }

    fun getUnreadMessagesCount(at: Int): String {
        return if (chatList[at].unReadCount == 0) {
            ""
        } else {
            chatList[at].unReadCount.toString()
        }
    }

    fun getLastMessage(at: Int): String {
        return chatList[at].lastMessage
    }

    fun getLastMessageTime(at: Int): String {
        return chatList[at].lastMessageTime.longDateToDisplayTimeString(true)
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
        return namedContacts[at].kryptId
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

    fun getSearchedData(searchString: String) {

        chatListRepository.getSearchedList(searchString)?.observeForever {
            chatList = it as ArrayList<ChatListSchema>
            update.value = true
        }

    }

    fun clearLocalStorage() {

        applicationInstance?.baseContext?.getExternalFilesDir(null)!!.absolutePath.let {
            if (File(it).exists()) {
                File(it).deleteRecursively()
            }
        }
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

    fun saveToSingleton(groupType: String) {
        SelectedContactSingleton.getInstance()?.listofUsers = selectedList
        SelectedContactSingleton.getInstance()?.groupType = groupType
    }

    fun getSelectedUsers() {
        SelectedContactSingleton.getInstance()?.listofUsers?.let { selectedList = it }
    }

    fun uploadGroupImage(grpName: String, groupImage: File?) {

        if (groupImage != null) {
            amazonRepository.uploadToAWS(applicationInstance?.baseContext!!, groupImage)
                .observeForever {
                    it.error?.let { error ->
                        if (!error) {
                            it.message?.let { url -> createGroup(grpName, url) }
                        } else {
                            errorMessage.value = it.message
                            groupCreated.value = false
                        }
                    }
                }
        } else {
            createGroup(grpName, "")
        }


    }

    private fun createGroup(grpName: String, groupImageUrl: String) {
        val entity = ChatListSchema()
        val id = UUID.randomUUID().toString()

        entity.chatType = "GROUP"
        SelectedContactSingleton.getInstance()?.groupType?.let { entity.groupType = it }

        entity.kryptId = id
        entity.roomId = id
        entity.roomName = grpName
        entity.roomImage = groupImageUrl


        val nameList = getNameList(applicationInstance?.baseContext!!)

        val jsonObject = JSONObject()
        jsonObject.put(
            Constants.ApiKeys.USERNAME,
            sharedHelper?.kryptKey.toString().toLowerCase() + "@" + Constants.XMPPKeys.CHAT_DOMAIN
        )
        jsonObject.put(Constants.ApiKeys.GROUPNAME, id)
        jsonObject.put(Constants.ApiKeys.GROUPTITLENAME, grpName)
        jsonObject.put(Constants.ApiKeys.DESCRIPTION, "group created by $id")
        jsonObject.put(Constants.ApiKeys.GROUPTYPE, entity.groupType)

        if (groupImageUrl != "") {
            jsonObject.put(Constants.ApiKeys.IMAGE, groupImageUrl)
        }


        chatListRepository.createGroup(
            getApiParams(
                applicationInstance?.baseContext!!,
                jsonObject,
                UrlHelper.CREATEGROUP
            )
        ).observeForever {

            if (it.error == "true") {
                errorMessage.value = it.message
                groupCreated.value = false
            } else {

                addMembersToList(entity, nameList)


            }

        }

    }

    private fun addMembersToList(
        entity: ChatListSchema,
        nameList: List<String>?
    ) {

        val userList = JSONArray()
        for (i in 0 until selectedList.size) {
            val jsonObject = JSONObject()
            jsonObject.put(
                Constants.ApiKeys.USERNAME,
                selectedList[i].kryptId + "@" + Constants.XMPPKeys.CHAT_DOMAIN
            )
            userList.put(jsonObject)
        }

        val jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.USERNAME, userList.toString())
        jsonObject.put(Constants.ApiKeys.GROUPNAME, entity.roomId)
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

    fun getGroupDetails() {

        val jsonObject = JSONObject()
        jsonObject.put(
            Constants.ApiKeys.USERNAME,
            sharedHelper?.kryptKey + "@" + Constants.XMPPKeys.CHAT_DOMAIN
        )
        chatListRepository.getGroupDetails(
            getApiParams(
                applicationInstance?.baseContext!!,
                jsonObject,
                UrlHelper.GETGROUPDETAILS
            )
        ).observeForever {
            if (it.error == "false") {
                syncToDb(it.data.data)
                syncBlockedUser(it.data.blockedUsers)
                getOldMessages(it.data.chatUsers)
            }
        }

    }

    private fun getOldMessages(chatUsers: java.util.ArrayList<ChatUsers>) {

        for (i in 0 until chatUsers.size) {
            val data = Data.Builder()
                .putString("fromId", chatUsers[i].fromUserName)
                .putInt("count", chatUsers[i].count)


            val connectRequest: OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<GetOfflineMessagesWorker>()
                    .setInputData(data.build())
                    .build()
            WorkManager
                .getInstance(MyApp.getInstance().applicationContext)
                .enqueueUniqueWork(
                    chatUsers[i].fromUserName,
                    ExistingWorkPolicy.REPLACE, connectRequest
                )
        }

    }

    private fun syncBlockedUser(blockedUsers: ArrayList<BlockedUsers>) {

        Coroutine.iOWorker {
            val kryptList = ArrayList<String>()
            for (i in blockedUsers.indices) {
                kryptList.add(blockedUsers[i].toUser.toUpperCase())
                val count = chatListRepository.isUserBlocked(blockedUsers[i].toUser)
                if (count == 0) {
                    chatListRepository.insertBlockedUser(blockedUsers[i].toUser.toUpperCase())
                }
            }

            chatListRepository.removeUnBlockedUser(kryptList)
        }
    }

    private fun syncToDb(data: ArrayList<GroupDetails>?) {

        data?.let {

            Coroutine.iOWorker {

                val nameList = getNameList(applicationInstance?.baseContext!!)

                for (i in 0 until data.size) {

                    if (!chatListRepository.getGroupExist(data[i].groupName)) {

                        val entity = ChatListSchema()

                        entity.chatType = "GROUP"
                        entity.groupType = data[i].type
                        entity.showNotification = true
                        entity.kryptId = data[i].groupName
                        entity.roomId = data[i].groupName
                        entity.roomName = data[i].groupTitleName
                        data[i].image?.let { entity.roomImage = it }

                        chatListRepository.insertData(entity)

                        data[i].participants?.let {
                            for (j in 0 until it.size) {

                                val groupParticipationSchema = GroupParticipationSchema()
                                groupParticipationSchema.roomId = entity.roomId
                                groupParticipationSchema.roomName = entity.roomName!!

                                if (entity.groupType == "PRIVATE") {
                                    if (it[j].userName.bareUsername()
                                            .toUpperCase() == sharedHelper?.kryptKey?.toUpperCase()
                                    ) {
                                        groupParticipationSchema.userName = "You"
                                    } else {
                                        nameList?.get(j)
                                            ?.let { groupParticipationSchema.userName = it }
                                    }

                                } else {
                                    if (it[j].userName.bareUsername()
                                            .toUpperCase() == sharedHelper?.kryptKey?.toUpperCase()
                                    ) {
                                        groupParticipationSchema.userName = "You"
                                    } else {
                                        groupParticipationSchema.userName =
                                            it[j].userName.bareUsername()
                                    }
                                }
                                groupParticipationSchema.kryptId = it[j].userName.bareUsername()

                                groupParticipationSchema.userImage = ""
                                groupParticipationSchema.role = it[j].role

                                chatListRepository.insertPaticipations(groupParticipationSchema)
                            }
                        }

                    } else {

                        chatListRepository.updateGroupInfo(
                            data[i].image,
                            data[i].groupTitleName,
                            data[i].groupName
                        )

                        var count: Int? = chatListRepository.getParticipants(it[i].groupName)?.size


                        data[i].participants?.let {
                            for (j in 0 until it.size) {

                                if (!chatListRepository.getParticipationExist(
                                        data[i].groupName,
                                        it[j].userName.bareUsername()
                                    )
                                ) {


                                    val groupParticipationSchema = GroupParticipationSchema()
                                    groupParticipationSchema.roomId = data[i].groupName
                                    groupParticipationSchema.roomName = data[i].groupName


                                    if (data[i].type == "PRIVATE") {
                                        if (it[j].userName.bareUsername()
                                                .toUpperCase() == sharedHelper?.kryptKey?.toUpperCase()
                                        ) {
                                            groupParticipationSchema.userName = "You"
                                        } else {
                                            count?.let { ct -> count = ct + 1 }

                                            nameList?.get(count!!)
                                                ?.let { groupParticipationSchema.userName = it }
                                        }
                                    } else {
                                        if (it[j].userName.bareUsername()
                                                .toUpperCase() == sharedHelper?.kryptKey?.toUpperCase()
                                        ) {
                                            groupParticipationSchema.userName = "You"
                                        } else {
                                            groupParticipationSchema.userName =
                                                it[j].userName.bareUsername()
                                        }
                                    }

                                    groupParticipationSchema.role = it[j].role
                                    groupParticipationSchema.kryptId = it[j].userName.bareUsername()


                                    chatListRepository.insertPaticipations(groupParticipationSchema)
                                }

                            }
                        }

                    }
                }

                applicationInstance?.baseContext!!.startService(
                    Intent(
                        applicationInstance?.baseContext!!,
                        ChatRoomConnection::class.java
                    )
                )

                removeGrpNotExist(it)
                removeUserNotExist(it)

            }

        }
    }

    private fun removeUserNotExist(groupList: ArrayList<GroupDetails>) {

        for (i in groupList.indices) {

            val presentMemberList = ArrayList<String>()
            groupList[i].participants?.let { memberList ->
                for (j in memberList.indices) {
                    presentMemberList.add(memberList[j].userName.bareUsername().toUpperCase())
                }
                chatListRepository.removeParticipants(
                    groupList[i].groupName.toUpperCase(),
                    presentMemberList
                )
            }

        }

    }

    fun removeGrpNotExist(groupList: ArrayList<GroupDetails>) {

        val presentGrpList = ArrayList<String>()
        for (i in groupList.indices) {
            presentGrpList.add(groupList[i].groupName.toUpperCase())
        }
        chatListRepository.markGrpAsRemoved(presentGrpList)

    }

    fun getProfileImages() {

        Coroutine.iOWorker {
            val chatListSchema = chatListRepository.getPrivateMessageUsers()
            chatListSchema?.let {

                val request = JSONObject()
                if (it.isNotEmpty()) {
                    val array = JSONArray()

                    for (i in it.indices) {
                        val jsonObject = JSONObject()
                        jsonObject.put(Constants.ApiKeys.USERNAME, it[i])
                        array.put(jsonObject)
                    }

                    request.put(Constants.ApiKeys.USERNAME, array.toString())

                    Coroutine.mainWorker {
                        chatListRepository.getProfilePictures(
                            getApiParams(
                                applicationInstance?.baseContext!!,
                                request,
                                UrlHelper.GETUSERSPROPERTY
                            )
                        )?.observeForever { response ->
                            Coroutine.iOWorker {
                                if (!response.error) {
                                    if (!response.data.isNullOrEmpty()) {
                                        for (i in response.data!!.indices) {
                                            if (!response.data?.get(i)?.properties.isNullOrEmpty()) {

                                                val userName = response?.data?.get(i)?.username


                                                userName?.let {
                                                    response.data?.get(i)?.properties?.let { properties ->
                                                        var image = ""
                                                        var status = "Available"
                                                        for (j in properties.indices) {


                                                            if (properties[j].key == "image") {
                                                                properties[j].value?.let { image1 ->
                                                                    image = image1
                                                                }
                                                            } else if (properties[j].key == "status" && properties[j].value != "" && properties[j].value != " ") {
                                                                properties[j].value?.let { status1 ->
                                                                    status = status1
                                                                }
                                                            }

                                                            chatListRepository.updateUserProperties(
                                                                userName,
                                                                image,
                                                                status
                                                            )


                                                        }

                                                    }
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }

    }

    fun makeSelection(position: Int) {
        if (selectedRoomIds.contains(chatList[position].roomId.toUpperCase())) {
            selectedRoomIds.remove(chatList[position].roomId.toUpperCase())
        } else {
            selectedRoomIds.add(chatList[position].roomId.toUpperCase())
        }

        isMultiSelectionEnabled = selectedRoomIds.size != 0
        getRoomName()
        update.value = true
    }

    private fun getRoomName() {
        Coroutine.iOWorker {
            if (selectedRoomIds.size == 1) {
                val detail = chatListRepository.getRoomDetails(selectedRoomIds[0])
                detail?.let {
                    if (detail.chatType == "PRIVATE") {
                        if (detail.roomName == "") {
                            selectedRoomName = detail.kryptId
                        } else {
                            detail.roomName?.let { selectedRoomName = it }
                        }
                    } else {
                        detail.roomName?.let { selectedRoomName = it }
                    }

                }
            }

        }
    }

    fun getIsSelected(position: Int): Boolean =
        selectedRoomIds.contains(chatList[position].roomId.toUpperCase())

    fun removeSelection() {
        isMultiSelectionEnabled = false
        selectedRoomIds.clear()
        update.value = true
    }

    fun getSelectedCount(): Int {
        return selectedRoomIds.size
    }

    fun deleteSelectedChats(checked: Boolean) {

        Coroutine.iOWorker {

            chatListRepository.deleteChats(selectedRoomIds)
            chatListRepository.deleteMessages(selectedRoomIds)

            if (checked) {

                Coroutine.mainWorker {
                    removeSelection()
                }

            }

        }
    }

    fun setContactSelected(position: Int) {

        if (selectedContactList.contains(namedContacts[position].kryptId.toUpperCase())) {
            selectedContactList.remove(namedContacts[position].kryptId.toUpperCase())
        } else {
            selectedContactList.add(namedContacts[position].kryptId.toUpperCase())
        }

        updateNameList?.value = true

    }

    fun setKryptContactSelected(position: Int) {

        if (selectedContactList.contains(unNamedContacts[position].kryptId.toUpperCase())) {
            selectedContactList.remove(unNamedContacts[position].kryptId.toUpperCase())
        } else {
            selectedContactList.add(unNamedContacts[position].kryptId.toUpperCase())
        }

        updateUnnameList?.value = true

    }

    fun isContactSelectionEnabled(): Boolean = selectedContactList.size != 0

    fun isContactSelected(position: Int): Boolean =
        selectedContactList.contains(namedContacts[position].kryptId.toUpperCase())

    fun isKryptContactSelected(position: Int): Boolean =
        selectedContactList.contains(unNamedContacts[position].kryptId.toUpperCase())

    fun getisAnyContactSelected(): Boolean = selectedContactList.size != 0
    fun getcontactSelectedCount(): String = selectedContactList.size.toString()

    fun unselectContact() {

        selectedContactList.clear()
        updateNameList?.value = true
        updateUnnameList?.value = true

    }

    fun deleteSelectedContact() {

        Coroutine.iOWorker {
            chatListRepository.removeContacts(selectedContactList)
            Coroutine.mainWorker {
                unselectContact()
                getNamedUser()
                getUnnamedUser()
            }
        }

    }

    fun getContactName() {
        if (selectedContactList.size != 0) {
            Coroutine.iOWorker {
                val details = chatListRepository.getRoomData(selectedContactList[0].toUpperCase())
                details?.let { selectedDetail = it }
                Coroutine.mainWorker { notifyContact?.value = null }
            }

        }
    }

    fun updateValue(name: String) {
        Coroutine.iOWorker {
            chatListRepository.updateUserName(
                selectedDetail.kryptId.toUpperCase(Locale.getDefault()),
                name
            )
            Coroutine.mainWorker {
                unselectContact()
                getNamedUser()
                getUnnamedUser()
            }
        }
    }

    fun checkForBurnMessage() {

        var burnTimeSecs = 0L

        when (sharedHelper?.burnMessageType) {
            MessageBurnType.DAYS.type -> {
                burnTimeSecs = sharedHelper?.burnMessageTime?.toLong()!! * 1440 * 60
            }
            MessageBurnType.HOURS.type -> {
                burnTimeSecs = sharedHelper?.burnMessageTime?.toLong()!! * 60 * 60
            }
            MessageBurnType.MINUTES.type -> {
                burnTimeSecs = sharedHelper?.burnMessageTime?.toLong()!! * 60
            }
            MessageBurnType.SECONDS.type -> {
                burnTimeSecs = sharedHelper?.burnMessageTime?.toLong()!!
            }
        }

        sharedHelper?.isBurnMessageEnabled?.let { isEnabled ->
            if (!isEnabled) {
                return
            }
        }

        sharedHelper?.loggedInTime?.let { it ->
            if (it == "") {
                updateLoginTime()
                return
            }

            val burnTime: Date? = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(it)
            burnTime?.let {
                val calender = Calendar.getInstance()
                calender.time = burnTime

                val currentTime = Calendar.getInstance()

                val diff = currentTime.time.time - calender.time.time

                val secondsInMilli: Long = 1000
                val minutesInMilli = secondsInMilli * 60
                val hoursInMilli = minutesInMilli * 60
                val daysInMilli = hoursInMilli * 24

                val elapsedMins: Long = diff / minutesInMilli
                val elapsedSecs = diff / secondsInMilli
//                Log.d("MessageBurn burntime", burnTime.toString())
//                Log.d("MessageBurn currenttime", currentTime.time.toString())
//                Log.d("MessageBurn  ", elapsedMins.toString())
//                Log.d("MessageBurn  time", burnTimeMins.toString())

                if (elapsedSecs >= burnTimeSecs) {
                    LogUtil.d("Burnmessage ", "cleared")
                    // clearDb()
                    chatListRepository.burnSentMessage()

//                    clearLocalStorage()
                    updateLoginTime()
                }

            }
        }

    }

    fun updateLoginTime() {

        SharedHelper(applicationInstance?.baseContext!!).loggedInTime =
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.ENGLISH
            ).format(Calendar.getInstance().time)

    }

    fun clearAllChats() {
        Coroutine.iOWorker {
            selectedRoomIds.clear()
            isMultiSelectionEnabled = false
            chatListRepository.clearAllMessages()
            Coroutine.mainWorker {
                update.value = true
            }
        }
    }
}