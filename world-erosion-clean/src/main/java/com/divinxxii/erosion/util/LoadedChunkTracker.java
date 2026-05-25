package com.divinxxii.erosion.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LoadedChunkTracker {

    private static final Map<String, Set<ChunkPos>> loadedChunks = new ConcurrentHashMap<>();

    public static void register() {
        ServerChunkEvents.CHUNK_LOAD.register(LoadedChunkTracker::onChunkLoad);
        ServerChunkEvents.CHUNK_UNLOAD.register(LoadedChunkTracker::onChunkUnload);
    }

    private static void onChunkLoad(ServerWorld world, WorldChunk chunk) {
        loadedChunks.computeIfAbsent(worldKey(world), k -> ConcurrentHashMap.newKeySet())
                    .add(chunk.getPos());
    }

    private static void onChunkUnload(ServerWorld world, WorldChunk chunk) {
        Set<ChunkPos> set = loadedChunks.get(worldKey(world));
        if (set != null) set.remove(chunk.getPos());
    }

    public static List<ChunkPos> getLoadedChunks(ServerWorld world) {
        Set<ChunkPos> set = loadedChunks.get(worldKey(world));
        if (set == null) return Collections.emptyList();
        return new ArrayList<>(set);
    }

    private static String worldKey(ServerWorld world) {
        return world.getRegistryKey().getValue().toString();
    }
}
