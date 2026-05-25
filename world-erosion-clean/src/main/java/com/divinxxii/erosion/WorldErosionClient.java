package com.divinxxii.erosion;

import com.divinxxii.erosion.hud.ErosionHudRenderer;
import com.divinxxii.erosion.network.ErosionNetworking;
import net.fabricmc.api.ClientModInitializer;

public class WorldErosionClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ErosionNetworking.registerClientPackets();
        ErosionHudRenderer.register();
    }
}
