package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class Phase extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> speedSetting = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Movement speed while phasing")
        .defaultValue(0.5)
        .min(0.1)
        .sliderMax(2.0)
        .build()
    );

    public Phase() {
        super(Addon.CATEGORY, "phase", "Allows you to move through blocks.");
    }

    @Override
    public void onActivate() {
        if (mc.player != null) {
            mc.player.noPhysics = true;
        }
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) {
            mc.player.noPhysics = false;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        mc.player.noPhysics = true;

        double speed = speedSetting.get();
        double x = 0, y = 0, z = 0;

        var options = mc.options;
        float yaw = mc.player.getYRot();

        if (options.keyUp.isDown()) {
            x -= Math.sin(Math.toRadians(yaw)) * speed;
            z += Math.cos(Math.toRadians(yaw)) * speed;
        }
        if (options.keyDown.isDown()) {
            x += Math.sin(Math.toRadians(yaw)) * speed;
            z -= Math.cos(Math.toRadians(yaw)) * speed;
        }
        if (options.keyLeft.isDown()) {
            x -= Math.sin(Math.toRadians(yaw - 90)) * speed;
            z += Math.cos(Math.toRadians(yaw - 90)) * speed;
        }
        if (options.keyRight.isDown()) {
            x -= Math.sin(Math.toRadians(yaw + 90)) * speed;
            z += Math.cos(Math.toRadians(yaw + 90)) * speed;
        }
        if (options.keyJump.isDown()) y += speed;
        if (options.keyShift.isDown()) y -= speed;

        if (x != 0 || y != 0 || z != 0) {
            mc.player.setDeltaMovement(new Vec3(x, y, z));
        } else {
            mc.player.setDeltaMovement(Vec3.ZERO);
        }
    }
}
