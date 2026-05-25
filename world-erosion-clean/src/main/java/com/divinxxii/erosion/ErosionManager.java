package com.divinxxii.erosion;

import com.divinxxii.erosion.util.LoadedChunkTracker;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;

import java.util.List;
import java.util.Set;

public class ErosionManager {

    private static final ErosionManager INSTANCE = new ErosionManager();
    public static ErosionManager getInstance() { return INSTANCE; }

    public enum State { STOPPED, RUNNING, PAUSED }

    private State state         = State.STOPPED;
    private int   intervalTicks = 200;
    private int   ticksLeft     = 0;

    private boolean accelerating         = false;
    private float   accelerationFactor   = 0.95f;
    private int     minimumIntervalTicks = 20;

    private static final Set<Block> PROTECTED_BLOCKS = Set.of(
            Blocks.BEDROCK,
            Blocks.COMMAND_BLOCK,
            Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK,
            Blocks.BARRIER,
            Blocks.STRUCTURE_VOID
    );

    private ErosionManager() {}

    public void start(int seconds) {
        intervalTicks = seconds * 20;
        ticksLeft     = intervalTicks;
        state         = State.RUNNING;
    }

    public void pause() {
        if (state == State.RUNNING) state = State.PAUSED;
    }

    public void resume() {
        if (state == State.PAUSED) state = State.RUNNING;
    }

    public void stop() {
        state     = State.STOPPED;
        ticksLeft = 0;
    }

    public void setAccelerating(boolean on, float factor, int minSeconds) {
        this.accelerating         = on;
        this.accelerationFactor   = factor;
        this.minimumIntervalTicks = minSeconds * 20;
    }

    public State getState()          { return state; }
    public int   getTicksLeft()      { return ticksLeft; }
    public int   getIntervalTicks()  { return intervalTicks; }
    public int   getSecondsRemaining() {
        return (int) Math.ceil(ticksLeft / 20.0);
    }

    public void tick(MinecraftServer server) {
        if (state != State.RUNNING) return;
        ticksLeft--;
        if (ticksLeft <= 0) {
            performErosionWave(server);
            if (accelerating) {
                intervalTicks = Math.max(minimumIntervalTicks, (int)(intervalTicks * accelerationFactor));
            }
            ticksLeft = intervalTicks;
        }
    }

    private void performErosionWave(MinecraftServer server) {
        ServerWorld[] worlds = {
                server.getWorld(net.minecraft.world.World.OVERWORLD),
                server.getWorld(net.minecraft.world.World.NETHER),
                server.getWorld(net.minecraft.world.World.END)
        };
        for (ServerWorld world : worlds) {
            if (world != null) erodeWorld(world);
        }
    }

    private void erodeWorld(ServerWorld world) {
        List<ChunkPos> chunks = LoadedChunkTracker.getLoadedChunks(world);
        for (ChunkPos chunkPos : chunks) {
            if (!world.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z)) continue;
            WorldChunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
            erodeChunk(world, chunk);
        }
    }

    private void erodeChunk(ServerWorld world, WorldChunk chunk) {
        ChunkPos pos   = chunk.getPos();
        int      baseX = pos.getStartX();
        int      baseZ = pos.getStartZ();
        int      minY  = world.getBottomY();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int topY   = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, lx, lz);
                int worldX = baseX + lx;
                int worldZ = baseZ + lz;

                for (int y = topY; y >= minY; y--) {
                    BlockPos blockPos  = new BlockPos(worldX, y, worldZ);
                    var      blockState = world.getBlockState(blockPos);

                    if (blockState.isAir()) continue;
                    if (PROTECTED_BLOCKS.contains(blockState.getBlock())) break;

                    world.removeBlock(blockPos, false);
                    break;
                }
            }
        }
    }
}
