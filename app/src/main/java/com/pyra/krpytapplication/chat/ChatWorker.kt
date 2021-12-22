package com.pyra.krpytapplication.chat

import android.content.Context
import androidx.work.*
import com.pyra.krpytapplication.utils.LogUtil
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.repositories.interfaces.AuthenticationListner


class ChatWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    val context: Context = appContext
    private val parameters: WorkerParameters = workerParams
    override fun doWork(): Result {
//        println("LXMPP Init " + parameters.inputData.getString("Type"))
        val helper = SharedHelper(context)
        LogUtil.e("worker", "true ")
        if (helper.loggedIn) {
            MyApp.xmppOperations.connectToXMPPServer(
                helper.kryptKey,
                helper.password,
                object : AuthenticationListner {
                    override fun isSuccess(status: Boolean) {


                        MyApp.xmppOperations.getInstance().updateOnlinePresence(true)
                        val data = Data.Builder()
                        val connectRequest: OneTimeWorkRequest =
                            OneTimeWorkRequestBuilder<GetGroupMessagesWorker>()
                                .setInputData(data.build())
                                .build()

                        WorkManager
                            .getInstance(context)
                            .enqueueUniqueWork(
                                "GroupRequest",
                                ExistingWorkPolicy.KEEP, connectRequest
                            )

                    }
                })
        }



        return Result.success()
    }
}