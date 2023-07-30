package com.lambda

import com.lambda.client.plugin.api.Plugin
import com.lambda.commands.KenChatChannelInfo
import com.lambda.commands.KenChatCommand
import com.lambda.commands.KenChatPlayerInfo
import com.lambda.modules.KenChat

internal object KenChatPlugin : Plugin() {
    override fun onLoad() {
        modules.add(KenChat)
        commands.add(KenChatCommand)
        commands.add(KenChatPlayerInfo)
        commands.add(KenChatChannelInfo)
    }

    override fun onUnload() {
        KenChat.socket?.close()
    }
}