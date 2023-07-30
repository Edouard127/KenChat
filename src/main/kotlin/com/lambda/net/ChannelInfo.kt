package com.lambda.net

import java.util.Date

class ChannelInfo(length: Int, date: Date) {
    val messages = mutableListOf<ChatMessage>()

    init {
        messages.add(ChatMessage("====== Channel Info ======", ""))
        messages.add(ChatMessage("Length: $length", ""))
        messages.add(ChatMessage("Created at: $date", ""))
        messages.add(ChatMessage("====== End Channel Info ======", ""))
    }
}