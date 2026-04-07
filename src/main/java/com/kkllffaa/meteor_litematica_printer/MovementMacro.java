package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.world.TickEvent;

import net.minecraft.client.Options;

import java.util.ArrayList;
import java.util.List;

public class MovementMacro extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> recordingSetting = sgGeneral.add(new BoolSetting.Builder()
        .name("recording")
        .defaultValue(false)
        .build()
    );

    private final Setting<Keybind> recordToggleKey = sgGeneral.add(new KeybindSetting.Builder()
        .name("record-toggle-key")
        .defaultValue(Keybind.none())
        .build()
    );

    private final Setting<Keybind> holdRecordKey = sgGeneral.add(new KeybindSetting.Builder()
        .name("hold-record-key")
        .defaultValue(Keybind.none())
        .build()
    );

    private final Setting<Boolean> useHoldMode = sgGeneral.add(new BoolSetting.Builder()
        .name("use-hold")
        .defaultValue(false)
        .build()
    );

    private final List<Frame> frames = new ArrayList<>();
    private int playbackIndex = 0;
    private boolean wasRecording = false;

    public MovementMacro() {
        super(Categories.Movement, "movement-macro", "Records and replays movement.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        boolean isRecording;

        if (useHoldMode.get()) {
            isRecording = holdRecordKey.get().isPressed();
        } else {
            if (recordToggleKey.get().isPressed()) {
                recordingSetting.set(!recordingSetting.get());
            }
            isRecording = recordingSetting.get();
        }

        if (isRecording) {
            recordFrame();
            wasRecording = true;
            return;
        }

        if (!isRecording && wasRecording) {
            playbackIndex = 0;
            wasRecording = false;
        }

        if (!frames.isEmpty() && playbackIndex < frames.size()) {
            playFrame(frames.get(playbackIndex));
            playbackIndex++;
        } else {
            resetKeys();
        }
    }

    private void recordFrame() {
        Options o = mc.options;

        frames.add(new Frame(
            o.keyForward.isPressed(),
            o.keyBack.isPressed(),
            o.keyLeft.isPressed(),
            o.keyRight.isPressed(),
            o.keyJump.isPressed(),
            o.keySneak.isPressed(),
            mc.player.isSprinting()
        ));
    }

    private void playFrame(Frame f) {
        Options o = mc.options;

        o.keyForward.setPressed(f.forward);
        o.keyBack.setPressed(f.backward);
        o.keyLeft.setPressed(f.left);
        o.keyRight.setPressed(f.right);

        o.keyJump.setPressed(f.jump);
        o.keySneak.setPressed(f.sneak);

        mc.player.setSprinting(f.sprint);
    }

    private void resetKeys() {
        Options o = mc.options;

        o.keyForward.setPressed(false);
        o.keyBack.setPressed(false);
        o.keyLeft.setPressed(false);
        o.keyRight.setPressed(false);

        o.keyJump.setPressed(false);
        o.keySneak.setPressed(false);

        if (mc.player != null) mc.player.setSprinting(false);
    }

    private static class Frame {
        boolean forward, backward, left, right;
        boolean jump, sneak, sprint;

        Frame(boolean f, boolean b, boolean l, boolean r, boolean j, boolean sn, boolean sp) {
            forward = f;
            backward = b;
            left = l;
            right = r;
            jump = j;
            sneak = sn;
            sprint = sp;
        }
    }
}
