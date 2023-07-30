package com.lambda.commands

import com.lambda.client.command.ClientCommand
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.commands.KenChatPlayerInfo.execute
import com.lambda.modules.KenChat
import com.lambda.net.packet.Packet
import com.lambda.net.packet.SPacketChannelInfo
import com.lambda.net.packet.SPacketPlayerInfo
import java.util.*

object KenChatChannelInfo : ClientCommand(
    name = "channel",
    description = "Get info about the connected channel",
) {
    init {
        execute {
            KenChat.socket?.writePacket(Packet.marshal(SPacketChannelInfo))
        }
    }
}