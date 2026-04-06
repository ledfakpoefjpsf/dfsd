package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.util.Random;

public class AntiAFK extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> intervalSetting = sgGeneral.add(new IntSetting.Builder()
        .name("interval-seconds")
        .description("How often to turn (30s to 5 minutes)")
        .defaultValue(60)
        .min(30)
        .sliderMax(300)
        .build()
    );

    private final Setting<Integer> turnSpeedSetting = sgGeneral.add(new IntSetting.Builder()
        .name("turn-speed")
        .description("How fast to turn (ticks to complete the turn)")
        .defaultValue(10)
        .min(1)
        .sliderMax(40)
        .build()
    );

    private int tickCounter = 0;
    private int turningTick = 0;
    private boolean isTurning = false;
    private float startYaw = 0;
    private float targetYaw = 0;
    private final Random random = new Random();

    public AntiAFK() {
        super(Addon.CATEGORY, "anti-afk", "Turns your character left every set interval to avoid AFK detection.");
    }

    @Override
    public void onActivate() {
        tickCounter = 0;
        isTurning = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        if (isTurning) {
            float progress = (float) turningTick / turnSpeedSetting.get();
            float newYaw = startYaw - (90f * progress); // turn left 90 degrees
            mc.player.setYRot(newYaw);
            turningTick++;

            if (turningTick >= turnSpeedSetting.get()) {
                mc.player.setYRot(targetYaw);
                isTurning = false;
                turningTick = 0;
            }
            return;
        }

        tickCounter++;
        int intervalTicks = intervalSetting.get() * 20; // convert seconds to ticks

        if (tickCounter >= intervalTicks) {
            tickCounter = 0;
            startYaw = mc.player.getYRot();
            targetYaw = startYaw - 90f; // turn left
            isTurning = true;
            turningTick = 0;
        }
    }
}
