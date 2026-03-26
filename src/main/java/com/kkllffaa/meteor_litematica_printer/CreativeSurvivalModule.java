package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class CreativeSurvivalModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Item> itemToSpawn = sgGeneral.add(new ItemSetting.Builder()
        .name("item")
        .description("Item to spawn.")
        .defaultValue(Items.DIAMOND)
        .build()
    );

    private final Setting<KeyMapping> spawnItemKey = sgGeneral.add(new KeyBindingSetting.Builder()
        .name("spawn-key")
        .description("Key to spawn the item.")
        .defaultValue(new KeyMapping("Spawn Item", GLFW.GLFW_KEY_K, "CreativeSurvival"))
        .build()
    );

    private final Setting<KeyMapping> enchantKey = sgGeneral.add(new KeyBindingSetting.Builder()
        .name("enchant-key")
        .description("Key to enchant held item.")
        .defaultValue(new KeyMapping("Enchant Item", GLFW.GLFW_KEY_E, "CreativeSurvival"))
        .build()
    );

    public CreativeSurvivalModule() {
        super(meteordevelopment.meteorclient.systems.modules.Category.World, "creative-survival", "Spawn items and enchant in survival like creative.");
    }

    @Override
    public void onTick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        // Spawn item
        if (spawnItemKey.get().isDown()) {
            ItemStack stack = new ItemStack(itemToSpawn.get(), 64);
            if (player.getInventory().add(stack)) {
                ChatUtils.info("Spawned 64 " + stack.getHoverName().getString());
            }
        }

        // Enchant held item
        if (enchantKey.get().isDown()) {
            ItemStack stack = player.getMainHandItem();
            if (!stack.isEmpty()) {
                var firstEnchant = EnchantmentHelper.getEnchantments(stack).keySet().iterator().next();
                EnchantmentHelper.setEnchantments(Map.of(firstEnchant, 5), stack);
                ChatUtils.info("Enchanted " + stack.getHoverName().getString());
            }
        }
    }
}
