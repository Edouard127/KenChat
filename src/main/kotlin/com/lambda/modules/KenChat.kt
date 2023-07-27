package com.lambda.modules

import com.lambda.KenChatPlugin
import com.lambda.client.event.SafeClientEvent
import com.lambda.client.event.events.ConnectionEvent
import com.lambda.client.event.events.PacketEvent
import com.lambda.client.event.listener.listener
import com.lambda.client.module.Category
import com.lambda.client.plugin.api.PluginModule
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.threads.runSafe
import com.lambda.client.util.threads.runSafeR
import com.lambda.client.util.threads.safeListener
import com.lambda.net.TCPSocket
import com.lambda.net.packet.*
import kotlinx.coroutines.runBlocking
import net.minecraft.client.gui.GuiNewChat
import net.minecraft.network.login.server.SPacketEncryptionRequest
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import java.util.*
import kotlin.concurrent.thread

internal object KenChat : PluginModule(
    name = "KenChat",
    description = "Toggle the chat module",
    category = Category.MISC,
    pluginMain = KenChatPlugin,
) {
    private val address by setting("Address", "localhost")
    private val port by setting("Port", "42069")

    val chat = GuiNewChat(mc)
    var authDigest: String = ""

    var socket: TCPSocket? = null

    init {
        listener<ConnectionEvent.Connect> {
            thread {
                runBlocking {
                    runCatching {
                        if (socket?.isConnected() == true) return@runCatching
                        socket = TCPSocket(address, port.toInt()).apply {
                            addHandler(CPacketKeyRequest to ::handleKeyRequest)
                            addHandler(CPacketSystemMessage to ::handleSystemMessage)
                            addHandler(CPacketPlayerMessage to ::handlePlayerMessage)
                            addHandler(CPacketPlayerMessageBackoff to ::handleBackoffMessage)
                        }
                        socket!!.doDial()
                    }.onSuccess {
                        MessageSendHelper.sendChatMessage("Connected to KenChat server.")
                    }.onFailure {
                        it.printStackTrace()
                        socket?.close()
                    }
                }
            }
        }

        safeListener<ConnectionEvent.Disconnect> {
            socket?.close()
            MessageSendHelper.sendChatMessage("Disconnected from KenChat server.")
        }
    }
}

private suspend fun handleKeyRequest(socket: TCPSocket, packet: Packet) {
    runSafe {
        socket.writePacket(Packet.marshal(SPacketKeyResponse, player.name, player.uniqueID, KenChat.authDigest))
    }
}

private suspend fun handleSystemMessage(socket: TCPSocket, packet: Packet) {
    println("Received message: ${readTextComponentFrom(packet.inputStream!!)}")
}

private suspend fun handlePlayerMessage(socket: TCPSocket, packet: Packet) {
    println("Received message: ${readTextComponentFrom(packet.inputStream!!)}")
}

private suspend fun handleBackoffMessage(socket: TCPSocket, packet: Packet) {
    println(packet.data.joinToString(", "))
    println("Backoff after: ${Date(readLongFrom(packet.inputStream!!))}")
}