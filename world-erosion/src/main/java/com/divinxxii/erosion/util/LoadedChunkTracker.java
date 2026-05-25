package com.divinxxii.erosion.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which chunks are currently loaded per world,
 * using Fabric's ServerChunkEvents (guaranteed stable API).
 *
 * This avoids any reliance on internal ServerChunkManager methods
 * that vary across Minecraft versions.
 */
public class LoadedChunkTracker {

    // world registry key string → set of loaded ChunkPos
    private static final Map<String, Set<ChunkPos>> loadedChunks = new ConcurrentHashMap<>();

    public static void register() {
        ServerChunkEvents.CHUNK_LOAD.register(LoadedChunkTracker::onChunkLoad);
        ServerChunkEvents.CHUNK_UNLOAD.register(LoadedChunkTracker::onChunkUnload);
    }

    private static void onChunkLoad(ServerWorld world, WorldChunk chunk) {
        String key = worldKey(world);
        loadedChunks.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
                    .add(chunk.getPos());
    }

    private static void onChunkUnload(ServerWorld world, WorldChunk chunk) {
        String key = worldKey(world);
        Set<ChunkPos> set = loadedChunks.get(key);
        if (set != null) set.remove(chunk.getPos());
    }

    /** Returns a snapshot of currently loaded chunk positions for the given world. */
    public static List<ChunkPos> getLoadedChunks(ServerWorld world) {
        Set<ChunkPos> set = loadedChunks.get(worldKey(world));
        if (set == null) return Collections.emptyList();
        return new ArrayList<>(set); // snapshot
    }

    private static String worldKey(ServerWorld world) {
        return world.getRegistryKey().getValue().toString();
    }
}
