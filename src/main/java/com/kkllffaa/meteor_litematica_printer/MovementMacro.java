package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class MovementMacro extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> recordingSetting = sgGeneral.add(new BoolSetting.Builder()
        .name("recording")
        .description("Turn on to record, turn off to stop and save.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Keybind> recordKey = sgGeneral.add(new KeybindSetting.Builder()
        .name("record-keybind")
        .description("Key to toggle recording on and off.")
        .defaultValue(Keybind.none())
        .build()
    );

    private final Setting<Boolean> loopSetting = sgGeneral.add(new BoolSetting.Builder()
        .name("loop")
        .description("Loop the macro continuously.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> repeatsSetting = sgGeneral.add(new IntSetting.Builder()
        .name("repeats")
        .description("How many times to repeat if loop is off.")
        .defaultValue(1)
        .min(1)
        .sliderMax(100)
        .build()
    );

    private static class Frame {
        boolean forward, backward, left, right, jump, sneak, sprint;
        float yaw, pitch;

        Frame(boolean forward, boolean backward, boolean left, boolean right,
              boolean jump, boolean sneak, boolean sprint, float yaw, float pitch) {
            this.forward = forward;
            this.backward = backward;
            this.left = left;
            this.right = right;
            this.jump = jump;
            this.sneak = sneak;
            this.sprint = sprint;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    private final List<Frame> recorded = new ArrayList<>();
    private boolean wasRecording = false;
    private boolean playing = false;
    private int playIndex = 0;
    private int repeatCount = 0;
    private boolean lastKeyStatus = false;

    public MovementMacro() {
        // Using standard Meteor Category to avoid "Addon.CATEGORY" symbol errors
        super(Categories.Movement, "movement-macro", "Record your movements and play them back on repeat.");
    }

    @Override
    public void onActivate() {
        playing = false;
        playIndex = 0;
        repeatCount = 0;
        wasRecording = false;
        lastKeyStatus = false;
    }

    @Override
    public void onDeactivate() {
        playing = false;
        recordingSetting.set(false);
        restoreInputs();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        // Keybind toggle logic
        boolean isKeyPressed = recordKey.get().isPressed();
        if (isKeyPressed && !lastKeyStatus) {
            recordingSetting.set(!recordingSetting.get());
        }
        lastKeyStatus = isKeyPressed;

        boolean isRecording = recordingSetting.get();

        // Start logic
        if (isRecording && !wasRecording) {
            recorded.clear();
            playing = false;
            wasRecording = true;
            mc.player.displayClientMessage(Component.literal("§c[Macro] §fRecording started!"), true);
        }

        // Stop logic
        if (!isRecording && wasRecording) {
            wasRecording = false;
            if (recorded.isEmpty()) {
                mc.player.displayClientMessage(Component.literal("§c[Macro] §fNothing recorded!"), true);
            } else {
                mc.player.displayClientMessage(Component.literal("§a[Macro] §fRecorded §e" + recorded.size() + " §fticks! Playback starting..."), true);
                playing = true;
                playIndex = 0;
                repeatCount = 0;
            }
        }

        // Record
        if (isRecording) {
            var o = mc.options;
            recorded.add(new Frame(
                o.keyForward.isDown(),
                o.keyBack.isDown(),
                o.keyLeft.isDown(),
                o.keyRight.isDown(),
                o.keyJump.isDown(),
                o.keySneak.isDown(),
                o.keySprint.isDown(),
                mc.player.getYRot(),
                mc.player.getXRot()
            ));
            return;
        }

        // Playback
        if (!playing || recorded.isEmpty()) return;

        Frame frame = recorded.get(playIndex);
        applyFrame(mc.player, frame);
        playIndex++;

        if (playIndex >= recorded.size()) {
            playIndex = 0;
            repeatCount++;

            if (!loopSetting.get() && repeatCount >= repeatsSetting.get()) {
                playing = false;
                restoreInputs();
                mc.player.displayClientMessage(Component.literal("§a[Macro] §fPlayback finished!"), true);
            }
        }
    }

    private void applyFrame(LocalPlayer player, Frame frame) {
        var input = player.input;
        if (input == null) return;

        // Updated for your specific environment mappings
        input.pressingForward = frame.forward;
        input.pressingBackward = frame.backward;
        input.pressingLeft = frame.left;
        input.pressingRight = frame.right;
        input.jumping = frame.jump;
        input.shiftKeyDown = frame.sneak;

        // Necessary for 1.21.x movement physics
        input.forwardImpulse = frame.forward ? 1.0F : (frame.backward ? -1.0F : 0.0F);
        input.leftImpulse = frame.left ? 1.0F : (frame.right ? -1.0F : 0.0F);
        
        player.setSprinting(frame.sprint);
        player.setYRot(frame.yaw);
        player.setXRot(frame.pitch);
    }

    private void restoreInputs() {
        if (mc.player == null || mc.player.input == null) return;
        var input = mc.player.input;

        input.pressingForward = false;
        input.pressingBackward = false;
        input.pressingLeft = false;
        input.pressingRight = false;
        input.jumping = false;
        input.shiftKeyDown = false;
        input.forwardImpulse = 0.0F;
        input.leftImpulse = 0.0F;
    }
}
