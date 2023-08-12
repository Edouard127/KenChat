package com.lambda.modules

import com.lambda.KenChatPlugin
import com.lambda.client.event.events.ConnectionEvent
import com.lambda.client.event.listener.listener
import com.lambda.client.module.Category
import com.lambda.client.plugin.api.PluginModule
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.commands.KenChatCommand
import com.lambda.hud.KenChatTabHud
import com.lambda.net.ChatMessage
import com.lambda.net.TCPSocket
import com.lambda.net.packet.*
import kotlinx.coroutines.runBlocking
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

        listener<ConnectionEvent.Disconnect> {
            doDisconnect()
        }
    }

    private fun doConnect() {
        if (mc.currentServerData == null) return
        if (authDigest.isEmpty()) {
            MessageSendHelper.sendChatMessage("The auth digest is empty. Please reconnect to the server.")
            return
        }

        if (Date().time - authTime > TimeUnit.SECONDS.toMillis(60)) {
            MessageSendHelper.sendChatMessage("Mojang server hash has expired. Please reconnect to the server.")
            return
        }

        thread {
            runBlocking {
                runCatching {
                    if (socket?.isConnected() == true) return@runBlocking
                    socket = TCPSocket(address, port.toInt()).apply {
                        addHandler(CPacketDisconnect to ::handleDisconnect)
                        addHandler(CPacketKeyRequest to ::handleKeyRequest)
                        addHandler(CPacketUptime to ::handleUptime)
                        addHandler(CPacketPlayerInfo to ::handleGetPlayerInfo)
                        addHandler(CPacketSystemMessage to ::handleSystemMessage)
                        addHandler(CPacketPlayerMessage to ::handlePlayerMessage)
                        addHandler(CPacketPlayerMessageBackoff to ::handleBackoffMessage)
                        addHandler(CPacketPlayerList to ::handlePlayerList)
                        addHandler(CPacketKeepAlive to ::handleKeepAlive)
                    }
                    socket!!.doDial()
                }.onSuccess {
                    MessageSendHelper.sendChatMessage("Connected to KenChat server.")
                }.onFailure {
                    it.printStackTrace()
                    doDisconnect()
                }
            }
        }
    }

    private fun doDisconnect() {
        socket?.close()
        socket = null
        KenChatTabHud.uptime = ""
        KenChatTabHud.playerArray = emptyArray()
    }

    private suspend fun handleDisconnect(socket: TCPSocket, packet: Packet) {
        mc.ingameGUI.chatGUI.printChatMessage(ChatMessage("Disconnected from KenChat server: ${readStringFrom(packet.inputStream)}", "System Message"))
        doDisconnect()
    }

    private suspend fun handleKeyRequest(socket: TCPSocket, packet: Packet) {
        socket.writePacket(Packet.marshal(SPacketKeyResponse, mc.session.profile.name, authDigest, mc.currentServerData!!.serverIP, mc.session.profile.id, ClientVersion))
    }

    private suspend fun handleUptime(socket: TCPSocket, packet: Packet) {
        val diff = Date().time - Date(readLongFrom(packet.inputStream)).time
        KenChatTabHud.tps = readDoubleFrom(packet.inputStream)

        val years = TimeUnit.MILLISECONDS.toDays(diff) / 365
        val months = TimeUnit.MILLISECONDS.toDays(diff) / 30 % 12
        val days = TimeUnit.MILLISECONDS.toDays(diff) % 30
        val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

        var string = ""
        if (years > 0) string += "$years ${if (years == 1L) "year" else "years"} "
        if (months > 0) string += "$months ${if (months == 1L) "month" else "months"} "
        if (days > 0) string += "$days ${if (days == 1L) "day" else "days"} "
        if (hours > 0) string += "$hours ${if (hours == 1L) "hour" else "hours"} "
        if (minutes > 0) string += "$minutes ${if (minutes == 1L) "minute" else "minutes"} "
        if (seconds > 0) string += "$seconds ${if (seconds == 1L) "second" else "seconds"} "
        KenChatTabHud.uptime = string
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
        mc.ingameGUI.chatGUI.printChatMessage(ChatMessage(component.unformattedText, readStringFrom(packet.inputStream)).setStyle(component.style))
    }

    private suspend fun handleBackoffMessage(socket: TCPSocket, packet: Packet) {
        val nextMessage = TimeUnit.MILLISECONDS.toSeconds(Date(readLongFrom(packet.inputStream)).time - Date().time)
        mc.ingameGUI.chatGUI.printChatMessageWithOptionalDeletion(ChatMessage("&cYou have been flagged by the server. Please wait $nextMessage seconds.", "System Message"), 82395)
    }

    private suspend fun handlePlayerList(socket: TCPSocket, packet: Packet) {
        KenChatTabHud.playerArray = readArrayFrom(packet.inputStream, String())
    }

    private suspend fun handleKeepAlive(socket: TCPSocket, packet: Packet) {
        socket.writePacket(Packet.marshal(SPacketKeepAlive))
    }

    fun isConnected() = socket?.isConnected() == true && KenChatCommand.enabled
}