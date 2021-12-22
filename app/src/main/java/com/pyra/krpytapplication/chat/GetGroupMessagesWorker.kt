package com.pyra.krpytapplication.chat

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.repositories.implementations.ChatListRepository
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema
import org.jetbrains.anko.doAsync
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.jid.parts.Resourcepart


class GetGroupMessagesWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    val context: Context = appContext
    private val parameters: WorkerParameters = workerParams

    var chatRoomRepository: ChatListRepository? = null
    var handler = Handler(Looper.getMainLooper())
    lateinit var runnable: Runnable

    override fun doWork(): Result {

        chatRoomRepository = ChatListRepository.getInstance(MyApp.getInstance().getAppDatabase())
        initRunnable()


        val helper = SharedHelper(context)
        if (helper.loggedIn) {
            handler.postDelayed(runnable, 2000)
        }

        return Result.success()
    }


    private fun initRunnable() {


        runnable = Runnable {
            doAsync {
                if (!SharedHelper(context).loggedIn) {
                    return@doAsync
                }

                var roomList: List<ChatListSchema>? = chatRoomRepository?.getGroupRoom()

                roomList?.let {
                    for (i in roomList.indices) {
                        joinGroup(roomList[i].roomId, roomList[i].lastMessageTime)
                    }
                }

//                var userList: List<ChatListSchema>? = chatRoomRepository?.getUsersList()
//
//                userList?.let {
//                    for (i in userList.indices) {
//                        XMPPOperations.getMessages(userList[i].kryptId)
//                    }
//                }

                XMPPOperations.updateOnlinePresence(true)

//                stopSelf()
            }

        }

    }

    private fun joinGroup(roomId: String, lastMessageTime: String) {

        val mutliUserChatMessagerjoin =
            MultiUserChatManager.getInstanceFor(XMPP.getInstance().getUserConnection())

        //joining the group if not joined
        val joinBareId: EntityBareJid =
            JidCreate.entityBareFrom("$roomId@conference.${Constants.XMPPKeys.CHAT_DOMAIN}")
        val multiUserChatJoin =
            mutliUserChatMessagerjoin.getMultiUserChat(joinBareId)
        if (!multiUserChatJoin.isJoined) {
            multiUserChatJoin.join(Resourcepart.from(SharedHelper(MyApp.getInstance().baseContext).kryptKey))
        }

        if (lastMessageTime != "") {
            XMPPOperations.getMessageHistory(roomId, lastMessageTime)
        }

    }


}