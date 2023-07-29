package com.lambda.modules

import com.lambda.KenChatPlugin
import com.lambda.client.event.events.ConnectionEvent
import com.lambda.client.event.listener.listener
import com.lambda.client.module.Category
import com.lambda.client.plugin.api.PluginModule
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.threads.safeListener
import com.lambda.net.ChatMessage
import com.lambda.net.PlayerInfo
import com.lambda.net.TCPSocket
import com.lambda.net.packet.*
import kotlinx.coroutines.runBlocking
import net.minecraft.util.text.ITextComponent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

internal object KenChat : PluginModule(
    name = "KenChat",
    description = "Toggle the chat module",
    category = Category.MISC,
    pluginMain = KenChatPlugin,
) {
    private val address by setting("Address", "198.100.155.19")
    private val port by setting("Port", "42069")

    var authDigest: String = ""

    var socket: TCPSocket? = null
    private val messages = LinkedList<ITextComponent>()

    init {
        onEnable { doConnect() }

        onDisable { doDisconnect() }

        listener<ConnectionEvent.Connect> {
            doConnect()
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (messages.isEmpty()) return@safeListener
            messages.forEach {
                mc.ingameGUI.chatGUI.printChatMessage(it)
            }
            messages.clear()
        }

        safeListener<ConnectionEvent.Disconnect> {
            doDisconnect()
        }
    }

    fun doConnect() {
        if (authDigest.isEmpty() || mc.currentServerData == null) return

        thread {
            runBlocking {
                runCatching {
                    if (socket?.isConnected() == true) return@runCatching
                    socket = TCPSocket(address, port.toInt()).apply {
                        addHandler(CPacketKeyRequest to ::handleKeyRequest)
                        addHandler(CPacketPlayerInfo to ::handleGetPlayerInfo)
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

    fun doDisconnect() {
        socket?.close()
        MessageSendHelper.sendChatMessage("Disconnected from KenChat server.")
    }

    private suspend fun handleKeyRequest(socket: TCPSocket, packet: Packet) {
        socket.writePacket(Packet.marshal(SPacketKeyResponse, mc.session.profile.name, authDigest, mc.currentServerData!!.serverIP, mc.session.profile.id))
    }

    private suspend fun handleGetPlayerInfo(socket: TCPSocket, packet: Packet) {
        val hostmask = readStringFrom(packet.inputStream)
        val uuid = readUUIDFrom(packet.inputStream)
        val loginTime = Date(readLongFrom(packet.inputStream))
        val lastMessageTime = Date(readLongFrom(packet.inputStream))
        val serverHash = readStringFrom(packet.inputStream)

        val playerInfo = PlayerInfo(hostmask, uuid, loginTime, lastMessageTime, serverHash)
        playerInfo.messages.forEach {
            messages.add(it)
        }
    }

    private suspend fun handleSystemMessage(socket: TCPSocket, packet: Packet) {
        val component = readTextComponentFrom(packet.inputStream)
        messages.add(ChatMessage(component.unformattedText, "System Message"))
    }

    private suspend fun handlePlayerMessage(socket: TCPSocket, packet: Packet) {
        val component = readTextComponentFrom(packet.inputStream)
        messages.add(ChatMessage(component.unformattedText, readStringFrom(packet.inputStream)).setStyle(component.style))
    }

    private suspend fun handleBackoffMessage(socket: TCPSocket, packet: Packet) {
        val nextMessage = TimeUnit.MILLISECONDS.toSeconds(Date(readLongFrom(packet.inputStream)).time - Date().time)
        messages.add(ChatMessage("&cPlease wait $nextMessage seconds before sending another message.", "System Message"))
    }
}