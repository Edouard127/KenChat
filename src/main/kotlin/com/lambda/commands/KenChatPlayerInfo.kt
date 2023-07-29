package com.lambda.commands

import com.lambda.client.command.ClientCommand
import com.lambda.modules.KenChat
import com.lambda.net.packet.Packet
import com.lambda.net.packet.SPacketPlayerInfo
import java.util.*

object KenChatPlayerInfo : ClientCommand(
    name = "info",
    description = "Get info about a player in KenChat (UUID)",
) {
    init {
        string("uuid") { uuid ->
            execute {
                if (KenChat.socket?.isConnected() == false) return@execute
                KenChat.socket?.writePacket(Packet.marshal(SPacketPlayerInfo, UUID.fromString(uuid.value)))
            }
        }
    }
}