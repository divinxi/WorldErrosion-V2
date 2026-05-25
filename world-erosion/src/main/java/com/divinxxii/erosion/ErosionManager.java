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

import java.util.Set;

/**
 * Core server-side erosion engine.
 *
 * State machine:
 *   STOPPED → RUNNING → PAUSED → RUNNING → STOPPED
 *
 * The server calls {@link #tick(MinecraftServer)} every game tick (20/s).
 * When the countdown reaches zero an erosion wave fires across all loaded chunks
 * in every dimension simultaneously.
 */
public class ErosionManager {

    // ── Singleton ────────────────────────────────────────────────────────────
    private static final ErosionManager INSTANCE = new ErosionManager();
    public static ErosionManager getInstance() { return INSTANCE; }

    // ── State ─────────────────────────────────────────────────────────────────
    public enum State { STOPPED, RUNNING, PAUSED }

    private State state        = State.STOPPED;
    private int   intervalTicks = 200;   // ticks between waves (default 10 s)
    private int   ticksLeft     = 0;     // countdown

    // Optional: accelerating erosion
    private boolean accelerating        = false;
    private float   accelerationFactor  = 0.95f; // multiply interval each wave
    private int     minimumIntervalTicks = 20;    // floor: 1 second

    // Blocks that must NEVER be eroded
    private static final Set<Block> PROTECTED_BLOCKS = Set.of(
            Blocks.BEDROCK,
            Blocks.COMMAND_BLOCK,
            Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK,
            Blocks.BARRIER,
            Blocks.STRUCTURE_VOID
    );

    private ErosionManager() {}

    // ── Public API ────────────────────────────────────────────────────────────

    /** Start (or restart) with the given interval in seconds. */
    public void start(int seconds) {
        intervalTicks = seconds * 20;
        ticksLeft     = intervalTicks;
        state         = State.RUNNING;
        WorldErosionMod.LOGGER.info("[WorldErosion] Started — interval: {}s", seconds);
    }

    public void pause() {
        if (state == State.RUNNING) {
            state = State.PAUSED;
            WorldErosionMod.LOGGER.info("[WorldErosion] Paused.");
        }
    }

    public void resume() {
        if (state == State.PAUSED) {
            state = State.RUNNING;
            WorldErosionMod.LOGGER.info("[WorldErosion] Resumed.");
        }
    }

    public void stop() {
        state     = State.STOPPED;
        ticksLeft = 0;
        WorldErosionMod.LOGGER.info("[WorldErosion] Stopped.");
    }

    public void setAccelerating(boolean on, float factor, int minSeconds) {
        this.accelerating         = on;
        this.accelerationFactor   = factor;
        this.minimumIntervalTicks = minSeconds * 20;
    }

    // ── Getters for HUD / networking ──────────────────────────────────────────

    public State getState()          { return state; }
    public int   getTicksLeft()      { return ticksLeft; }
    public int   getIntervalTicks()  { return intervalTicks; }

    /** Seconds remaining, rounded up. */
    public int getSecondsRemaining() {
        return (int) Math.ceil(ticksLeft / 20.0);
    }

    // ── Tick — called every server tick ──────────────────────────────────────

    public void tick(MinecraftServer server) {
        if (state != State.RUNNING) return;

        ticksLeft--;
        if (ticksLeft <= 0) {
            performErosionWave(server);

            // Accelerating mode: shorten interval
            if (accelerating) {
                intervalTicks = Math.max(minimumIntervalTicks,
                        (int)(intervalTicks * accelerationFactor));
            }
            ticksLeft = intervalTicks;
        }
    }

    // ── Erosion wave ──────────────────────────────────────────────────────────

    private void performErosionWave(MinecraftServer server) {
        long start = System.currentTimeMillis();
        int  total = 0;

        // Process all three vanilla dimensions
        ServerWorld[] worlds = {
                server.getWorld(net.minecraft.world.World.OVERWORLD),
                server.getWorld(net.minecraft.world.World.NETHER),
                server.getWorld(net.minecraft.world.World.END)
        };

        for (ServerWorld world : worlds) {
            if (world == null) continue;
            total += erodeWorld(world);
        }

        long elapsed = System.currentTimeMillis() - start;
        WorldErosionMod.LOGGER.debug("[WorldErosion] Wave complete — {} blocks removed in {}ms", total, elapsed);
    }

    /**
     * For every loaded chunk column in {@code world}, remove the top-most
     * non-air, non-protected block.
     *
     * @return number of blocks actually removed
     */
    private int erodeWorld(ServerWorld world) {
        List<ChunkPos> loadedChunks = LoadedChunkTracker.getLoadedChunks(world);

        int removed = 0;
        for (ChunkPos chunkPos : loadedChunks) {
            if (!world.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z)) continue;
            WorldChunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
            removed += erodeChunk(world, chunk);
        }
        return removed;
    }

    /**
     * Scan every X/Z column in this 16×16 chunk and remove the highest
     * non-air, non-protected block.
     */
    private int erodeChunk(ServerWorld world, WorldChunk chunk) {
        ChunkPos pos   = chunk.getPos();
        int      baseX = pos.getStartX();
        int      baseZ = pos.getStartZ();
        int      removed = 0;

        int minY = world.getBottomY();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int worldX = baseX + lx;
                int worldZ = baseZ + lz;

                // Use the WORLD_SURFACE heightmap to get the top non-air block Y quickly
                // then walk down to find first non-protected block
                int topY = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, lx, lz);

                for (int y = topY; y >= minY; y--) {
                    BlockPos blockPos = new BlockPos(worldX, y, worldZ);
                    var blockState = world.getBlockState(blockPos);

                    if (blockState.isAir()) continue;
                    if (PROTECTED_BLOCKS.contains(blockState.getBlock())) break;

                    world.removeBlock(blockPos, false);
                    removed++;
                    break;
                }
            }
        }

        return removed;
    }
}
