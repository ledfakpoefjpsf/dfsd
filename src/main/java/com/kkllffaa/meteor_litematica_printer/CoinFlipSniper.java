package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoinFlipSniper extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> minAmountSetting = sgGeneral.add(new StringSetting.Builder()
        .name("min-amount")
        .description("Minimum amount to snipe e.g. $10K, $1M, 2 GC")
        .defaultValue("$10K")
        .build()
    );

    private final Setting<String> maxAmountSetting = sgGeneral.add(new StringSetting.Builder()
        .name("max-amount")
        .description("Maximum amount to snipe e.g. $1M, $10M, 5 GC")
        .defaultValue("$1M")
        .build()
    );

    private final Setting<Boolean> gcMode = sgGeneral.add(new BoolSetting.Builder()
        .name("gc-mode")
        .description("Snipe GC coinflips instead of $ coinflips")
        .defaultValue(false)
        .build()
    );

    // Pattern to match: [Rank] PlayerName made a Coinflip for: $10K or 2 GC
    private static final Pattern CF_PATTERN = Pattern.compile(
        "\\(/cf\\).*?(\\w+) made a.*?Coinflip.*?for: (.+)"
    );

    public CoinFlipSniper() {
        super(Addon.CATEGORY, "coinflip-sniper", "Auto-accepts coinflips in chat within your set range.");
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        if (mc.player == null) return;

        String msg = event.getMessage().getString();
        if (!msg.contains("(/cf)") || !msg.contains("made a")) return;

        Matcher matcher = CF_PATTERN.matcher(msg);
        if (!matcher.find()) return;

        String playerName = matcher.group(1).trim();
        String amountStr = matcher.group(2).trim();

        // Skip our own flips
        if (playerName.equalsIgnoreCase(mc.player.getName().getString())) return;

        // Check if it matches GC mode setting
        boolean isGC = !amountStr.startsWith("$");
        if (isGC != gcMode.get()) return;

        long amount = parseAmount(amountStr);
        long min = parseAmount(minAmountSetting.get());
        long max = parseAmount(maxAmountSetting.get());

        if (amount < min || amount > max) return;

        // Accept the flip!
        mc.getConnection().sendCommand("cf accept " + playerName);
        mc.player.displayClientMessage(Component.literal(
            "§aSniped §e" + playerName + "§a's coinflip for §e" + amountStr
        ), false);
    }

    private long parseAmount(String input) {
        if (input == null || input.isEmpty()) return 0;
        String s = input.trim().replace(",", "").replace("$", "").toUpperCase();
        try {
            if (s.endsWith("B")) return (long)(Double.parseDouble(s.replace("B", "")) * 1_000_000_000);
            if (s.endsWith("M")) return (long)(Double.parseDouble(s.replace("M", "")) * 1_000_000);
            if (s.endsWith("K")) return (long)(Double.parseDouble(s.replace("K", "")) * 1_000);
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
