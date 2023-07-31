package com.lambda.modules

import com.lambda.KenChatPlugin
import com.lambda.client.event.events.ConnectionEvent
import com.lambda.client.event.listener.listener
import com.lambda.client.module.Category
import com.lambda.client.plugin.api.PluginModule
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.threads.safeListener
import com.lambda.hud.KenChatTabHud
import com.lambda.net.ChatMessage
import com.lambda.net.TCPSocket
import com.lambda.net.packet.*
import kotlinx.coroutines.runBlocking
import net.minecraft.client.gui.GuiPlayerTabOverlay
import net.minecraft.util.text.TextComponentString
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

internal object KenChat : PluginModule(
    name = "KenChat",
    description = "Toggle the chat module",
    category = Category.MISC,
    pluginMain = KenChatPlugin,
) {
    private val address by setting("Address", "198.100.155.191")
    private val port by setting("Port", "42069")

    var authDigest: String = ""
    var authTime: Long = 0

    var socket: TCPSocket? = null

    init {
        onEnable { doConnect() }
        onDisable { doDisconnect() }

        listener<ConnectionEvent.Connect> {
            doConnect()
        }

        safeListener<ConnectionEvent.Disconnect> {
            doDisconnect()
        }
    }

    private fun doConnect() {
        if (mc.currentServerData == null) return
        if (authDigest.isEmpty()) {
            MessageSendHelper.sendChatMessage("The auth digest is empty. Please reconnect to the server.")
            return
        }

        if (Date().time - authTime > TimeUnit.MINUTES.toMillis(20)) {
            MessageSendHelper.sendChatMessage("Mojang server hash has expired. Please reconnect to the server.")
        }

        thread {
            runBlocking {
                runCatching {
                    if (socket?.isConnected() == true) return@runBlocking
                    socket = TCPSocket(address, port.toInt()).apply {
                        addHandler(CPacketKeyRequest to ::handleKeyRequest)
                        addHandler(CPacketChannelInfo to ::handleGetChannelInfo)
                        addHandler(CPacketPlayerInfo to ::handleGetPlayerInfo)
                        addHandler(CPacketSystemMessage to ::handleSystemMessage)
                        addHandler(CPacketPlayerMessage to ::handlePlayerMessage)
                        addHandler(CPacketPlayerMessageBackoff to ::handleBackoffMessage)
                        addHandler(CPacketPlayerList to ::handlePlayerList)
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

    private fun doDisconnect() {
        socket?.close()
        socket = null
    }

    private suspend fun handleKeyRequest(socket: TCPSocket, packet: Packet) {
        socket.writePacket(Packet.marshal(SPacketKeyResponse, mc.session.profile.name, authDigest, mc.currentServerData!!.serverIP, mc.session.profile.id))
    }

    private suspend fun handleGetChannelInfo(socket: TCPSocket, packet: Packet) {
        val connectedPlayers = readIntFrom(packet.inputStream)
        val channelCreationTime = Date(readLongFrom(packet.inputStream))

        mc.ingameGUI.chatGUI.printChatMessageWithOptionalDeletion(TextComponentString("====== Channel Info ======\nConnected Players: $connectedPlayers\nChannel Creation Time: $channelCreationTime\n====== End Channel Info ======"), 13756)
    }

    private suspend fun handleGetPlayerInfo(socket: TCPSocket, packet: Packet) {
        val hostmask = readStringFrom(packet.inputStream)
        val uuid = readUUIDFrom(packet.inputStream)
        val loginTime = Date(readLongFrom(packet.inputStream))
        val lastMessageTime = Date(readLongFrom(packet.inputStream))
        val serverHash = readStringFrom(packet.inputStream)

        mc.ingameGUI.chatGUI.printChatMessageWithOptionalDeletion(TextComponentString("====== Player Info ======\nHostmask: $hostmask\nUUID: $uuid\nLogged at: $loginTime\nLast Message: $lastMessageTime\nServer: $serverHash\n====== End Player Info ======"), 18674)
    }

    private suspend fun handleSystemMessage(socket: TCPSocket, packet: Packet) {
        val component = readTextComponentFrom(packet.inputStream)
        mc.ingameGUI.chatGUI.printChatMessage(ChatMessage(component.unformattedText, "System Message"))
    }

    private suspend fun handlePlayerMessage(socket: TCPSocket, packet: Packet) {
        val component = readTextComponentFrom(packet.inputStream)
        mc.currentServerData!!.playerList
        mc.ingameGUI.chatGUI.printChatMessage(ChatMessage(component.unformattedText, readStringFrom(packet.inputStream)).setStyle(component.style))
    }

    private suspend fun handleBackoffMessage(socket: TCPSocket, packet: Packet) {
        val nextMessage = TimeUnit.MILLISECONDS.toSeconds(Date(readLongFrom(packet.inputStream)).time - Date().time)
        mc.ingameGUI.chatGUI.printChatMessage(ChatMessage("&cYou are sending messages too fast! Please wait $nextMessage seconds.", "System Message"))
    }

    private suspend fun handlePlayerList(socket: TCPSocket, packet: Packet) {
        KenChatTabHud.playerArray = readArrayFrom(packet.inputStream, String())
    }
}