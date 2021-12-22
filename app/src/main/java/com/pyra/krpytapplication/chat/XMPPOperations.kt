package com.pyra.krpytapplication.chat

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.*
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.*
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.repositories.interfaces.AuthenticationListner
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema
import com.pyra.krpytapplication.roomDb.entity.ChatMessagesSchema
import isUserOnline
import org.jivesoftware.smack.*
import org.jivesoftware.smack.chat2.Chat
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.filter.MessageTypeFilter
import org.jivesoftware.smack.packet.DefaultExtensionElement
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.Presence
import org.jivesoftware.smack.packet.Stanza
import org.jivesoftware.smack.provider.ProviderManager
import org.jivesoftware.smack.roster.PresenceEventListener
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.roster.RosterListener
import org.jivesoftware.smack.roster.SubscribeListener
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jivesoftware.smackx.chatstates.ChatState
import org.jivesoftware.smackx.chatstates.ChatStateListener
import org.jivesoftware.smackx.chatstates.ChatStateManager
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager
import org.jivesoftware.smackx.disco.packet.DiscoverItems
import org.jivesoftware.smackx.iqlast.LastActivityManager
import org.jivesoftware.smackx.iqlast.packet.LastActivity
import org.jivesoftware.smackx.iqregister.AccountManager
import org.jivesoftware.smackx.mam.MamManager
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jivesoftware.smackx.privacy.PrivacyList
import org.jivesoftware.smackx.privacy.PrivacyListManager
import org.jivesoftware.smackx.privacy.packet.PrivacyItem
import org.jivesoftware.smackx.receipts.DeliveryReceipt
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener
import org.jxmpp.jid.BareJid
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.jid.FullJid
import org.jxmpp.jid.Jid
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.jid.impl.JidCreate.entityBareFrom
import org.jxmpp.jid.parts.Localpart
import org.jxmpp.jid.parts.Resourcepart
import java.io.IOException
import java.net.InetAddress
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object XMPPOperations : ConnectionListener, PresenceEventListener, ReceiptReceivedListener,
    RosterListener,
    SubscribeListener, ChatStateListener, MessageListener, StanzaListener {
    var listener: AuthenticationListner? = null
    lateinit var chatManager: ChatManager

    enum class ConnectionState {
        CONNECTED, AUTHENTICATED, CONNECTING, DISCONNECTING, DISCONNECTED
    }

    enum class LoggedInState {
        LOGGED_IN, LOGGED_OUT
    }

    var ourInstance: XMPPOperations? = null

    @Synchronized
    fun getInstance(): XMPPOperations {

        var xMPPOperations: XMPPOperations? = null

        synchronized(XMPP::class.java) {
            if (ourInstance == null) {
                ourInstance = XMPPOperations
            }
            xMPPOperations = ourInstance as XMPPOperations
        }
        return xMPPOperations!!
    }

    init {
        ProviderManager.addExtensionProvider(
            DeliveryReceipt.ELEMENT,
            DeliveryReceipt.NAMESPACE,
            DeliveryReceipt.Provider()
        )
        ProviderManager.addExtensionProvider(
            DeliveryReceiptRequest.ELEMENT,
            DeliveryReceiptRequest().namespace,
            DeliveryReceiptRequest.Provider()
        )
    }

    private fun getBasicBuilder(): XMPPTCPConnectionConfiguration.Builder {
        val serviceName = JidCreate.domainBareFrom(Constants.XMPPKeys.CHAT_DOMAIN)

        return XMPPTCPConnectionConfiguration.builder()
            .setXmppDomain(Constants.XMPPKeys.CHAT_DOMAIN)
            .setHost(Constants.XMPPKeys.CHAT_DOMAIN)
            .setHostAddress(InetAddress.getByName(Constants.XMPPKeys.CHAT_DOMAIN))
            .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
            .setPort(5222) // Your Port for accepting c2s connection
            .setServiceName(serviceName);
    }

    fun establishConnection() {
        if (XMPP.getInstance().getAbstractConnection() == null) {
            val connConfig: XMPPTCPConnectionConfiguration = getBasicBuilder().build()
            val connection = XMPPTCPConnection(connConfig)
            connection.connect()

            XMPP.getInstance().setAbstractConnection(connection)
        }
    }

    fun registerNewUser(username: String, password: String, imei: String) {
        print("IMEI " + imei)
        val connection = XMPP.getInstance().getAbstractConnection()?.let {
            try {
                val manager: AccountManager = AccountManager.getInstance(it);
                manager.sensitiveOperationOverInsecureConnection(true);
                val props = HashMap<String, String>();
                props["username"] = username
                props.put("password", password)
                props.put("email", imei)
                manager.createAccount(Localpart.from(username), password, props)

            } catch (e: SmackException) {
                e.printStackTrace();
            } catch (e: IOException) {
                e.printStackTrace();
            } catch (e: InterruptedException) {
                e.printStackTrace();
            }
        }
    }

    fun connectToXMPPServer(
        username: String,
        password: String,
        listener: AuthenticationListner
    ) {
        this.listener = listener
        if (XMPP.getInstance().getUserConnection() != null) {
            if (XMPP.getInstance().getUserConnection()!!.isConnected && XMPP.getInstance()
                    .getUserConnection()!!.isAuthenticated
            ) {
                println("LXMPP Already Connected & Authenticated")
                listener.isSuccess(true)
//                listenerInvitation()
                return
            }
        }
        val config = getBasicBuilder().setUsernameAndPassword(username, password).build()
        val connection = XMPPTCPConnection(config)
        connection.addConnectionListener(this)

        connection.setUseStreamManagementResumption(true)
        connection.setUseStreamManagement(true)
        connection.addStanzaAcknowledgedListener(this)
        println("LXMPP Connecting")
        try {
            connection.connect().login()
            XMPP.getInstance().setUserConnection(connection)
            initManagers(connection)
            listener.isSuccess(true)
        } catch (error: SmackException) {
            println("LXMPP Connection Failed ${error.localizedMessage}")
            error.printStackTrace()
        }

    }

    private fun initManagers(connection: XMPPConnection?) {
        DeliveryReceiptManager.getInstanceFor(connection).autoAddDeliveryReceiptRequests()
        DeliveryReceiptManager.getInstanceFor(connection).autoReceiptMode =
            DeliveryReceiptManager.AutoReceiptMode.always
        DeliveryReceiptManager.getInstanceFor(connection).addReceiptReceivedListener(this)

        chatManager = ChatManager.getInstanceFor(connection)
        chatManager.addIncomingListener { from, message, chat ->
            handleIncomingMessage(message, from)
        }

        connection?.addAsyncStanzaListener(this, null)

        connection?.addAsyncStanzaListener(StanzaListener { packet ->

            if (packet is Message) {
                val message = packet as Message
                handleIncomingGroupMessage(message)

            }
        }, MessageTypeFilter.GROUPCHAT)

        val roster: Roster = Roster.getInstanceFor(connection)
        roster.addRosterListener(this)
        roster.addSubscribeListener(this)
        roster.addPresenceEventListener(this)

        roster.subscriptionMode = Roster.SubscriptionMode.accept_all
        println("LXMPP Roster Size " + roster.entries.size)

//        ChatManager.getInstanceFor(connection).addOutgoingListener { to, message, chat ->
//            for (body in message.bodies) {
//                println("Outgoing message: ${body.message} for ${body.language} from $to")
//            }
//        }

    }

    private fun handleIncomingGroupMessage(message: Message) {

        var from = ""
        var messageType = ""
        var roomId = ""
        var roomName = ""
        var roomImage = ""
        var content = ""
        var messageId = ""
        var messageTime = ""
        var mediaThumbUrl = ""
        var mediaUrl = ""
        var mediaLength = ""
        var mediaDocumentName = ""
        var mediaDocumentType = ""
        var groupType = ""


        var isReply = ""
        var replyedKryptId = ""
        var replyedMessageType = ""
        var replyedMessage = ""

        var deleteMessage = ""
        var isBurnMessage = ""

        for (body in message.bodies) {
            when (body.language) {
                "message" -> content = body.message
                "from" -> from = body.message
                "messageType" -> messageType = body.message
                "roomId" -> roomId = body.message
                "messageId" -> messageId = body.message
                "messageTime" -> messageTime = body.message
                "roomName" -> roomName = body.message
                "roomImage" -> roomImage = body.message
                "mediaThumbUrl" -> mediaThumbUrl = body.message
                "mediaUrl" -> mediaUrl = body.message
                "mediaLength" -> mediaLength = body.message
                "mediaDocumentName" -> mediaDocumentName = body.message
                "mediaDocumentType" -> mediaDocumentType = body.message
                "groupType" -> groupType = body.message
                "isReply" -> isReply = body.message
                "replyedKryptId" -> replyedKryptId = body.message
                "replyedMessageType" -> replyedMessageType = body.message
                "replyedMessage" -> replyedMessage = body.message
                "isBurnMessage" -> isBurnMessage = body.message
                "deleteMessage" -> deleteMessage = body.message

            }
        }

        if (from.equals(
                SharedHelper(MyApp.getInstance().baseContext).kryptKey,
                true
            ) || from == "" || from == "null"
        ) {
            return
        }


        if (deleteMessage == "0") {


            val data = Data.Builder()
                .putString("messageId", messageId)
                .putString("messageTime", messageTime)
                .putString("messageType", messageType)
                .putString("roomId", roomId)
                .putString("roomName", roomName)
                .putString("roomImage", roomImage)
                .putString("from", from)
                .putString("message", content)
                .putString("mediaThumbUrl", mediaThumbUrl)
                .putString("mediaUrl", mediaUrl)
                .putString("mediaLength", mediaLength)
                .putString("mediaDocumentName", mediaDocumentName)
                .putString("mediaDocumentType", mediaDocumentType)
                .putString("groupType", groupType)
                .putString("isReply", isReply)
                .putString("replyedKryptId", replyedKryptId)
                .putString("replyedMessageType", replyedMessageType)
                .putString("replyedMessage", replyedMessage)


            val connectRequest: OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<ChatMessageReceiverWorker>()
                    .setInputData(data.build())
                    .build()
            WorkManager
                .getInstance(MyApp.getInstance().applicationContext)
                .enqueueUniqueWork(
                    messageId,
                    ExistingWorkPolicy.KEEP, connectRequest
                )

        } else {

            val data = Data.Builder()
                .putString("isBurnMessage", isBurnMessage)
                .putString("deleteMessage", deleteMessage)
                .putString("messageId", messageId)
                .putString("from", from)


            val connectRequest: OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<ChatMessageDeleteWorker>()
                    .setInputData(data.build())
                    .build()
            WorkManager
                .getInstance(MyApp.getInstance().applicationContext)
                .enqueueUniqueWork(
                    messageId,
                    ExistingWorkPolicy.KEEP, connectRequest
                )

        }
    }

    fun addUserToRoster(kryptId: String) {
        val connection = XMPP.getInstance().getUserConnection()
        if (connection != null) {
            val roster: Roster = Roster.getInstanceFor(connection)
            val jid = JidCreate.from(kryptId.jidString())
            roster.createEntry(jid.asBareJid(), kryptId, null)
        }

    }

    fun updateSeenStatus(messageId: String, kryptId: String) {
        val connection = XMPP.getInstance().getUserConnection()
        if (connection != null) {
            val message = Message()
            message.stanzaId = messageId
            message.from =
                JidCreate.from(SharedHelper(MyApp.getInstance().applicationContext).kryptKey.jidString())
            message.to = JidCreate.from(kryptId.jidString())
            message.addExtension(DefaultExtensionElement("read", "status"))
            connection.sendStanza(message)
        }
    }

    fun updateOnlinePresence(isOnline: Boolean) {
        val presence =
            if (isOnline) Presence(Presence.Type.available) else Presence(Presence.Type.unavailable)
        if (isOnline) {
            presence.mode = Presence.Mode.available
        }
        val connection = XMPP.getInstance().getUserConnection()
        if (connection != null) {
            connection.sendStanza(presence)
            println("LXMPP Presence $isOnline Update successful.")
        } else {
            println("LXMPP Presence $isOnline Update Failed becuase there is no connection yet.")
        }
    }

    private fun handleIncomingMessage(message: Message, from: EntityBareJid) {
        var messageType = ""
        var roomId = ""
        var roomName = ""
        var roomImage = ""
        var content = ""
        var messageId = ""
        var messageTime = ""
        var mediaThumbUrl = ""
        var mediaUrl = ""
        var mediaLength = ""
        var mediaDocumentName = ""
        var mediaDocumentType = ""

        var isReply = ""
        var replyedKryptId = ""
        var replyedMessageType = ""
        var replyedMessage = ""
        var isBurnMessage = ""
        var deleteMessage = ""

        for (body in message.bodies) {
            when (body.language) {
                "message" -> content = body.message
                "messageType" -> messageType = body.message
                "roomId" -> roomId = body.message
                "messageId" -> messageId = body.message
                "messageTime" -> messageTime = body.message
                "roomName" -> roomName = body.message
                "roomImage" -> roomImage = body.message
                "mediaThumbUrl" -> mediaThumbUrl = body.message
                "mediaUrl" -> mediaUrl = body.message
                "mediaLength" -> mediaLength = body.message
                "mediaDocumentName" -> mediaDocumentName = body.message
                "mediaDocumentType" -> mediaDocumentType = body.message
                "isReply" -> isReply = body.message
                "replyedKryptId" -> replyedKryptId = body.message
                "replyedMessageType" -> replyedMessageType = body.message
                "replyedMessage" -> replyedMessage = body.message
                "isBurnMessage" -> isBurnMessage = body.message
                "deleteMessage" -> deleteMessage = body.message
            }
        }


        if (deleteMessage == "0") {
            val data = Data.Builder()
                .putString("messageId", messageId)
                .putString("messageTime", messageTime)
                .putString("messageType", messageType)
                .putString("roomId", roomId)
                .putString("roomName", roomName)
                .putString("roomImage", roomImage)
                .putString("from", from.asEntityBareJidString())
                .putString("message", content)
                .putString("mediaThumbUrl", mediaThumbUrl)
                .putString("mediaUrl", mediaUrl)
                .putString("mediaLength", mediaLength)
                .putString("mediaDocumentName", mediaDocumentName)
                .putString("mediaDocumentType", mediaDocumentType)
                .putString("isReply", isReply)
                .putString("replyedKryptId", replyedKryptId)
                .putString("replyedMessageType", replyedMessageType)
                .putString("replyedMessage", replyedMessage)

            val connectRequest: OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<ChatMessageReceiverWorker>()
                    .setInputData(data.build())
                    .build()
            WorkManager
                .getInstance(MyApp.getInstance().applicationContext)
                .enqueueUniqueWork(
                    messageId,
                    ExistingWorkPolicy.KEEP, connectRequest
                )

        } else {

            val data = Data.Builder()
                .putString("isBurnMessage", isBurnMessage)
                .putString("deleteMessage", deleteMessage)
                .putString("messageId", messageId)
                .putString("from", from.asEntityBareJidString())


            val connectRequest: OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<ChatMessageDeleteWorker>()
                    .setInputData(data.build())
                    .build()
            WorkManager
                .getInstance(MyApp.getInstance().applicationContext)
                .enqueueUniqueWork(
                    messageId,
                    ExistingWorkPolicy.KEEP, connectRequest
                )

        }

    }

    fun sendMessage(to: String, messageSchema: ChatMessagesSchema) {
        try {
            val jid = JidCreate.from(to.jidString())
            val chat: Chat = ChatManager.getInstanceFor(XMPP.getInstance().getUserConnection())
                .chatWith(jid.asEntityBareJidIfPossible())
            val message = Message()
            message.body = messageSchema.message
            message.type = Message.Type.chat
            message.stanzaId = messageSchema.messageId
            message.addBody("message", messageSchema.message)
            message.addBody("roomId", messageSchema.roomId)
            message.addBody("messageType", messageSchema.messageType)
            message.addBody("messageTime", messageSchema.messageTime)
            message.addBody("messageId", messageSchema.messageId)
            message.addBody("roomName", messageSchema.userName)
            message.addBody("roomImage", messageSchema.userImage)
            message.addBody("mediaThumbUrl", messageSchema.mediaThumbUrl)
            message.addBody("mediaUrl", messageSchema.mediaUrl)
            message.addBody("mediaLength", messageSchema.mediaLength)
            message.addBody("mediaDocumentName", messageSchema.mediaDocumentName)
            message.addBody("mediaDocumentType", messageSchema.mediaDocumentType)
            message.addBody("isReply", messageSchema.isReply.toString())
            message.addBody("replyedKryptId", messageSchema.replyedKryptId)
            message.addBody("replyedMessageType", messageSchema.replyedMessageType)
            message.addBody("replyedMessage", messageSchema.replyedMessage)
            message.addBody("deleteMessage", "0")
            message.addBody("isBurnMessage", "0")

//            XMPP.getInstance().getUserConnection()?.addStanzaIdAcknowledgedListener(
//                message.stanzaId
//            ) { packet -> print("Send Status for message" + packet.stanzaId.toString()) }

            chat.send(message)
            DeliveryReceiptRequest.addTo(message)
            println("LXMPP ALL SENDING Stanza Id: ${message.stanzaId} type: ${message.type} body: ${message.body} extension: ${message.extensions[0].elementName}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendGroupMessage(
        to: String,
        messageSchema: ChatMessagesSchema,
        groupType: String
    ) {

        val mutliUserChatMessagerjoin =
            MultiUserChatManager.getInstanceFor(XMPP.getInstance().getUserConnection())

        //joining the group if not joined
        val joinBareId: EntityBareJid =
            entityBareFrom("$to@conference.${Constants.XMPPKeys.CHAT_DOMAIN}")
        val multiUserChatJoin =
            mutliUserChatMessagerjoin.getMultiUserChat(joinBareId)
        if (!multiUserChatJoin.isJoined) {
            multiUserChatJoin.join(Resourcepart.from(SharedHelper(MyApp.getInstance().baseContext).kryptKey))
        }

        val barejid: EntityBareJid =
            entityBareFrom("$to@${Constants.XMPPKeys.CHAT_DOMAIN}")

        val message = Message(barejid, Message.Type.groupchat)
        message.body = messageSchema.message
        message.stanzaId = messageSchema.messageId
        message.addBody("message", messageSchema.message)
        message.addBody("from", SharedHelper(MyApp.getInstance().baseContext).kryptKey)
        message.addBody("roomId", messageSchema.roomId)
        message.addBody("messageType", messageSchema.messageType)
        message.addBody("messageTime", messageSchema.messageTime)
        message.addBody("messageId", messageSchema.messageId)
        message.addBody("roomName", messageSchema.userName)
        message.addBody("roomImage", messageSchema.userImage)
        message.addBody("mediaThumbUrl", messageSchema.mediaThumbUrl)
        message.addBody("mediaUrl", messageSchema.mediaUrl)
        message.addBody("mediaLength", messageSchema.mediaLength)
        message.addBody("mediaDocumentName", messageSchema.mediaDocumentName)
        message.addBody("mediaDocumentType", messageSchema.mediaDocumentType)
        message.addBody("groupType", groupType)

        message.addBody("isReply", messageSchema.isReply.toString())
        message.addBody("replyedKryptId", messageSchema.replyedKryptId)
        message.addBody("replyedMessageType", messageSchema.replyedMessageType)
        message.addBody("replyedMessage", messageSchema.replyedMessage)
        message.addBody("deleteMessage", "0")
        message.addBody("isBurnMessage", "0")

        multiUserChatJoin.sendMessage(message)
        DeliveryReceiptRequest.addTo(message)

    }

    fun updateTypingStatus(kryptId: String, isTyping: Boolean) {
        val jid = JidCreate.from(kryptId.jidString())
        if (this::chatManager.isInitialized) {
            val chat = chatManager.chatWith(jid.asEntityBareJidIfPossible())
            if (isTyping) {
                ChatStateManager.getInstance(XMPP.getInstance().getUserConnection())
                    .setCurrentState(ChatState.composing, chat)
            } else {
                ChatStateManager.getInstance(XMPP.getInstance().getUserConnection())
                    .setCurrentState(ChatState.paused, chat)
            }
        }
    }

    override fun connected(connection: XMPPConnection?) {
        println("LXMPP connected")
    }

    override fun connectionClosed() {
        println("LXMPP closed")
    }

    override fun connectionClosedOnError(e: Exception?) {
        println("LXMPP closed on error")
    }

    override fun authenticated(connection: XMPPConnection?, resumed: Boolean) {
        listener?.isSuccess(true)
//        listenerInvitation()
        println("LXMPP authenticated. Resumed $resumed")
    }

    override fun presenceAvailable(address: FullJid?, availablePresence: Presence?) {
        val kryptId = address?.asBareJid()?.bareUsername()
        println("LXMPP Presence Available for " + kryptId + " which is " + availablePresence?.mode?.name)
        if (kryptId != null)
            updatePresenceToChatPage(availablePresence, kryptId)
    }

    override fun presenceSubscribed(address: BareJid?, subscribedPresence: Presence?) {
        println("LXMPP Presence subscribed for " + address.toString())
    }

    override fun presenceError(address: Jid?, errorPresence: Presence?) {
        println("LXMPP Presence Error " + address.toString())
    }

    override fun presenceUnsubscribed(address: BareJid?, unsubscribedPresence: Presence?) {
        println("LXMPP Presence Unsubscribed for " + address.toString())
    }

    override fun presenceUnavailable(address: FullJid?, presence: Presence?) {
        val kryptId = address?.asBareJid()?.bareUsername()
        println("LXMPP Presence Unavailable for " + address.toString())
        if (kryptId != null)
            updatePresenceToChatPage(presence, kryptId)
    }

    override fun onReceiptReceived(
        fromJid: Jid?,
        toJid: Jid?,
        receiptId: String?,
        receipt: Stanza?
    ) {
        val elementName = receipt?.extensions?.get(0)!!.elementName
        println("LXMPP $fromJid to $toJid delivery for: $receiptId received. The receipt is $elementName")
        if (elementName == "received") {
            if (receiptId != null) {
                updateMessageStatus(receiptId, "delivered")
            }

        }
    }

    private fun updateMessageStatus(messageId: String, messageStatus: String) {
        val data = Data.Builder()
            .putString("messageId", messageId)
            .putString("messageStatus", messageStatus)

        val connectRequest: OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<ChatMessageAcknowledgementWorker>()
                .setInputData(data.build())
                .build()
        WorkManager
            .getInstance(MyApp.getInstance().applicationContext)
            .enqueueUniqueWork(
                messageId + messageStatus,
                ExistingWorkPolicy.KEEP, connectRequest
            )
    }

    override fun entriesDeleted(addresses: MutableCollection<Jid>?) {
        println("LXMPP entries deleted")
    }

    override fun presenceChanged(presence: Presence?) {
        println("LXMPP Presence changed")
    }

    override fun entriesUpdated(addresses: MutableCollection<Jid>?) {
        println("LXMPP entries updated")
    }

    override fun entriesAdded(addresses: MutableCollection<Jid>?) {
        println("LXMPP entries added")
    }

    override fun processSubscribe(
        from: Jid?,
        subscribeRequest: Presence?
    ): SubscribeListener.SubscribeAnswer {
        println("LXMPP subscribed from " + from.toString())
        return SubscribeListener.SubscribeAnswer.Approve
    }

    fun getPresence(kryptId: String): LiveData<Presence> {
        val presence = MutableLiveData<Presence>()
        val connection = XMPP.getInstance().getUserConnection()
        if (connection != null) {
            val roster: Roster = Roster.getInstanceFor(connection)
            val jid = JidCreate.from(kryptId.jidString())
            presence.value = roster.getPresence(jid.asBareJid())
            return presence
        }
        return presence
    }

    private fun getLastSeenTime(kryptId: String): String {
        var lastActivity = ""
        try {
            val connection = XMPP.getInstance().getUserConnection()
            if (connection != null) {
                val lastactivity: LastActivityManager =
                    LastActivityManager.getInstanceFor(connection)
                val jid = JidCreate.from(kryptId.jidString())
                val result: LastActivity = lastactivity.getLastActivity(jid) // user jid
                val currentTime = System.currentTimeMillis() - (result.lastActivity * 1000)
                lastActivity = currentTime.toString().longDateToDisplayTimeString(true)
            }
        } catch (e: Exception) {
            return lastActivity
        }
        return lastActivity
    }

    fun getLastActivity(kryptId: String): LiveData<String> {
        val lastActivity = MutableLiveData<String>()
        try {
            val connection = XMPP.getInstance().getUserConnection()
            if (connection != null) {
                val lastactivity: LastActivityManager =
                    LastActivityManager.getInstanceFor(connection)
                val jid = JidCreate.from(kryptId.jidString())
                val result: LastActivity = lastactivity.getLastActivity(jid) // user jid
                val currentTime = System.currentTimeMillis() - (result.lastActivity * 1000)
                lastActivity.value = currentTime.toString().longDateToDisplayTimeString(true)
            }
        } catch (e: Exception) {
            return lastActivity
        }
        return lastActivity
    }

    fun updatePresenceToChatPage(
        availablePresence: Presence?,
        kryptId: String
    ) {
        val localBroadcastManager =
            LocalBroadcastManager.getInstance(MyApp.getInstance().applicationContext)
        var presenceInfo = ""
        if (availablePresence?.isUserOnline()!!) {
            presenceInfo = MyApp.getInstance().applicationContext.getString(R.string.online)
        } else {
            presenceInfo =
                MyApp.getInstance().applicationContext.getString(R.string.last_seen_at) + getLastSeenTime(
                    kryptId
                )
        }
        val localIntent = Intent("PRESENCE")
            .putExtra("presenceInfo", presenceInfo)
            .putExtra("kryptId", kryptId)
        localBroadcastManager.sendBroadcast(localIntent)
    }

    override fun stateChanged(chat: Chat?, state: ChatState?, message: Message?) {
        when (state) {
            ChatState.active -> println("LXMPP ChatState active")
            ChatState.composing -> println("LXMPP ChatState composing")
            ChatState.paused -> println("LXMPP ChatState paused")
            ChatState.inactive -> println("LXMPP ChatState inactive")
            ChatState.gone -> println("LXMPP ChatState gone")
            null -> println("LXMPP ChatState null")
        }
    }

    override fun processMessage(message: Message?) {
        println("LXMPP Message received ${message?.body}")
    }

    override fun processStanza(packet: Stanza?) {
        if (packet is Message) {

            if (packet.extensions.size == 0) {
                return
            }
            println("LXMPP ALL Stanza Id: ${packet.stanzaId} type: ${packet.type} body: ${packet.body} extension: ${packet.extensions[0].elementName}")
//            for (extension in message.extensions) {
//                println("LXMPP Stanza From: ${message.from.bareUsername()} ${extension.elementName}: ${message.stanzaId}")
//            }

            if (packet.type == Message.Type.groupchat) {
                handleIncomingGroupMessage(packet)
            }

            if (packet.extensions.size > 0) {
                println(packet.extensions[0].elementName)
                when (packet.extensions[0].elementName) {
                    "composing" -> {
                        val localBroadcastManager =
                            LocalBroadcastManager.getInstance(MyApp.getInstance().applicationContext)
                        val localIntent = Intent("TYPING")
                            .putExtra("typing", true)
                            .putExtra("kryptId", packet.from.bareUsername())
                        localBroadcastManager.sendBroadcast(localIntent)
                    }
                    "paused" -> {
                        val localBroadcastManager =
                            LocalBroadcastManager.getInstance(MyApp.getInstance().applicationContext)
                        val localIntent = Intent("TYPING")
                            .putExtra("typing", false)
                            .putExtra("kryptId", packet.from.bareUsername())
                        localBroadcastManager.sendBroadcast(localIntent)
                    }
                    "read" -> {
                        updateMessageStatus(packet.stanzaId, "read")
                    }
                    "body" -> {
                        updateMessageStatus(packet.stanzaId, "sent")
                    }

                }
            }
            // Do your task
        }

    }

    fun createGroup(
        roomId: String,
        grpName: String,
        userList: ArrayList<ChatListSchema>,
        entity: ChatListSchema
    ) {

        val barejid: EntityBareJid =
            entityBareFrom("$roomId@conference.${Constants.XMPPKeys.CHAT_DOMAIN}")
        val mutliUserChatMessager =
            MultiUserChatManager.getInstanceFor(XMPP.getInstance().getUserConnection())

        val multiUserChat =
            mutliUserChatMessager.getMultiUserChat(barejid)

        multiUserChat.create(Resourcepart.from(roomId))

        val form = multiUserChat.configurationForm
        val submitForm = form.createAnswerForm()

        val owners = ArrayList<String>()
        val admins = ArrayList<String>()

        owners.add(SharedHelper(MyApp.getInstance().baseContext).kryptKey + "@" + Constants.XMPPKeys.CHAT_DOMAIN)

        for (i in 0 until userList.size) {
            admins.add(userList[i].kryptId + "@" + Constants.XMPPKeys.CHAT_DOMAIN)
        }

        submitForm.getField("muc#roomconfig_enablelogging").addValue("1")
        submitForm.getField("x-muc#roomconfig_reservednick").addValue("0")
        submitForm.getField("x-muc#roomconfig_canchangenick").addValue("0")
        submitForm.getField("x-muc#roomconfig_registration").addValue("0")
        submitForm.getField("muc#roomconfig_passwordprotectedroom").addValue("0")
        submitForm.getField("muc#roomconfig_roomname").addValue(grpName)
        submitForm.getField("muc#roomconfig_whois").addValue("participants")
        submitForm.getField("muc#roomconfig_membersonly").addValue("0")
        submitForm.getField("muc#roomconfig_persistentroom").addValue("1")
        submitForm.getField("muc#roomconfig_publicroom").addValue("1")
        submitForm.setAnswer("muc#roomconfig_roomowners", owners)
        submitForm.setAnswer("muc#roomconfig_roomadmins", admins)

        multiUserChat.sendConfigurationForm(submitForm)

        multiUserChat.join(Resourcepart.from(SharedHelper(MyApp.getInstance().baseContext).kryptKey))

        val message = Message()
        // message.setType(Type.normal);  //optional
        message.subject = Constants.XMPPKeys.GROUP_CHAT_INVITATION
        message.body = Constants.XMPPKeys.GROUP_GREETINGS
        message.addBody("groupName", entity.roomName)
        message.addBody("groupType", entity.groupType)
        message.addBody("groupImage", entity.roomImage)
        message.addBody("groupId", entity.roomId)
        message.addBody("from", SharedHelper(MyApp.getInstance().baseContext).kryptKey)

        for (i in userList.indices) {
            val eJId =
                entityBareFrom(userList[i].kryptId + "@" + Constants.XMPPKeys.CHAT_DOMAIN)
            multiUserChat.invite(message, eJId, grpName)
        }


//        Handler(Looper.getMainLooper()).postDelayed({
//
//            var barejid2: EntityBareJid =
//                entityBareFrom("$roomId@${Constants.XMPPKeys.CHAT_DOMAIN}")
//
//            var message2 = Message(barejid2, Message.Type.groupchat)
//            message2.body = "Helloooo"
//            multiUserChat.sendMessage(message2)
//        }, 2000)

    }

    fun listenerInvitation() {
        MultiUserChatManager.getInstanceFor(XMPP.getInstance().getUserConnection())
            .addInvitationListener { _, room, inviter, reason, password, message, invitation ->


                val data = Data.Builder()

                var groupId = ""
                for (body in message.bodies) {

                    when (body.language) {
                        "groupName" -> data.putString("groupName", body.message)
                        "groupType" -> data.putString("groupType", body.message)
                        "groupImage" -> data.putString("groupImage", body.message)
                        "groupId" -> {
                            data.putString("groupId", body.message)
                            groupId = body.message
                        }

                    }
                }

                val barejid: EntityBareJid =
                    entityBareFrom("$groupId@conference.${Constants.XMPPKeys.CHAT_DOMAIN}")
                val mutliUserChatMessager =
                    MultiUserChatManager.getInstanceFor(XMPP.getInstance().getUserConnection())

                val multiUserChat =
                    mutliUserChatMessager.getMultiUserChat(barejid)

                multiUserChat.join(Resourcepart.from(SharedHelper(MyApp.getInstance().baseContext).kryptKey))


//                multiUserChat.addMessageListener {
//
//                    //                    Handler(Looper.getMainLooper()).post {
////                        Toast.makeText(
////                            MyApp.getInstance().baseContext,
////                            it.body?.toString(),
////                            Toast.LENGTH_LONG
////                        ).show()
////                    }
////                    MyApp.getInstance().baseContext
//
//                }

                XMPP.getInstance().getUserConnection()?.addAsyncStanzaListener(this, null)


                val serviceDiscover =
                    ServiceDiscoveryManager.getInstanceFor(XMPP.getInstance().getUserConnection())


                val items = serviceDiscover.discoverItems(barejid)
                val itemList: List<DiscoverItems.Item> = items.items

                val participation = ArrayList<String>()

                for (i in itemList.indices) {
                    participation.add(
                        itemList[i].entityID.toString().substring(
                            itemList[i].entityID.toString().lastIndexOf(
                                "/"
                            ) + 1
                        )
                    )

                    LogUtil.d(
                        "$i username  ",
                        itemList[i].entityID.toString().substring(
                            itemList[i].entityID.toString().lastIndexOf("/") + 1
                        )
                    )
                    LogUtil.d(
                        "$i groupname  ",
                        itemList[i].entityID.toString().substring(
                            0,
                            itemList[i].entityID.toString().lastIndexOf("/")
                        )
                    )
                }

                data.putStringArray("participationList", participation.toTypedArray())

                val connectRequest: OneTimeWorkRequest =
                    OneTimeWorkRequestBuilder<GroupChatWorker>()
                        .setInputData(data.build())
                        .build()

                WorkManager
                    .getInstance(MyApp.getInstance().applicationContext)
                    .enqueueUniqueWork(
                        "chatInvitation",
                        ExistingWorkPolicy.KEEP, connectRequest
                    )

            }
    }

    fun getMessageHistory(roomId: String, lastMessageTime: String) {

        val barejid: EntityBareJid =
            entityBareFrom("$roomId@conference.${Constants.XMPPKeys.CHAT_DOMAIN}")
        val mutliUserChatMessager =
            MultiUserChatManager.getInstanceFor(XMPP.getInstance().getUserConnection())

        val multiUserChat =
            mutliUserChatMessager.getMultiUserChat(barejid)

        val mucConfig =
            multiUserChat.getEnterConfigurationBuilder(
                Resourcepart.from(
                    SharedHelper(
                        MyApp.getInstance().baseContext
                    ).kryptKey
                )
            )

        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)

        val fromdate: Date? = Date(lastMessageTime.toLong())
        fromdate?.let {
            mucConfig.requestHistorySince(it)
        }

        val mucEnterConfig = mucConfig.build()
        multiUserChat.join(mucEnterConfig)
    }

    fun XMPPAddNewPrivacyList(
        userName: String?
    ) {
        val listName = "private"
        val privacyItems: ArrayList<PrivacyItem> = ArrayList()
        val item = PrivacyItem(
            PrivacyItem.Type.jid,
            userName, false, 1
        ).also {

            it.isFilterMessage = true
            it.isFilterPresenceIn = true
            it.isFilterIQ = true
            it.isFilterPresenceOut = true
        }

        privacyItems.add(item)
        // Create the new list.
        try {
            val privacyManager: PrivacyListManager = PrivacyListManager
                .getInstanceFor(XMPP.getInstance().getUserConnection())
            privacyManager.createPrivacyList(listName, privacyItems)

        } catch (e: XMPPException) {
            println("PRIVACY_ERROR: $e")
        }

    }

    fun getBlockedUserList(userId: String): List<String> {

        val privacyList = ArrayList<String>()
        try {
            val privacyManager: PrivacyListManager = PrivacyListManager
                .getInstanceFor(XMPP.getInstance().getUserConnection());

            val ser = "@" + Constants.XMPPKeys.CHAT_DOMAIN
            var plist: PrivacyList? = null;
            try {
                plist = privacyManager.getPrivacyList("private");
            } catch (e: SmackException.NoResponseException) {
                e.printStackTrace();
            } catch (e: SmackException.NotConnectedException) {
                e.printStackTrace();
            }
            if (plist != null) {// No blacklisted or is not listed, direct getPrivacyList error
                val items: List<PrivacyItem> = plist.getItems()

                for (item: PrivacyItem in items) {
                    val from = item.value.substring(0, item.value.indexOf(ser))
                    if (userId == from) {
                        item.isAllow
                    }
                    // privacyList.add(from);
                }
            } else {
                return privacyList;
            }
        } catch (ex: XMPPException) {
        }
        return privacyList;
    }

    fun changePassword(password: String, function: (Boolean) -> Unit) {

        AccountManager.sensitiveOperationOverInsecureConnectionDefault(true)
        val accountManager: AccountManager =
            AccountManager.getInstance(XMPP.getInstance().getUserConnection())

        try {
            accountManager.changePassword(password)
        } catch (e: SmackException.NoResponseException) {
            function(false)
            return
        } catch (e: XMPPException.XMPPErrorException) {
            function(false)
            return
        } catch (e: SmackException.NotConnectedException) {
            function(false)
            return
        }

        function(true)
    }

    fun deleteMessage(messageId: String, to: String) {

        try {
            val jid = JidCreate.from(to.jidString())
            val chat: Chat = ChatManager.getInstanceFor(XMPP.getInstance().getUserConnection())
                .chatWith(jid.asEntityBareJidIfPossible())

            val message = Message()
            message.body = "Delete message"
            message.type = Message.Type.chat
            message.stanzaId = messageId
            message.addBody("messageId", messageId)
            message.addBody("isBurnMessage", "0")
            message.addBody("deleteMessage", "1")

            chat.send(message)
            DeliveryReceiptRequest.addTo(message)
            println("LXMPP ALL SENDING Stanza Id: ${message.stanzaId} type: ${message.type} body: ${message.body} extension: ${message.extensions[0].elementName}")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteGroupMessage(
        roomId: String,
        messageId: String
    ) {

        val mutliUserChatMessagerjoin =
            MultiUserChatManager.getInstanceFor(XMPP.getInstance().getUserConnection())

        //joining the group if not joined
        val joinBareId: EntityBareJid =
            entityBareFrom("$roomId@conference.${Constants.XMPPKeys.CHAT_DOMAIN}")
        val multiUserChatJoin =
            mutliUserChatMessagerjoin.getMultiUserChat(joinBareId)
        if (!multiUserChatJoin.isJoined) {
            multiUserChatJoin.join(Resourcepart.from(SharedHelper(MyApp.getInstance().baseContext).kryptKey))
        }

        val barejid: EntityBareJid =
            entityBareFrom("$roomId@${Constants.XMPPKeys.CHAT_DOMAIN}")

        val message = Message(barejid, Message.Type.groupchat)
        message.body = "Delete message"
        message.stanzaId = messageId
        message.addBody("messageId", messageId)
        message.addBody("isBurnMessage", "0")
        message.addBody("deleteMessage", "1")
        message.addBody("from", SharedHelper(MyApp.getInstance().baseContext).kryptKey)

        multiUserChatJoin.sendMessage(message)
        DeliveryReceiptRequest.addTo(message)

    }

    fun burnMessage(to: String) {

        try {
            val jid = JidCreate.from(to.jidString())
            val chat: Chat = ChatManager.getInstanceFor(XMPP.getInstance().getUserConnection())
                .chatWith(jid.asEntityBareJidIfPossible())

            val msgId = UUID.randomUUID().toString()
            val message = Message()
            message.body = "Delete message"
            message.type = Message.Type.chat
            message.stanzaId = msgId
            message.addBody("messageId", msgId)
            message.addBody("isBurnMessage", "1")
            message.addBody("deleteMessage", "1")

            chat.send(message)
            DeliveryReceiptRequest.addTo(message)
            println("LXMPP ALL SENDING Stanza Id: ${message.stanzaId} type: ${message.type} body: ${message.body} extension: ${message.extensions[0].elementName}")

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getMessages(kryptId: String, messageToBeTaken: Int): Boolean {

        if (XMPP.getInstance().getUserConnection()!!.isConnected) {
            val jid = JidCreate.from(kryptId.jidString())

            val manager = MamManager.getInstanceFor(XMPP.getInstance().getUserConnection())
            val r: MamManager.MamQuery = manager.queryMostRecentPage(jid, messageToBeTaken)

            val messagesList = r.messages

            LogUtil.d("Messages ", messagesList.toString())
            if (messagesList.size != 0) {
                for (i in messagesList.indices) {
                    handleIncomingMessage(
                        messagesList[i],
                        messagesList[i].from.asEntityBareJidIfPossible()
                    )
                }
            }

            return true
        } else {
            return false
        }

    }

}