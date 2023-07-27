package com.lambda.mixins;

import com.lambda.modules.KenChat;
import net.minecraft.network.login.client.CPacketEncryptionResponse;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerLoginServer;
import net.minecraft.util.CryptManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.crypto.SecretKey;
import java.math.BigInteger;

@Mixin(NetHandlerLoginServer.class)
public class NetHandlerLoginServerMixin {
    @Shadow @Final private MinecraftServer server;

    @Shadow private SecretKey secretKey;

    @Inject(method = "processEncryptionResponse", at = @At("TAIL"))
    private void processEncryptionResponseMixin(CPacketEncryptionResponse p_147315_1_, CallbackInfo ci) {
        KenChat.INSTANCE.setAuthDigest((new BigInteger(CryptManager.getServerIdHash("", this.server.getKeyPair().getPublic(), this.secretKey))).toString(16));
    }
}
