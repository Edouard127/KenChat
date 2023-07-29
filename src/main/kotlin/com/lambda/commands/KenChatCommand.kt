package com.lambda.commands

import com.lambda.client.command.ClientCommand
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.modules.KenChat

object KenChatCommand : ClientCommand(
    name = "chat",
    description = "Toggle the chat module",
) {
    var enabled = true
    init {
        execute {
            enabled = !enabled
            MessageSendHelper.sendChatMessage("KenChat is now ${if (enabled) "enabled" else "disabled"}")
            if (!enabled) KenChat.doDisconnect()
            else KenChat.doConnect()
        }
    }
}