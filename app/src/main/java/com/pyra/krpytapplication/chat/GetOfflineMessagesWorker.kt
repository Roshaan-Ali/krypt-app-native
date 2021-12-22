package com.pyra.krpytapplication.chat

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.repositories.implementations.ChatMessagesRepository
import com.pyra.network.UrlHelper
import getApiParams
import org.json.JSONObject

class GetOfflineMessagesWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    val context: Context = appContext
    private val parameters: WorkerParameters = workerParams
    var app = MyApp.getInstance()

    private val chatMessagesRepository: ChatMessagesRepository

    init {
        chatMessagesRepository = ChatMessagesRepository.getInstance(app.getAppDatabase())
    }


    override fun doWork(): Result {

        if (SharedHelper(app).kryptKey == "") {
            return Result.success()
        }

        val fromId = parameters.inputData.getString("fromId").toString()
        val count = parameters.inputData.getInt("count", 0)


        if(count == 0){
            return Result.success()
        }
        var isMessageTriggered = XMPPOperations.getMessages(fromId, count)

        if (isMessageTriggered) {
            val jsonObject = JSONObject()
            jsonObject.put("fromUserName", fromId)
            jsonObject.put("toUserName", SharedHelper(app).kryptKey)

            chatMessagesRepository.resetMessages(
                getApiParams(
                    app.baseContext!!,
                    jsonObject,
                    UrlHelper.RESETMESSAGESTATUS
                )
            )
        }

        return Result.success()

    }

}