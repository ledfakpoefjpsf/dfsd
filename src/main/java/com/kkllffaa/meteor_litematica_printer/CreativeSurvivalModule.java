package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.ModuleType;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

public class CreativeSurvivalModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // ----- Checkboxes -----
    private final Setting<Boolean> flyEnabled = sgGeneral.add(new BoolSetting.Builder()
            .name("fly")
            .description("Enable flying")
            .defaultValue(true)
            .build());

    private final Setting<Boolean> vanishEnabled = sgGeneral.add(new BoolSetting.Builder()
            .name("vanish")
            .description("Enable vanish mode")
            .defaultValue(false)
            .build());

    private final Setting<Boolean> godmodeEnabled = sgGeneral.add(new BoolSetting.Builder()
            .name("godmode")
            .description("Enable godmode")
            .defaultValue(false)
            .build());

    private final Setting<Boolean> instaBreakEnabled = sgGeneral.add(new BoolSetting.Builder()
            .name("insta-break")
            .description("Break blocks instantly")
            .defaultValue(true)
            .build());

    private final Setting<Boolean> spawnItemsEnabled = sgGeneral.add(new BoolSetting.Builder()
            .name("spawn-items")
            .description("Spawn items automatically")
            .defaultValue(false)
            .build());

    private final Setting<Boolean> overEnchantEnabled = sgGeneral.add(new BoolSetting.Builder()
            .name("over-enchant")
            .description("Over-enchant items in hand")
            .defaultValue(false)
            .build());

    // ----- Optional Keybinds -----
    private final Setting<KeybindSetting> spawnItemKey = sgGeneral.add(new KeybindSetting.Builder()
            .name("spawn-item-key")
            .description("Key to spawn a stack of items")
            .defaultValue(new KeybindSetting(InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, false))
            .build());

    private final Setting<KeybindSetting> enchantKey = sgGeneral.add(new KeybindSetting.Builder()
            .name("enchant-key")
            .description("Key to over-enchant the item in hand")
            .defaultValue(new KeybindSetting(InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_E, false))
            .build());

    public CreativeSurvivalModule() {
        super(ModuleType.Player, "creative-survival", "Ultimate creative powers in survival with GUI toggles");
    }

    @Override
    public void onTick() {
        // Fly
        if (flyEnabled.get()) {
            mc.player.getAbilities().flying = true;
            mc.player.getAbilities().allowFlying = true;
        } else {
            mc.player.getAbilities().flying = false;
        }

        // Instant break
        if (instaBreakEnabled.get()) PlayerUtils.breakBlockInstant();

        // Spawn items
        if (spawnItemsEnabled.get() || spawnItemKey.get().isPressed()) {
            mc.player.getInventory().insertStack(Items.DIAMOND.getDefaultStack());
        }

        // Vanish
        mc.player.setInvisible(vanishEnabled.get());

        // Godmode
        if (godmodeEnabled.get()) {
            mc.player.setHealth(mc.player.getMaxHealth());
            mc.player.getHungerManager().setFoodLevel(20);
        }

        // Over-enchant
        if (overEnchantEnabled.get() || enchantKey.get().wasPressed()) {
            ItemStack stack = mc.player.getMainHandStack();
            if (!stack.isEmpty()) enchantItem(stack, 3000); // Sharpness 3000 example
        }
    }

    private void enchantItem(ItemStack stack, int level) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtCompound enchants = new NbtCompound();
        enchants.putInt("minecraft:sharpness", level);
        nbt.put("Enchantments", enchants);
        stack.setNbt(nbt);
    }

    @Override
    public void onDeactivate() {
        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().allowFlying = false;
        mc.player.setInvisible(false);
    }
}
