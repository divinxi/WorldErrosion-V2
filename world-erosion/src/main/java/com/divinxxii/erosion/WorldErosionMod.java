package com.divinxxii.erosion;

import com.divinxxii.erosion.command.ErosionCommand;
import com.divinxxii.erosion.network.ErosionNetworking;
import com.divinxxii.erosion.util.ErosionTickHandler;
import com.divinxxii.erosion.util.LoadedChunkTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldErosionMod implements ModInitializer {

    public static final String MOD_ID = "worlderosion";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[WorldErosion] Initializing — the world will not last forever.");

        // Register server→client networking
        ErosionNetworking.registerServerPackets();

        // Register the /erosion command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ErosionCommand.register(dispatcher)
        );

        // Track loaded chunks via Fabric events (used by ErosionManager)
        LoadedChunkTracker.register();

        // Register server tick handler (drives the erosion engine)
        ErosionTickHandler.register();

        LOGGER.info("[WorldErosion] Ready. Use /erosion start <seconds> to begin.");
    }
}
