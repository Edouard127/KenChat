package com.lambda.mixins;

import com.lambda.commands.KenChatCommand;
import com.lambda.modules.KenChat;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(EntityPlayerSP.class)
public class ChatMixinSP {
    @Inject(at = @At("HEAD"), method = "sendMessage", cancellable = true)
    public void sendMessage(ITextComponent message, CallbackInfo ci) {
        if (!KenChatCommand.INSTANCE.getEnabled()) {
            return;
        }

        Objects.requireNonNull(KenChat.INSTANCE.getChat()).printChatMessageWithOptionalDeletion(message, 0);
        ci.cancel();
    }
}
