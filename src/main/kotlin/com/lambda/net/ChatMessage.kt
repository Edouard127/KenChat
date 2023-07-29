package com.lambda.net

import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentBase

class ChatMessage(private val message: String, private val author: String) : TextComponentBase() {
    private val formattedText = message.replace("&", "§")

    override fun getUnformattedComponentText(): String {
        return "${if (author.isEmpty()) "" else "[$author]"} $formattedText"
    }

    override fun createCopy(): ITextComponent {
        return ChatMessage(message, author)
    }
}