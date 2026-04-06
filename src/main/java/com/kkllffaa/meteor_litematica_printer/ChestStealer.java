package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public class ChestStealer extends Module {

    private String lastTitle = "";
    private boolean done = false;

    public ChestStealer() {
        super(Addon.CATEGORY, "chest-stealer", "Instantly takes all items from a chest when opened.");
    }

    @Override
    public void onActivate() {
        lastTitle = "";
        done = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) {
            lastTitle = "";
            done = false;
            return;
        }

        String title = screen.getTitle().getString();
        if (!title.equals(lastTitle)) {
            lastTitle = title;
            done = false;
        }

        if (done) return;
        done = true;

        var slots = screen.getMenu().slots;
        int chestSize = slots.size() - 36;

        for (int i = 0; i < chestSize; i++) {
            ItemStack stack = slots.get(i).getItem();
            if (!stack.isEmpty()) {
                mc.gameMode.handleInventoryMouseClick(
                    screen.getMenu().containerId,
                    i, 0,
                    ClickType.QUICK_MOVE,
                    mc.player
                );
            }
        }

        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.literal("§aChest cleared!"), false
            );
        }
    }
}
