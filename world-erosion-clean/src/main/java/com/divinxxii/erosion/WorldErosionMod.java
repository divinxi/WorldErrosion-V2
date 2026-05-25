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
        ErosionNetworking.registerServerPackets();
        LoadedChunkTracker.register();
        ErosionTickHandler.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ErosionCommand.register(dispatcher));
        LOGGER.info("[WorldErosion] Ready. Use /erosion start <seconds>");
    }
}
