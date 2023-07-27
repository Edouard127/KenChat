package com.lambda.commands

import com.lambda.client.command.ClientCommand
import com.lambda.client.util.text.MessageSendHelper

object KenChatCommand : ClientCommand(
    name = "chat",
    description = "Toggle the chat module",
) {
    var enabled = false
    init {
        execute {
            enabled = !enabled
            MessageSendHelper.sendChatMessage("KenChat is now ${if (enabled) "enabled" else "disabled"}")
        }
    }
}