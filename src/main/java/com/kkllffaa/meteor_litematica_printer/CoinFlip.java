package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;

public class CoinFlip extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> amountSetting = sgGeneral.add(new StringSetting.Builder()
        .name("amount")
        .description("Amount to coinflip e.g. $10K or 2 GC")
        .defaultValue("$1K")
        .build()
    );

    private int winStreak = 0;
    private int totalWins = 0;
    private int totalLosses = 0;

    public CoinFlip() {
        super(Addon.CATEGORY, "coinflip", "Sends /cf in chat and tracks your win streak.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) return;
        mc.getConnection().sendCommand("cf " + amountSetting.get());
        toggle();
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        String msg = event.getMessage().getString();

        if (msg.contains("You have won the coinflip")) {
            totalWins++;
            winStreak++;
            printStats(true);
        } else if (msg.contains("You have lost the coinflip")) {
            totalLosses++;
            winStreak = 0;
            printStats(false);
        }
    }

    private void printStats(boolean won) {
        if (mc.player == null) return;

        double streakProb = Math.pow(0.5, winStreak) * 100;
        int total = totalWins + totalLosses;
        double winRate = total > 0 ? (totalWins * 100.0 / total) : 0;

        mc.player.displayClientMessage(Component.literal(
            (won ? "§aWon! " : "§cLost! ") +
            "§fStreak: §e" + winStreak +
            " §f| Next flip: §e50%" +
            " §f| Streak prob: §e" + String.format("%.2f", streakProb) + "%" +
            " §f| Win rate: §e" + String.format("%.1f", winRate) + "%" +
            " §f(" + totalWins + "W/" + totalLosses + "L)"
        ), false);
    }
}
