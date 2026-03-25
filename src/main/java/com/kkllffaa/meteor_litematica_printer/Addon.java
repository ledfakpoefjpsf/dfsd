package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Addon extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("Spectator Plus");
    
    // This creates the category in your ClickGUI
    public static final Category CATEGORY = new Category("Custom");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Spectator Plus...");
        
        // We COMMENTED OUT the printer so it doesn't cause errors
        // Modules.get().add(new Printer());
        
        // We only add your new Spectator module
        Modules.get().add(new SpectatorModule());
    }

    @Override
    public String getPackage() {
        return "com.kkllffaa.meteor_litematica_printer";
    }
}
