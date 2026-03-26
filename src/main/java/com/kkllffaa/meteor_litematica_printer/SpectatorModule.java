package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.render.RenderTooltipEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

public class SpectatorModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public SpectatorModule() {
        super(meteordevelopment.meteorclient.systems.modules.Category.Render, "spectator-module", "Show tooltip on armor in spectator mode.");
    }

    @EventHandler
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
