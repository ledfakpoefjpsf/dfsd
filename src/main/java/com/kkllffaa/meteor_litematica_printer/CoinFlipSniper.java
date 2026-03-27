/*
 * CoinFlip Sniper — full feature set in this file:
 * - Snipes /cf broadcasts in range (min/max amount), optional GC mode.
 * - random-delay: random 3–10s before /cf, or off for instant.
 * - loss-regex / win-regex: 3 losses in a row (wins reset streak) disables the module.
 * - GUI: finds player head + Confirm from tooltips.
 *
 * Register once in your Addon onInitialize():
 *   Modules.get().add(new CoinFlipSniper());
 */
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.inventory.ClickType;
        .name("min-amount")
        .description("Minimum amount to snipe e.g. $10K, $1M")
        .description("Minimum amount to snipe (e.g. $10K, $1M).")
        .defaultValue("$10K")
        .name("max-amount")
        .description("Maximum amount to snipe e.g. $1M, $10M")
        .description("Maximum amount to snipe (e.g. $1M, $10M).")
        .defaultValue("$1M")
        .name("gc-mode")
        .description("Snipe GC coinflips instead of $ coinflips")
        .description("Snipe GC coinflips instead of $ coinflips.")
        .defaultValue(false)
        .name("random-delay")
        .description("Wait a random 3-10 seconds before running /cf. Turn off for no delay.")
        .description("Random 3–10s delay before /cf. Off = no delay.")
        .defaultValue(true)
    );
    private final Setting<Integer> maxLosses = sgGeneral.add(new IntSetting.Builder()
        .name("max-losses")
        .description("Disable the module after this many losses in a row (wins reset the streak).")
        .defaultValue(3)
        .min(1)
        .sliderMax(10)
        .build()
    );
        .name("loss-regex")
        .description("Case-insensitive regex for a coinflip loss in chat. Counts toward auto-disable.")
        .description("Regex for a loss line in chat (case-insensitive). Tune for your server.")
        .defaultValue("(?i).*(you lost|lost the coin|lost the flip).*")
        .name("win-regex")
        .description("Case-insensitive regex for a win; resets the loss streak. Empty = never reset from chat.")
        .description("Regex for a win; resets the loss streak. Empty = never reset from chat.")
        .defaultValue("(?i).*(you won|won the coin|won the flip).*")
    );
    private final Setting<Boolean> feedback = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-feedback")
        .description("Print short status lines in chat when sniping / clicking / disabled.")
        .defaultValue(true)
        .build()
    );
    }
    @Override
    public void onDeactivate() {
        pendingTarget = null;
        waitingForConfirm = false;
    }
            String win = winRegex.get();
            if (!win.isEmpty()) {
                try {
                    if (Pattern.compile(win, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(msg).find()) {
                        lossStreak = 0;
                    }
                } catch (Exception ignored) {
                }
            if (!win.isEmpty() && matchesRegex(win, msg)) {
                lossStreak = 0;
            }
            try {
                if (Pattern.compile(lossRegex.get(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(msg).find()) {
                    lossStreak++;
                    if (lossStreak >= 3) {
                        lossStreak = 0;
                        mc.execute(() -> {
                            if (mc.player != null) {
                                mc.player.displayClientMessage(
                                    Component.literal("§cCoinflip Sniper disabled after 3 losses."),
                                    false
                                );
                            }
                            disable();
                        });
                    }
                    return;
                }
            } catch (Exception ignored) {
            if (matchesRegex(lossRegex.get(), msg)) {
                lossStreak++;
                int cap = maxLosses.get();
                if (lossStreak >= cap) {
                    lossStreak = 0;
                    mc.execute(() -> {
                        msgFeedback("§cCoinflip Sniper disabled after " + cap + " losses.");
                        disable();
                    });
                }
                return;
            }
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(
                "§aSniping §e" + playerName + "§a's flip for §e" + amountStr
            ), false);
        }
        msgFeedback("§aSniping §e" + playerName + "§a for §e" + amountStr);
                }
                mc.execute(() -> mc.getConnection().sendCommand("cf"));
                mc.execute(() -> {
                    if (mc.getConnection() != null) {
                        mc.getConnection().sendCommand("cf");
                    }
                });
            } catch (InterruptedException e) {
        String title = screen.getTitle().getString();
        // Debug
        if (mc.player != null) {
            mc.player.displayClientMessage(
                Component.literal("§eTicking GUI: §f" + title), false
            );
        }
        // Looking for target player head
        if (pendingTarget != null && !waitingForConfirm) {
            var slots = screen.getMenu().slots;
            for (int i = 0; i < slots.size(); i++) {
                ItemStack stack = slots.get(i).getItem();
                if (stack.isEmpty()) continue;
                List<Component> tooltip = stack.getTooltipLines(
                    net.minecraft.world.item.Item.TooltipContext.EMPTY, null,
                    net.minecraft.world.item.TooltipFlag.NORMAL
                );
                for (Component line : tooltip) {
                    String lineText = line.getString();
                    // Debug every tooltip
                    if (mc.player != null) {
                        mc.player.displayClientMessage(
                            Component.literal("§7Slot " + i + ": §f" + lineText), false
                        );
                    }
                    if (lineText.contains(pendingTarget)) {
                        mc.gameMode.handleInventoryMouseClick(
                            screen.getMenu().containerId, i, 0,
                            ClickType.PICKUP,
                            mc.player
                        );
                        waitingForConfirm = true;
                        if (mc.player != null) {
                            mc.player.displayClientMessage(Component.literal(
                                "§aClicked §e" + pendingTarget + "§a's head!"
                            ), false);
                        }
                        return;
                    }
                }
            }
        }
        // Looking for confirm button
        if (waitingForConfirm) {
            var slots = screen.getMenu().slots;
            for (int i = 0; i < slots.size(); i++) {
                ItemStack stack = slots.get(i).getItem();
                if (stack.isEmpty()) continue;
            Integer slot = findSlotWithTooltipContaining(screen, pendingTarget);
            if (slot != null) {
                clickSlot(screen, slot);
                waitingForConfirm = true;
                msgFeedback("§aClicked §e" + pendingTarget + "§a.");
            }
            return;
        }
        if (waitingForConfirm) {
            Integer slot = findSlotWithTooltipContaining(screen, "Confirm");
            if (slot != null) {
                clickSlot(screen, slot);
                msgFeedback("§aCoinflip accepted.");
                pendingTarget = null;
                waitingForConfirm = false;
            }
        }
    }
    private void clickSlot(AbstractContainerScreen<?> screen, int slotIndex) {
        mc.gameMode.handleInventoryMouseClick(
            screen.getMenu().containerId,
            slotIndex,
            0,
            ClickType.PICKUP,
            mc.player
        );
    }
    private Integer findSlotWithTooltipContaining(AbstractContainerScreen<?> screen, String needle) {
        var slots = screen.getMenu().slots;
        boolean confirm = needle.equalsIgnoreCase("Confirm");
        for (int i = 0; i < slots.size(); i++) {
            ItemStack stack = slots.get(i).getItem();
            if (stack.isEmpty()) continue;
                List<Component> tooltip = stack.getTooltipLines(
                    net.minecraft.world.item.Item.TooltipContext.EMPTY, null,
                    net.minecraft.world.item.TooltipFlag.NORMAL
                );
            List<Component> lines = stack.getTooltipLines(
                Item.TooltipContext.EMPTY,
                null,
                TooltipFlag.NORMAL
            );
                for (Component line : tooltip) {
                    String lineText = line.getString();
                    if (lineText.equalsIgnoreCase("Confirm")) {
                        mc.gameMode.handleInventoryMouseClick(
                            screen.getMenu().containerId, i, 0,
                            ClickType.PICKUP,
                            mc.player
                        );
                        if (mc.player != null) {
                            mc.player.displayClientMessage(Component.literal(
                                "§aCoinflip accepted!"
                            ), false);
                        }
                        pendingTarget = null;
                        waitingForConfirm = false;
                        return;
                    }
                }
            }
        }
            for (Component line : lines) {
                String text = line.getString();
                if (confirm) {
                    if (text.equalsIgnoreCase("Confirm")) return i;
                } else {
                    if (text.contains(needle)) return i;
                }
            }
        }
        return null;
    }
    private static boolean matchesRegex(String regex, String msg) {
        try {
            return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(msg).find();
        } catch (Exception e) {
            return false;
        }
    }
    private void msgFeedback(String text) {
        if (!feedback.get() || mc.player == null) return;
        mc.player.displayClientMessage(Component.literal(text), false);
    }
        try {
            if (s.endsWith("B")) return (long)(Double.parseDouble(s.replace("B", "")) * 1_000_000_000);
            if (s.endsWith("M")) return (long)(Double.parseDouble(s.replace("M", "")) * 1_000_000);
            if (s.endsWith("K")) return (long)(Double.parseDouble(s.replace("K", "")) * 1_000);
            if (s.endsWith("B")) return (long) (Double.parseDouble(s.replace("B", "")) * 1_000_000_000);
            if (s.endsWith("M")) return (long) (Double.parseDouble(s.replace("M", "")) * 1_000_000);
            if (s.endsWith("K")) return (long) (Double.parseDouble(s.replace("K", "")) * 1_000);
            return Long.parseLong(s);
