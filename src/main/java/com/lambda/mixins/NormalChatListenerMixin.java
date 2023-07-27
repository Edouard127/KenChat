package com.lambda.mixins;

import com.lambda.commands.KenChatCommand;
import net.minecraft.client.gui.chat.NormalChatListener;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NormalChatListener.class)
public class NormalChatListenerMixin {
    @Inject(at = @At("HEAD"), method = "say", cancellable = true)
    public void say(ChatType chatTypeIn, ITextComponent message, CallbackInfo ci) {
        if (KenChatCommand.INSTANCE.getEnabled()) {
            ci.cancel();
        }
    }
}
