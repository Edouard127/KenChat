package com.lambda.mixins;

import com.lambda.modules.KenChat;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(YggdrasilMinecraftSessionService.class)
public class YggdrasilMinecraftSessionServiceMixin {
    @Inject(method = "joinServer", at = @At("HEAD"), remap = false)
    public void joinServer(GameProfile profile, String authenticationToken, String serverId, CallbackInfo ci) {
        KenChat.INSTANCE.setAuthDigest(serverId);
        KenChat.INSTANCE.setAuthTime(System.currentTimeMillis());
    }
}
