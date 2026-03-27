@EventHandler
private void onPacket(PacketEvent.Receive event) {
    if (!(event.packet instanceof ClientboundOpenScreenPacket packet)) return;
    
    String title = packet.getTitle().getString();
    
    // Debug - print every GUI title to chat
    if (mc.player != null) {
        mc.player.displayClientMessage(
            Component.literal("§eGUI opened: §f" + title), false
        );
    }

    if (!waitingForCurrencyMenu) return;
    if (!title.contains("Wager")) return;

    new Thread(() -> {
        try {
            Thread.sleep(200);
            mc.execute(() -> clickCurrency());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }).start();
}
