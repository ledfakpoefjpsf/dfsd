package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.Map;

public class FishCounter extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> showHUD = sgGeneral.add(new BoolSetting.Builder()
        .name("show-hud")
        .description("Show fish count in chat periodically")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> hudInterval = sgGeneral.add(new IntSetting.Builder()
        .name("hud-interval-seconds")
        .description("How often to print fish count to chat")
        .defaultValue(60)
        .min(10)
        .sliderMax(300)
        .build()
    );

    private final Map<String, Integer> fishCounts = new LinkedHashMap<>();
    private int tickCounter = 0;
    private int totalFish = 0;

    // All fish item names to watch for in pickup messages
    private static final Map<String, String> FISH_IDS = Map.of(
        "cod", "Cod",
        "salmon", "Salmon",
        "tropical fish", "Tropical Fish",
        "pufferfish", "Pufferfish",
        "cooked cod", "Cooked Cod",
        "cooked salmon", "Cooked Salmon"
    );

    public FishCounter() {
        super(Addon.CATEGORY, "fish-counter", "Tracks how many of each fish you catch per session.");
    }

    @Override
    public void onActivate() {
        fishCounts.clear();
        totalFish = 0;
        tickCounter = 0;
        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.literal("§aFish Counter started!"), false
            );
        }
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        String msg = event.getMessage().getString().toLowerCase();

        // Watch for item pickup messages
        for (Map.Entry<String, String> entry : FISH_IDS.entrySet()) {
            if (msg.contains(entry.getKey())) {
                // Try to parse count from message e.g. "picked up 3 cod"
                int count = parseCount(msg, entry.getKey());
                fishCounts.merge(entry.getValue(), count, Integer::sum);
                totalFish += count;
                break;
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!showHUD.get() || mc.player == null) return;

        tickCounter++;
        if (tickCounter < hudInterval.get() * 20) return;
        tickCounter = 0;

        printStats();
    }

    private void printStats() {
        if (mc.player == null) return;

        if (fishCounts.isEmpty()) {
            mc.player.displayClientMessage(
                Component.literal("§e[FishCounter] §fNo fish caught yet!"), false
            );
            return;
        }

        mc.player.displayClientMessage(
            Component.literal("§e[FishCounter] §fTotal: §a" + totalFish), false
        );

        for (Map.Entry<String, Integer> entry : fishCounts.entrySet()) {
            mc.player.displayClientMessage(
                Component.literal("§7  " + entry.getKey() + ": §f" + entry.getValue()), false
            );
        }
    }

    private int parseCount(String msg, String fishName) {
        try {
            // Look for patterns like "picked up 3 cod" or "x3 cod"
            String[] words = msg.split(" ");
            for (int i = 0; i < words.length; i++) {
                if (words[i].contains(fishName.split(" ")[0])) {
                    if (i > 0) {
                        String prev = words[i - 1].replace("x", "").replace(",", "");
                        return Integer.parseInt(prev);
                    }
                }
            }
        } catch (NumberFormatException ignored) {}
        return 1;
    }
}
