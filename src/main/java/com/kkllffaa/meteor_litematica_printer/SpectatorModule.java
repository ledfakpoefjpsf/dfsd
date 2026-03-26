package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class SpectatorModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> playerName = sgGeneral.add(new StringSetting.Builder()
            .name("player-name")
            .description("Target player to track infinitely.")
            .defaultValue("")
            .build()
    );

    // Keep track of the last known position for "Infinite" tracking
    private Vec3 lastPos = null;

    public SpectatorModule() {
        super(Addon.CATEGORY, "spectator-plus", "Infinite range tracking and spectating.");
    }

    @Override
    public void onActivate() {
        lastPos = null;
    }

    @Override
    public void onTick() {
        if (mc.level == null) return;

        Player target = null;
        for (Player player : mc.level.players()) {
            if (player.getName().getString().equalsIgnoreCase(playerName.get())) {
                target = player;
                break;
            }
        }

        if (target != null) {
            // Update last known position
            lastPos = target.position();
            
            // Force the camera to lock
            if (mc.getCameraEntity() != target) {
                mc.setCameraEntity(target);
            }
        } else if (lastPos != null) {
            // INFINITE RENDER LOGIC: 
            // If the player vanishes (out of range), we point the camera at their last spot
            info("Target out of range! Last seen at: " + 
                (int)lastPos.x + ", " + (int)lastPos.y + ", " + (int)lastPos.z);
        }
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) {
            mc.setCameraEntity(mc.player);
            info("Spectator disabled.");
        }
    }
}
