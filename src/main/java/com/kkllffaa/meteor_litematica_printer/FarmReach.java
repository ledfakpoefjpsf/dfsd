package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.entity.player.ReachEvent;
import meteordevelopment.orbit.EventHandler;

public class FarmReach extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("reach-range")
        .description("How far you can reach blocks/crops. Server limit is usually ~5.5.")
        .defaultValue(5.0)
        .min(0)
        .sliderMax(10.0) 
        .build()
    );

    public FarmReach() {
        // Uses the custom Category defined in your Addon class
        super(Addon.CATEGORY, "farm-reach", "Extends your reach for farming and block interaction.");
    }

    @EventHandler
    private void onReach(ReachEvent event) {
        // Overrides the vanilla reach value with the slider value
        event.reach = range.get().floatValue();
    }
}
