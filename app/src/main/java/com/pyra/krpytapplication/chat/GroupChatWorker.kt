package com.pyra.krpytapplication.chat

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.repositories.implementations.ChatListRepository
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema
import com.pyra.krpytapplication.roomDb.entity.GroupParticipationSchema
import getNameList

class GroupChatWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    private val chatListRepository: ChatListRepository

    val context: Context = appContext
    private val parameters: WorkerParameters = workerParams
    var app = MyApp.getInstance()

    init {
        chatListRepository = ChatListRepository.getInstance(app.getAppDatabase())
    }

    override fun doWork(): Result {
        val name = parameters.inputData.getString("groupName").toString()
        val type = parameters.inputData.getString("groupType").toString()
        val image = parameters.inputData.getString("groupImage").toString()
        val id = parameters.inputData.getString("groupId").toString()

        val chatEntity = ChatListSchema()
        chatEntity.chatType = "GROUP"
        chatEntity.groupType = type
        chatEntity.roomId = id
        chatEntity.kryptId = id
        chatEntity.roomName = name
        chatEntity.roomImage = image
        chatEntity.showNotification = true
        chatListRepository.insertData(chatEntity)

        var paticipationList = parameters.inputData.getStringArray("participationList")
        var nameList = getNameList(app.baseContext)

        nameList?.let {

            paticipationList?.let {
                var user = GroupParticipationSchema()

                for (i in paticipationList.indices) {
                    user = GroupParticipationSchema()
                    user.roomId = id
                    user.roomName = name
                    user.userName = if (type == "PRIVATE") nameList[i] else ""
                    user.userImage = ""
                    user.kryptId = paticipationList[i]

                    if (user.kryptId.toUpperCase() != SharedHelper(app.baseContext).kryptKey.toUpperCase())
                        chatListRepository.insertPaticipations(user)
                }

            }
        }

        return Result.success()

    }

}