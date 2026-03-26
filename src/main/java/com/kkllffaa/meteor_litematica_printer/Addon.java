package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.events.render.RenderTooltipEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

public class CreativeSurvivalModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Item> itemToSpawn = sgGeneral.add(new ItemSetting.Builder()
        .name("item")
        .description("Item to spawn.")
        .defaultValue(Items.DIAMOND)
        .build()
    );

    private final Setting<KeyBinding> spawnItemKey = sgGeneral.add(new KeyBindingSetting.Builder()
        .name("spawn-key")
        .description("Key to spawn the item.")
        .defaultValue(new KeyBinding("Spawn Item", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "CreativeSurvival"))
        .build()
    );

    private final Setting<KeyBinding> enchantKey = sgGeneral.add(new KeyBindingSetting.Builder()
        .name("enchant-key")
        .description("Key to enchant held item.")
        .defaultValue(new KeyBinding("Enchant Item", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_E, "CreativeSurvival"))
        .build()
    );

    public CreativeSurvivalModule() {
        super(meteordevelopment.meteorclient.systems.modules.Category.World, "creative-survival", "Spawn items and enchant in survival like creative.");
    }

    @Override
    public void onTick() {
        ClientPlayerEntity player = mc.player;
        if (player == null) return;

        // Spawn item
        if (spawnItemKey.get().wasPressed()) {
            ItemStack stack = new ItemStack(itemToSpawn.get(), 64);
            if (player.getInventory().insertStack(stack)) {
                ChatUtils.info("Spawned 64 " + itemToSpawn.get().getName().getString());
            }
        }

        // Enchant held item
        if (enchantKey.get().wasPressed()) {
            ItemStack stack = player.getMainHandStack();
            if (!stack.isEmpty()) {
                EnchantmentHelper.setLevel(EnchantmentHelper.get(stack).keySet().iterator().next(), stack, 5);
                ChatUtils.info("Enchanted " + stack.getName().getString());
            }
        }
    }
}

// ----------------------------- Spectator Module -----------------------------
package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.client.option.KeyBinding;

public class SpectatorModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public SpectatorModule() {
        super(meteordevelopment.meteorclient.systems.modules.Category.Render, "spectator-module", "Show tooltip on armor in spectator mode.");
    }

    @Override
    public void onRenderTooltip(RenderTooltipEvent event) {
        GuiGraphics graphics = event.graphics;
        ClientPlayerEntity player = mc.player;
        if (player == null) return;

        for (ItemStack armor : player.getInventory().armor) {
            if (!armor.isEmpty()) {
                graphics.drawItem(armor, event.x, event.y);
                graphics.renderTooltip(graphics.getFont(), armor.getTooltipLines(player, null), event.x, event.y, null);
            }
        }
    }
}
