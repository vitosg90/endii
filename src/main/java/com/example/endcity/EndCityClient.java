package com.example.endcity;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class EndCityClient implements ClientModInitializer {
    public static final String MOD_ID = "endcitydetector";

    public static KeyMapping SETTINGS_KEY;
    public static KeyMapping LIST_KEY;

    @Override
    public void onInitializeClient() {
        DetectorConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "main"));
        SETTINGS_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.endcitydetector.settings", InputConstants.Type.KEYSYM, InputConstants.KEY_K, category));
        LIST_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.endcitydetector.list", InputConstants.Type.KEYSYM, InputConstants.KEY_L, category));

        ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> Detector.onEntityLoad(entity, level));
        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> Detector.onChunkLoad(level, chunk));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> Detector.reset());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Detector.tick();
            while (SETTINGS_KEY.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    client.gui.setScreen(new SettingsScreen(null));
                }
            }
            while (LIST_KEY.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    Detector.printFound();
                }
            }
        });
    }
}
