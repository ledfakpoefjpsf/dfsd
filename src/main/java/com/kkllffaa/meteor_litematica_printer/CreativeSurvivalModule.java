package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBind;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.registry.Registries;
import org.lwjgl.glfw.GLFW;

public class CreativeSurvivalModule extends Module {

    // ---------------- SETTINGS ----------------
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> spawnItemsEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("spawn-items")
        .description("Enables spawning items")
        .defaultValue(true)
        .build()
    );

    private final Setting<Item> itemToSpawn = sgGeneral.add(new ItemSetting.Builder()
        .name("item-to-spawn")
        .description("Item to spawn")
        .defaultValue(Items.DIAMOND)
        .build()
    );

    private final Setting<KeyBind> spawnItemKey = sgGeneral.add(new KeyBindSetting.Builder()
        .name("spawn-item-key")
        .description("Key to spawn item")
        .defaultValue(new KeyBind(InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, false))
        .build()
    );

    private final Setting<Boolean> overEnchantEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("over-enchant")
        .description("Allows over-enchanting items")
        .defaultValue(true)
        .build()
    );

    private final Setting<KeyBind> enchantKey = sgGeneral.add(new KeyBindSetting.Builder()
        .name("enchant-key")
        .description("Key to over-enchant item in hand")
        .defaultValue(new KeyBind(InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_E, false))
        .build()
    );

    private final Setting<Boolean> vanishEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("vanish")
        .description("Makes you invisible to others")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> godModeEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("godmode")
        .description("No damage & instant health regen")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> flyEnabled = sgGeneral.add(new BoolSetting.Builder()
        .name("fly")
        .description("Allows flying")
        .defaultValue(true)
        .build()
    );

    // ---------------- Constructor ----------------
    public CreativeSurvivalModule() {
        super(Categories.Player, "creative-survival", "Gives creative powers in survival.");
    }

    // ---------------- Event Handling ----------------
    @Override
    public void tick() {
        ClientPlayerEntity player = mc.player;
        if (player == null) return;

        // Godmode
        if (godModeEnabled.get()) {
            player.setHealth(player.getMaxHealth());
            player.getHungerManager().setFoodLevel(20);
        }

        // Fly
        if (flyEnabled.get()) {
            player.getAbilities().allowFlying = true;
            player.getAbilities().flying = true;
        } else {
            player.getAbilities().allowFlying = false;
        }

        // Spawn item
        if (spawnItemsEnabled.get() || spawnItemKey.get().isPressed()) {
            ItemStack stack = new ItemStack(itemToSpawn.get(), 64);
            player.getInventory().insertStack(stack);
        }

        // Over-enchant
        if (overEnchantEnabled.get() || enchantKey.get().wasPressed()) {
            ItemStack stack = player.getMainHandStack();
            if (!stack.isEmpty()) {
                CompoundTag tag = stack.getOrCreateTag();
                CompoundTag ench = new CompoundTag();
                // Example over-enchant: Sharpness 3000
                ench.putInt("minecraft:sharpness", 3000);
                tag.put("Enchantments", ench);
            }
        }

        // Vanish (client-side only)
        if (vanishEnabled.get()) {
            player.setInvisible(true);
        } else {
            player.setInvisible(false);
        }
    }
}
