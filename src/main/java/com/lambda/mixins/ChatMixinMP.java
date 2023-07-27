package com.lambda.mixins;

import com.lambda.commands.KenChatCommand;
import com.lambda.modules.KenChat;
import com.lambda.net.TCPSocket;
import com.lambda.net.packet.Packet;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

import static com.lambda.net.packet.PacketIdKt.SPacketPlayerMessage;

@Mixin(EntityPlayerMP.class)
public class ChatMixinMP {
    @Inject(at = @At("HEAD"), method = "sendMessage", cancellable = true)
    public void sendMessage(ITextComponent message, CallbackInfo ci) {
        if (!KenChatCommand.INSTANCE.getEnabled()) {
            return;
        }

        System.out.println("Sending message: " + message.getUnformattedText());

        @Nullable
        TCPSocket socket = KenChat.INSTANCE.getSocket();
        if (socket == null) {
            return;
        }

        socket.javaWrite(Packet.Companion.marshal(SPacketPlayerMessage, message));
        ci.cancel();
    }
}