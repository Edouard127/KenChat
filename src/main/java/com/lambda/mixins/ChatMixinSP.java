package com.lambda.mixins;

import com.lambda.commands.KenChatCommand;
import com.lambda.modules.KenChat;
import com.lambda.net.packet.Packet;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextComponentString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Objects;

import static com.lambda.net.packet.PacketIdKt.SPacketPlayerMessage;

@Mixin(EntityPlayerSP.class)
public class ChatMixinSP {
    @Inject(at = @At("HEAD"), method = "sendChatMessage", cancellable = true)
    public void sendMessage(String message, CallbackInfo ci) {
        if (message.startsWith("/")) {
            return;
        }

        if (KenChat.INSTANCE.getSocket() == null || !KenChat.INSTANCE.getSocket().isConnected()) {
            return;
        }

        if (KenChatCommand.INSTANCE.getEnabled()) {
            Objects.requireNonNull(KenChat.INSTANCE.getSocket()).javaWrite(Packet.Companion.marshal(SPacketPlayerMessage, new TextComponentString(message)));
            ci.cancel();
        }
    }
}