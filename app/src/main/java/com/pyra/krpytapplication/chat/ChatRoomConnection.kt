package com.pyra.krpytapplication.chat

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.LogUtil
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.repositories.implementations.ChatListRepository
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema
import org.jetbrains.anko.doAsync
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.jid.parts.Resourcepart

class ChatRoomConnection : Service() {

    var chatRoomRepository: ChatListRepository? = null
    var handler = Handler()
    lateinit var runnable: Runnable

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        chatRoomRepository = ChatListRepository.getInstance(MyApp.getInstance().getAppDatabase())

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        initRunnable()

        handler.postDelayed(runnable, 2000)
        return START_STICKY
    }

    private fun initRunnable() {


        runnable = Runnable {
            doAsync {
                if (!SharedHelper(this@ChatRoomConnection).loggedIn) {
                    return@doAsync
                }

                var roomList: List<ChatListSchema>? = chatRoomRepository?.getGroupRoom()

                roomList?.let {
                    for (i in roomList.indices) {
                        joinGroup(roomList[i].roomId, roomList[i].lastMessageTime)
                    }
                }

//                var userList : List<ChatListSchema>? = chatRoomRepository?.getUsersList()
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

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d("Destroyed Service", ":)")
    }

}