package com.lambda.hud

import com.lambda.KenChatPlugin
import com.lambda.client.plugin.api.PluginHudElement
import com.lambda.client.util.graphics.VertexHelper
import com.lambda.client.util.threads.runSafe
import com.lambda.modules.KenChat
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.text.TextComponentString

internal object KenChatTabHud : PluginHudElement(
    name = "KenChatTabHud",
    category = Category.MISC,
    description = "Show the kenchat tab overlay",
    enabledByDefault = true,
    pluginMain = KenChatPlugin
) {
    var playerArray = arrayOf<String>()
    var uptime = ""
    var tps = 0.0

    override fun renderHud(vertexHelper: VertexHelper) {
        super.renderHud(vertexHelper)

        if (!this.mc.gameSettings.keyBindPlayerList.isKeyDown || !KenChat.isConnected()) return

        runSafe {
            var maxUsernameWidth = 0

            for (player in playerArray) {
                val usernameWidth = mc.fontRenderer.getStringWidth(player)
                if (usernameWidth > maxUsernameWidth) {
                    maxUsernameWidth = usernameWidth
                }
            }

            val width = ScaledResolution(KenChatTabHud.mc).scaledWidth - maxUsernameWidth

            val length = playerArray.size
            var i4 = length
            var columnWidth = 1

            while (i4 > 15) {
                columnWidth++
                i4 = (length + columnWidth - 1) / columnWidth
            }

            val i1 = (columnWidth * (maxUsernameWidth + 22)).coerceAtMost(width) / columnWidth
            val j1 = width / 2 - (i1 * columnWidth + (columnWidth - 1) * 5) / 2
            var k1 = ScaledResolution(KenChatTabHud.mc).scaledHeight/2
            var l1 = i1 * columnWidth + (columnWidth - 1) * 5

            val stringList = this.mc.fontRenderer.listFormattedStringToWidth(TextComponentString("There ${if (length > 1) "are $length players" else "is $length player"} connected.").formattedText, width - 50)
            for (s1 in stringList) {
                l1 = l1.coerceAtLeast(this.mc.fontRenderer.getStringWidth(s1))
            }

            val uptimeList = this.mc.fontRenderer.listFormattedStringToWidth(TextComponentString("Uptime: $uptime").formattedText, width - 50)
            for (s1 in uptimeList) {
                l1 = l1.coerceAtLeast(this.mc.fontRenderer.getStringWidth(s1))
            }

            /*val tpsList = this.mc.fontRenderer.listFormattedStringToWidth(TextComponentString("TPS: $tps").formattedText, width - 50)
            for (s1 in tpsList) {
                l1 = l1.coerceAtLeast(this.mc.fontRenderer.getStringWidth(s1))
            }*/

            Gui.drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + i4 * 9, Int.MIN_VALUE)

            for (j4 in 0 until length) {
                val k4 = j4 / i4
                val l4 = j4 % i4
                val i5 = j1 + k4 * i1 + k4 * 5
                val j5 = k1 + l4 * 9
                Gui.drawRect(i5, j5, i5 + i1, j5 + 8, 553648127)
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
                GlStateManager.enableAlpha()
                GlStateManager.enableBlend()
                GlStateManager.tryBlendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO
                )

                mc.fontRenderer.drawStringWithShadow(playerArray[j4], i5.toFloat(), j5.toFloat(), -1)
            }

            k1 += i4 * 9 + 1
            Gui.drawRect(
                width / 2 - l1 / 2 - 1,
                k1 - 1,
                width / 2 + l1 / 2 + 1,
                k1 + stringList.size * this.mc.fontRenderer.FONT_HEIGHT,
                Int.MIN_VALUE
            )

            for (s3 in stringList) {
                val j5 = this.mc.fontRenderer.getStringWidth(s3)
                this.mc.fontRenderer.drawStringWithShadow(s3, (width / 2 - j5 / 2).toFloat(), k1.toFloat(), -1)
                k1 += this.mc.fontRenderer.FONT_HEIGHT
            }

            Gui.drawRect(
                width / 2 - l1 / 2 - 1,
                k1 - 1,
                width / 2 + l1 / 2 + 1,
                k1 + uptimeList.size * this.mc.fontRenderer.FONT_HEIGHT,
                Int.MIN_VALUE
            )

            for (s3 in uptimeList) {
                val j5 = this.mc.fontRenderer.getStringWidth(s3)
                this.mc.fontRenderer.drawStringWithShadow(s3, (width / 2 - j5 / 2).toFloat(), k1.toFloat(), -1)
                k1 += this.mc.fontRenderer.FONT_HEIGHT
            }

            /*Gui.drawRect(
                width / 2 - l1 / 2 - 1,
                k1 - 1,
                width / 2 + l1 / 2 + 1,
                k1 + tpsList.size * this.mc.fontRenderer.FONT_HEIGHT,
                Int.MIN_VALUE
            )

            for (s3 in tpsList) {
                val j5 = this.mc.fontRenderer.getStringWidth(s3)
                this.mc.fontRenderer.drawStringWithShadow(s3, (width / 2 - j5 / 2).toFloat(), k1.toFloat(), -1)
                k1 += this.mc.fontRenderer.FONT_HEIGHT
            }*/
        }
    }
}