package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

public class MiddleClickFriend extends Module {

    private boolean wasPressed = false;

    public MiddleClickFriend() {
        super(Addon.CATEGORY, "middle-click-friend", "Middle click a player to add or remove them as a friend.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.level == null || mc.screen != null) return;

        long window = mc.getWindow().getWindow();
        boolean isPressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;

        if (isPressed && !wasPressed) {
            if (mc.crosshairPickEntity instanceof Player target && target != mc.player) {
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

        wasPressed = isPressed;
    }
}
