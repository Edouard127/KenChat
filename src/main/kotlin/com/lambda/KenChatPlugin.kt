package com.lambda

import com.lambda.client.plugin.api.Plugin
import com.lambda.commands.KenChatCommand
import com.lambda.commands.KenChatPlayerInfo
import com.lambda.hud.KenChatTabHud
import com.lambda.modules.KenChat

internal object KenChatPlugin : Plugin() {
    override fun onLoad() {
        modules.add(KenChat)
        commands.add(KenChatCommand)
        commands.add(KenChatPlayerInfo)
        hudElements.add(KenChatTabHud)
    }

    override fun onUnload() {
        KenChat.socket?.close()
    }
}