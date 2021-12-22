package com.pyra.krpytapplication.chat

import org.jivesoftware.smack.AbstractXMPPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnection

public object XMPP {
    private var abstractXMPPConnection: AbstractXMPPConnection? = null
    private var concreteXMPPConnection: XMPPTCPConnection? = null
    var ourInstance: XMPP? = null

    @Synchronized
    fun getInstance(): XMPP {

        var xMPPLogic: XMPP? = null

        synchronized(XMPP::class.java) {
            if (ourInstance == null) {
                ourInstance = XMPP
            }
            xMPPLogic = ourInstance as XMPP
        }
        return xMPPLogic!!
    }

    internal  fun getAbstractConnection(): AbstractXMPPConnection? {
        return abstractXMPPConnection
    }

    internal fun setAbstractConnection(connection: AbstractXMPPConnection) {
        abstractXMPPConnection = connection
    }


    internal  fun getUserConnection(): XMPPTCPConnection? {
        return concreteXMPPConnection
    }

    internal fun setUserConnection(connection: XMPPTCPConnection) {
        concreteXMPPConnection = connection
    }

}