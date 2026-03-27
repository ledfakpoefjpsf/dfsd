package com.kkllffaa.meteor_litematica_printer;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.CommandSourceStack;

public class SingleplayerCommand extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> commandSetting = sgGeneral.add(new StringSetting.Builder()
        .name("command")
        .description("Command to run (without leading slash).")
        .defaultValue("time set day")
        .build()
    );

    public SingleplayerCommand() {
        super(Addon.CATEGORY, "singleplayer-command", "Run a command on your integrated singleplayer server.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) {
            toggle();
            return;
        }

        var server = mc.getSingleplayerServer();
        if (server == null) {
            mc.player.displayClientMessage(Component.literal("§cSingleplayer world not available."), false);
            toggle();
            return;
        }

        String raw = commandSetting.get().trim();
        if (raw.isEmpty()) {
            mc.player.displayClientMessage(Component.literal("§cCommand is empty."), false);
            toggle();
            return;
        }

        // Strip leading slash if user types one
        if (raw.startsWith("/")) raw = raw.substring(1);

        var uuid = mc.player.getUUID();
        final String cmd = raw;

        server.execute(() -> {
            ServerPlayer sp = server.getPlayerList().getPlayer(uuid);
            if (sp == null) return;

            CommandSourceStack source = sp.createCommandSourceStack().withPermission(4);

            try {
                server.getCommands().performPrefixedCommand(source, cmd);
            } catch (Exception e) {
                sp.sendSystemMessage(Component.literal("§cCommand failed: " + e.getMessage()));
            }
        });

        toggle();
    }
}
