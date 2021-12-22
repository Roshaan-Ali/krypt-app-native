package com.pyra.krpytapplication.chat

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.utils.bareUsername
import com.pyra.krpytapplication.utils.jidString
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.repositories.implementations.ChatMessagesRepository

class ChatMessageDeleteWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    private val chatMessagesRepository: ChatMessagesRepository

    val context: Context = appContext
    private val parameters: WorkerParameters = workerParams
    var app = MyApp.getInstance()

    init {
        chatMessagesRepository = ChatMessagesRepository.getInstance(app.getAppDatabase())
    }

    override fun doWork(): Result {

        if (SharedHelper(app).kryptKey == "") {
            return Result.success()
        }

        val messageId = parameters.inputData.getString("messageId").toString()
        val isBurnMessage = parameters.inputData.getString("isBurnMessage").toString()
        val from = parameters.inputData.getString("from").toString().jidString()


        if (isBurnMessage == "1") {
            chatMessagesRepository.burnMessage(from.bareUsername(),messageId)
        } else {
            chatMessagesRepository.deleteMessage(messageId)
        }


        return Result.success()

    }

}