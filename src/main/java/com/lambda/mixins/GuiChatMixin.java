package com.lambda.mixins;

import com.lambda.modules.KenChat;
import com.lambda.net.TCPSocket;
import com.lambda.net.packet.Packet;
import net.minecraft.client.gui.GuiChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.lambda.net.packet.PacketIdKt.SPacketStartWriting;

@Mixin(GuiChat.class)
public class GuiChatMixin {
    @Inject(at = @At("HEAD"), method = "initGui")
    public void initGui(CallbackInfo ci) {
        TCPSocket socket = KenChat.INSTANCE.getSocket();
        if (socket == null || !socket.isConnected()) {
            return;
        }
        socket.javaWrite(Packet.Companion.marshal(SPacketStartWriting));
    }
}
