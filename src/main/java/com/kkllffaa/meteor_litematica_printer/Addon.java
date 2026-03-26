package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class Addon {
    public static void init() {
        // Register modules with Meteor Client
        Modules modules = MeteorClient.modules;
        modules.add(new CreativeSurvivalModule());
        modules.add(new SpectatorModule());
    }
}
