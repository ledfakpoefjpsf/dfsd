package com.kkllffaa.meteor_litematica_printer;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class MixinServerPlayer {

    @Inject(method = "getPermissionLevel", at = @At("HEAD"), cancellable = true)
    private void elevatePermission(CallbackInfoReturnable<Integer> cir) {
        ServerPlayer self = (ServerPlayer)(Object)this;
        if (self.getTags().contains("survivalGiveActive")) {
            cir.setReturnValue(2);
        }
    }
}
