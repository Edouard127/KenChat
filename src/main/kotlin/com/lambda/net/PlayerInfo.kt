package com.lambda.net

import java.util.*

class PlayerInfo(
    hostmask: String,
    uuid: UUID,
    loginTime: Date,
    lastMessageTime: Date,
    serverHash: String,
) {
    val messages = mutableListOf<ChatMessage>()

    init {
        messages.add(ChatMessage("====== Player Info ======", ""))
        messages.add(ChatMessage("Hostmask: $hostmask", ""))
        messages.add(ChatMessage("UUID: $uuid", ""))
        messages.add(ChatMessage("Logged at: $loginTime", ""))
        messages.add(ChatMessage("Last Message: $lastMessageTime", ""))
        messages.add(ChatMessage("Server Hash: $serverHash", ""))
        messages.add(ChatMessage("====== End Player Info ======", ""))
    }
}