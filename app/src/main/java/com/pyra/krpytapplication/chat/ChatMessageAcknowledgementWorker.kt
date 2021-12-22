package com.pyra.krpytapplication.chat

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.repositories.implementations.ChatListRepository
import com.pyra.krpytapplication.repositories.implementations.ChatMessagesRepository
import java.util.*

class ChatMessageAcknowledgementWorker(appContext: Context, workerParams: WorkerParameters): Worker(appContext, workerParams) {
    private val chatListRepository: ChatListRepository
    private val chatMessagesRepository: ChatMessagesRepository

    val context:Context = appContext
    private val parameters:WorkerParameters = workerParams
    var app = MyApp.getInstance()

    init {
        chatListRepository = ChatListRepository.getInstance(app.getAppDatabase())
        chatMessagesRepository = ChatMessagesRepository.getInstance(app.getAppDatabase())
    }

    override fun doWork(): Result {
        val messageId = parameters.inputData.getString("messageId").toString()
        val messageStatus = parameters.inputData.getString("messageStatus").toString()
        chatMessagesRepository.updateMessageStatus(messageId.toUpperCase(Locale.ROOT),messageStatus)
        return  Result.success()
    }

}