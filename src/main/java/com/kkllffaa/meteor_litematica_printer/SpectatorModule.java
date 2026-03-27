package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.world.entity.player.Player;

public class SpectatorModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> targetSetting = sgGeneral.add(new StringSetting.Builder()
        .name("target")
        .description("Name of the player to spectate")
        .defaultValue("")
        .build()
    );

    public SpectatorModule() {
        super(Addon.CATEGORY, "spectator-module", "Lock your camera to another player's perspective.");
    }

    @Override
    public void onActivate() {
        if (mc.level == null) return;

        String target = targetSetting.get().trim();
        if (target.isEmpty()) {
            info("Please set a target player name.");
            toggle();
            return;
        }

        Player targetPlayer = mc.level.players().stream()
            .filter(p -> p.getName().getString().equalsIgnoreCase(target))
            .findFirst()
            .orElse(null);

        if (targetPlayer == null) {
            info("Player not found: " + target);
            toggle();
            return;
        }

        mc.setCameraEntity(targetPlayer);
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) mc.setCameraEntity(mc.player);
    }
}
