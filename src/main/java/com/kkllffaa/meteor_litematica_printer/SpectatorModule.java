package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Categories;

public class SpectatorModule extends Module {
    public SpectatorModule() {
        super(Categories.Player, "spectator-plus", "A placeholder for the spectator module.");
    }

    @Override
    public void onActivate() {
        info("Spectator Plus activated! (Logic coming soon)");
    }

    @Override
    public void onDeactivate() {
        info("Spectator Plus deactivated!");
    }
}
