package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class MovementMacro extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSlots  = settings.createGroup("Saved Macros");

    // ── General ──────────────────────────────────────────────────────────────

    private final Setting<Boolean> recordingSetting = sgGeneral.add(new BoolSetting.Builder()
        .name("recording")
        .description("Turn on to record, turn off to stop and save")
        .defaultValue(false)
        .build()
    );

    private final Setting<Keybind> recordKey = sgGeneral.add(new KeybindSetting.Builder()
        .name("record-keybind")
        .description("Toggles recording on/off.")
        .defaultValue(Keybind.none())
        .build()
    );

    private final Setting<Boolean> loopSetting = sgGeneral.add(new BoolSetting.Builder()
        .name("loop")
        .description("Loop the macro continuously")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> repeatsSetting = sgGeneral.add(new IntSetting.Builder()
        .name("repeats")
        .description("How many times to repeat if loop is off")
        .defaultValue(1)
        .min(1)
        .sliderMax(100)
        .build()
    );

    // Which slot to save into after recording finishes (1-10)
    private final Setting<Integer> saveSlotSetting = sgGeneral.add(new IntSetting.Builder()
        .name("save-to-slot")
        .description("Which slot (1-10) to save the next recording into")
        .defaultValue(1)
        .min(1)
        .sliderMax(10)
        .build()
    );

    // ── Per-slot settings (name + enable toggle) ──────────────────────────────

    private static final int MAX_SLOTS = 10;

    // Names
    private final Setting<String>[]  slotNames   = new Setting[MAX_SLOTS];
    // Enable toggles (drives playback)
    private final Setting<Boolean>[] slotEnabled = new Setting[MAX_SLOTS];

    // ── Runtime state ─────────────────────────────────────────────────────────

    private static class Frame {
        boolean forward, backward, left, right, jump, sneak, sprint;
        float yaw, pitch;

        Frame(boolean forward, boolean backward, boolean left, boolean right,
              boolean jump, boolean sneak, boolean sprint, float yaw, float pitch) {
            this.forward  = forward;
            this.backward = backward;
            this.left     = left;
            this.right    = right;
            this.jump     = jump;
            this.sneak    = sneak;
            this.sprint   = sprint;
            this.yaw      = yaw;
            this.pitch    = pitch;
        }
    }

    // 10 independent save slots
    @SuppressWarnings("unchecked")
    private final List<Frame>[] slots = new List[MAX_SLOTS];

    // Live recording buffer
    private final List<Frame> recording = new ArrayList<>();

    private boolean wasRecording  = false;
    private boolean lastKeyStatus = false;

    // Playback state — which slot is currently playing (-1 = none)
    private int     activeSlot   = -1;
    private int     playIndex    = 0;
    private int     repeatCount  = 0;

    @SuppressWarnings("unchecked")
    public MovementMacro() {
        super(Addon.CATEGORY, "movement-macro", "Record movements, save to named slots, and toggle them.");

        for (int i = 0; i < MAX_SLOTS; i++) {
            slots[i] = new ArrayList<>();

            final int slot = i; // effectively final for lambdas

            slotNames[i] = sgSlots.add(new StringSetting.Builder()
                .name("slot-" + (i + 1) + "-name")
                .description("Name for macro slot " + (i + 1))
                .defaultValue("Macro " + (i + 1))
                .build()
            );

            slotEnabled[i] = sgSlots.add(new BoolSetting.Builder()
                .name("slot-" + (i + 1) + "-play")
                .description("Toggle playback of slot " + (i + 1) + " on/off")
                .defaultValue(false)
                .onChanged(enabled -> onSlotToggled(slot, enabled))
                .build()
            );
        }
    }

    // ── Slot toggle handler ───────────────────────────────────────────────────

    private void onSlotToggled(int slot, boolean enabled) {
        if (mc.player == null) return;
        String name = slotNames[slot].get();

        if (enabled) {
            if (slots[slot].isEmpty()) {
                slotEnabled[slot].set(false);
                mc.player.displayClientMessage(
                    Component.literal("§c[Macro] §fSlot §e" + (slot + 1) + " §f(\"" + name + "\") is empty!"), false
                );
                return;
            }

            // Stop any currently playing slot first
            if (activeSlot != -1 && activeSlot != slot) {
                slotEnabled[activeSlot].set(false);
                mc.player.displayClientMessage(
                    Component.literal("§c[Macro] §fStopped §e\"" + slotNames[activeSlot].get() + "\""), false
                );
            }

            activeSlot  = slot;
            playIndex   = 0;
            repeatCount = 0;

            mc.player.displayClientMessage(
                Component.literal("§a[Macro] §fPlaying §e\"" + name + "\" §f(" + slots[slot].size() + " ticks)"), false
            );
        } else {
            if (activeSlot == slot) {
                activeSlot = -1;
                restoreInputs();
                mc.player.displayClientMessage(
                    Component.literal("§c[Macro] §fStopped §e\"" + name + "\""), false
                );
            }
        }
    }

    // ── Module lifecycle ──────────────────────────────────────────────────────

    @Override
    public void onActivate() {
        activeSlot    = -1;
        playIndex     = 0;
        repeatCount   = 0;
        wasRecording  = false;
        lastKeyStatus = false;
        recording.clear();

        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.literal("§e[Macro] §fUse §aRecording §fto capture, then enable a slot to play back."), false
            );
        }
    }

    @Override
    public void onDeactivate() {
        activeSlot = -1;
        recordingSetting.set(false);
        for (int i = 0; i < MAX_SLOTS; i++) slotEnabled[i].set(false);
        restoreInputs();
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        // Keybind toggle
        boolean isKeyPressed = recordKey.get().isPressed();
        if (isKeyPressed && !lastKeyStatus) {
            recordingSetting.set(!recordingSetting.get());
        }
        lastKeyStatus = isKeyPressed;

        boolean isRecording = recordingSetting.get();

        // Started recording
        if (isRecording && !wasRecording) {
            recording.clear();
            // Pause any active playback while recording
            if (activeSlot != -1) {
                restoreInputs();
            }
            wasRecording = true;
            mc.player.displayClientMessage(
                Component.literal("§c[Macro] §fRecording started! Will save to slot §e"
                    + saveSlotSetting.get() + "§f."), false
            );
        }

        // Stopped recording → save
        if (!isRecording && wasRecording) {
            wasRecording = false;

            if (recording.isEmpty()) {
                mc.player.displayClientMessage(
                    Component.literal("§c[Macro] §fNothing recorded!"), false
                );
                return;
            }

            int targetSlot = saveSlotSetting.get() - 1; // 0-indexed
            slots[targetSlot].clear();
            slots[targetSlot].addAll(recording);
            recording.clear();

            String name = slotNames[targetSlot].get();
            mc.player.displayClientMessage(
                Component.literal("§a[Macro] §fSaved §e" + slots[targetSlot].size()
                    + " §fticks to slot §e" + (targetSlot + 1) + " §f(\"" + name + "\"). Enable its toggle to play!"), false
            );
            return;
        }

        // Active recording — capture frame
        if (isRecording) {
            var options = mc.options;
            recording.add(new Frame(
                options.keyUp.isDown(),
                options.keyDown.isDown(),
                options.keyLeft.isDown(),
                options.keyRight.isDown(),
                options.keyJump.isDown(),
                options.keyShift.isDown(),
                options.keySprint.isDown(),
                mc.player.getYRot(),
                mc.player.getXRot()
            ));
            return;
        }

        // Playback
        if (activeSlot == -1 || slots[activeSlot].isEmpty()) return;

        List<Frame> active = slots[activeSlot];
        applyFrame(mc.player, active.get(playIndex));
        playIndex++;

        if (playIndex >= active.size()) {
            playIndex = 0;
            repeatCount++;

            if (!loopSetting.get() && repeatCount >= repeatsSetting.get()) {
                restoreInputs();
                String name = slotNames[activeSlot].get();
                mc.player.displayClientMessage(
                    Component.literal("§a[Macro] §fPlayback of §e\"" + name + "\" §ffinished!"), false
                );
                slotEnabled[activeSlot].set(false); // auto-disable toggle
                activeSlot = -1;
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void applyFrame(LocalPlayer player, Frame frame) {
        var options = mc.options;
        options.keyUp.setDown(frame.forward);
        options.keyDown.setDown(frame.backward);
        options.keyLeft.setDown(frame.left);
        options.keyRight.setDown(frame.right);
        options.keyJump.setDown(frame.jump);
        options.keyShift.setDown(frame.sneak);
        options.keySprint.setDown(frame.sprint);
        player.setYRot(frame.yaw);
        player.setXRot(frame.pitch);
    }

    private void restoreInputs() {
        if (mc.options == null) return;
        var options = mc.options;
        options.keyUp.setDown(false);
        options.keyDown.setDown(false);
        options.keyLeft.setDown(false);
        options.keyRight.setDown(false);
        options.keyJump.setDown(false);
        options.keyShift.setDown(false);
        options.keySprint.setDown(false);
    }
}
