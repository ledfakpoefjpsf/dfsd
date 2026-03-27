package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class FastBridgeMacro extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> autoSwing = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-swing")
        .description("Swings your hand automatically while bridging.")
        .defaultValue(true)
        .build()
    );

    public FastBridgeMacro() {
        super(Addon.CATEGORY, "fast-bridge-macro", "High-speed right-click bridge macro.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        if (!(mc.player.getMainHandItem().getItem() instanceof BlockItem)) return;

        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hit = (BlockHitResult) mc.hitResult;

            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);

            if (autoSwing.get()) mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }
}
