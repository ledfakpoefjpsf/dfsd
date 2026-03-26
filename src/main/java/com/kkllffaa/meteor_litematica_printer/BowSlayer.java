package com.kkllffaa.meteor_litematica_printer.modules;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.phys.Vec3;

import com.kkllffaa.meteor_litematica_printer.Addon;

public class BowSlayer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> smoothness = sgGeneral.add(new DoubleSetting.Builder()
        .name("smoothness")
        .defaultValue(5)
        .min(1)
        .max(20)
        .build()
    );

    private Vec3 lastPos = null;
    private Vec3 lastVelocity = Vec3.ZERO;

    public BowSlayer() {
        super(Addon.CATEGORY, "bow-slayer", "Bow aiming assist with prediction.");
    }

    @Override
    public void onActivate() {
        lastPos = null;
        lastVelocity = Vec3.ZERO;
    }

    private boolean isAimingBow() {
        return mc.player != null &&
               mc.player.isUsingItem() &&
               mc.player.getUseItem().getItem() instanceof BowItem;
    }

    private float lerp(float a, float b, float f) {
        return a + (b - a) * f;
    }

    private float getBowPower() {
        int useTime = mc.player.getUseItemRemainingTicks();
        float charge = (72000 - useTime) / 20.0f;
        charge = (charge * charge + charge * 2.0f) / 3.0f;
        return Math.min(charge, 1.0f);
    }

    private Vec3 predict(Player target, double time) {
        Vec3 velocity = target.getDeltaMovement();

        if (velocity.length() < 0.01) {
            velocity = lastVelocity;
        }

        return target.position().add(velocity.scale(time));
    }

    private void aimAt(Vec3 pos) {
        Vec3 eyes = mc.player.getEyePosition();
        Vec3 diff = pos.subtract(eyes);

        double distXZ = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        float yaw = (float)(Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90F);
        float pitch = (float)(-Math.toDegrees(Math.atan2(diff.y, distXZ)));

        float smooth = smoothness.get().floatValue();

        mc.player.setYRot(lerp(mc.player.getYRot(), yaw, 1f / smooth));
        mc.player.setXRot(lerp(mc.player.getXRot(), pitch, 1f / smooth));
    }

    private Player getTarget() {
        Player closest = null;
        double bestDist = Double.MAX_VALUE;

        for (Player p : mc.level.players()) {
            if (p == mc.player) continue;

            double dist = mc.player.distanceTo(p);
            if (dist < bestDist) {
                bestDist = dist;
                closest = p;
            }
        }

        return closest;
    }

    @Override
    public void onTick() {
        if (!isAimingBow()) return;

        Player target = getTarget();
        if (target == null) return;

        // track velocity
        if (lastPos != null) {
            lastVelocity = target.position().subtract(lastPos);
        }
        lastPos = target.position();

        double distance = mc.player.distanceTo(target);
        double speed = getBowPower() * 3.0;

        if (speed < 0.1) return;

        double time = distance / speed;

        Vec3 predicted = predict(target, time);

        // gravity compensation
        predicted = predicted.add(0, distance * 0.05, 0);

        aimAt(predicted);
    }
}
