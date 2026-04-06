package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.mouse.MouseButtonEvent;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class MiddleClickFriend extends Module {

    public MiddleClickFriend() {
        super(Addon.CATEGORY, "middle-click-friend", "Middle click a player to add or remove them as a friend.");
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action != KeyAction.Press) return;
        if (event.button != 2) return; // 2 = middle mouse button
        if (mc.player == null || mc.level == null) return;
        if (mc.screen != null) return; // don't trigger in GUIs

        // Find the player being looked at
        if (mc.crosshairPickEntity instanceof Player target) {
            if (target == mc.player) return;

            var friend = Friends.get().get(target);
            if (friend == null) {
                Friends.get().add(target);
                mc.player.displayClientMessage(
                    Component.literal("§aAdded §e" + target.getName().getString() + " §aas a friend!"), false
                );
            } else {
                Friends.get().remove(friend);
                mc.player.displayClientMessage(
                    Component.literal("§cRemoved §e" + target.getName().getString() + " §cfrom friends!"), false
                );
            }
        }
    }
}
