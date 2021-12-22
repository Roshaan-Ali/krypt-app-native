package com.pyra.krpytapplication.chat

import org.jivesoftware.smack.packet.ExtensionElement

import org.jivesoftware.smack.provider.EmbeddedExtensionProvider

import org.jivesoftware.smack.util.XmlStringBuilder


class MessageIdExtension : ExtensionElement {
    //__________________________________________________________________________________________________
    var replyText: String? = null

    override fun getElementName(): String {
        return ELEMENT
    }

    override fun toXML(enclosingNamespace: String?): CharSequence {
        TODO("Not yet implemented")
    }

    override fun getNamespace(): String {
        return NAMESPACE
    }

    fun toXML(): XmlStringBuilder {
        val xml = XmlStringBuilder(this)
        xml.attribute(ATTRIBUTE_REPLY_TEXT, replyText)
        xml.closeEmptyElement()
        return xml
    }

    //__________________________________________________________________________________________________
    class Provider : EmbeddedExtensionProvider<MessageIdExtension>() {
        override fun createReturnExtension(
            currentElement: String,
            currentNamespace: String,
            attributeMap: Map<String, String>,
            content: List<ExtensionElement>
        ): MessageIdExtension {
            val repExt = MessageIdExtension()
            repExt.replyText = attributeMap[ATTRIBUTE_REPLY_TEXT]
            return repExt
        }
    }

    companion object {
        const val NAMESPACE = "shayan:reply"
        const val ELEMENT = "reply"
        const val ATTRIBUTE_REPLY_TEXT = "rText"
    }
}
