package com.divinxxii.erosion.util;

import com.divinxxii.erosion.ErosionManager;
import com.divinxxii.erosion.network.ErosionNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

/**
 * Wires the ErosionManager into the server tick loop and
 * handles optional warning effects (sound + particle burst).
 *
 * Warning effects fire at WARNING_SECONDS before each erosion wave.
 */
public class ErosionTickHandler {

    private static final int WARNING_SECONDS = 3;
    private static boolean warningFired = false;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ErosionTickHandler::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        ErosionManager mgr = ErosionManager.getInstance();
        ErosionManager.State stateBefore = mgr.getState();
        int secondsBefore = mgr.getSecondsRemaining();

        // Tick the erosion engine
        mgr.tick(server);

        ErosionManager.State stateAfter = mgr.getState();
        int secondsAfter = mgr.getSecondsRemaining();

        // Broadcast HUD state every second (every 20 ticks) while running
        // Also broadcast when a state change or countdown flip occurs
        if (stateAfter == ErosionManager.State.RUNNING) {
            if (secondsBefore != secondsAfter) {
                ErosionNetworking.broadcastState(server);
            }

            // Warning effects — fire once when crossing the warning threshold
            if (secondsAfter == WARNING_SECONDS && !warningFired) {
                warningFired = true;
                playWarning(server);
            } else if (secondsAfter > WARNING_SECONDS) {
                warningFired = false;
            }
        } else if (stateBefore != stateAfter) {
            // State changed (e.g. stopped after running) — sync immediately
            ErosionNetworking.broadcastState(server);
            warningFired = false;
        }
    }

    /**
     * Play a subtle warning sound to all players in all worlds.
     * Uses the anvil land sound — heavy, ominous, unmistakable.
     */
    private static void playWarning(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            world.getPlayers().forEach(player ->
                    world.playSoundFromEntity(
                            null,
                            player,
                            SoundEvents.BLOCK_ANVIL_LAND,
                            SoundCategory.BLOCKS,
                            0.6f,   // volume
                            1.8f    // pitch — high pitched clang
                    )
            );
        }
    }
}
