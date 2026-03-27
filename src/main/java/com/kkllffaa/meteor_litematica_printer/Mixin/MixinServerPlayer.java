package com.kkllffaa.meteor_litematica_printer.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class MixinServerPlayer {

    @Inject(method = "getPermissionLevel()I", at = @At("HEAD"), cancellable = true, remap = false)
    private void elevatePermission(CallbackInfoReturnable<Integer> cir) {
        ServerPlayer self = (ServerPlayer)(Object)this;
        if (self.getTags().contains("survivalGiveActive")) {
            cir.setReturnValue(2);
        }
    }
}
