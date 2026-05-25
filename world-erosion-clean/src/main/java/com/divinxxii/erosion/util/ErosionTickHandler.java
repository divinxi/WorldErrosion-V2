package com.divinxxii.erosion.util;

import com.divinxxii.erosion.ErosionManager;
import com.divinxxii.erosion.network.ErosionNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class ErosionTickHandler {

    private static final int WARNING_SECONDS = 3;
    private static boolean warningFired = false;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ErosionTickHandler::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        ErosionManager mgr = ErosionManager.getInstance();
        ErosionManager.State stateBefore  = mgr.getState();
        int                  secondsBefore = mgr.getSecondsRemaining();

        mgr.tick(server);

        ErosionManager.State stateAfter  = mgr.getState();
        int                  secondsAfter = mgr.getSecondsRemaining();

        if (stateAfter == ErosionManager.State.RUNNING) {
            if (secondsBefore != secondsAfter) {
                ErosionNetworking.broadcastState(server);
            }
            if (secondsAfter == WARNING_SECONDS && !warningFired) {
                warningFired = true;
                playWarning(server);
            } else if (secondsAfter > WARNING_SECONDS) {
                warningFired = false;
            }
        } else if (stateBefore != stateAfter) {
            ErosionNetworking.broadcastState(server);
            warningFired = false;
        }
    }

    private static void playWarning(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            world.getPlayers().forEach(player ->
                    world.playSoundFromEntity(null, player,
                            SoundEvents.BLOCK_ANVIL_LAND,
                            SoundCategory.BLOCKS, 0.6f, 1.8f)
            );
        }
    }
}
