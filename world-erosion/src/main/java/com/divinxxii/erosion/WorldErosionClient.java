package com.divinxxii.erosion;

import com.divinxxii.erosion.hud.ErosionHudRenderer;
import com.divinxxii.erosion.network.ErosionNetworking;
import net.fabricmc.api.ClientModInitializer;

public class WorldErosionClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Receive server state packets and update HUD
        ErosionNetworking.registerClientPackets();

        // Register the HUD countdown renderer
        ErosionHudRenderer.register();

        WorldErosionMod.LOGGER.info("[WorldErosion] Client initialized — HUD active.");
    }
}
