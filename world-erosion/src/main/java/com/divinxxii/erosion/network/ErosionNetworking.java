package com.divinxxii.erosion.network;

import com.divinxxii.erosion.ErosionManager;
import com.divinxxii.erosion.WorldErosionMod;
import com.divinxxii.erosion.hud.ErosionHudState;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

/**
 * Handles server → client HUD synchronization.
 *
 * Packet sent every tick (while running) and on every state change:
 *   - state ordinal (0=STOPPED, 1=RUNNING, 2=PAUSED)
 *   - secondsRemaining
 *   - intervalSeconds
 */
public class ErosionNetworking {

    // ── Packet definition ─────────────────────────────────────────────────────

    public static final Identifier EROSION_STATE_ID =
            Identifier.of(WorldErosionMod.MOD_ID, "erosion_state");

    public record ErosionStatePayload(int stateOrdinal, int secondsRemaining,
                                       int intervalSeconds)
            implements CustomPayload {

        public static final Id<ErosionStatePayload> ID = new Id<>(EROSION_STATE_ID);

        public static final PacketCodec<RegistryByteBuf, ErosionStatePayload> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.INTEGER, ErosionStatePayload::stateOrdinal,
                        PacketCodecs.INTEGER, ErosionStatePayload::secondsRemaining,
                        PacketCodecs.INTEGER, ErosionStatePayload::intervalSeconds,
                        ErosionStatePayload::new
                );

        @Override
        public Id<? extends CustomPayload> getId() { return ID; }
    }

    // ── Registration ──────────────────────────────────────────────────────────

    public static void registerServerPackets() {
        PayloadTypeRegistry.playS2C().register(
                ErosionStatePayload.ID,
                ErosionStatePayload.CODEC
        );
    }

    public static void registerClientPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ErosionStatePayload.ID,
                (payload, context) -> {
                    // Update client-side HUD state on the render thread
                    context.client().execute(() ->
                            ErosionHudState.update(
                                    payload.stateOrdinal(),
                                    payload.secondsRemaining(),
                                    payload.intervalSeconds()
                            )
                    );
                });
    }

    // ── Broadcast helpers ─────────────────────────────────────────────────────

    /** Send current erosion state to every connected player. */
    public static void broadcastState(MinecraftServer server) {
        ErosionManager mgr = ErosionManager.getInstance();
        ErosionStatePayload payload = new ErosionStatePayload(
                mgr.getState().ordinal(),
                mgr.getSecondsRemaining(),
                mgr.getIntervalTicks() / 20
        );
        server.getPlayerManager().getPlayerList().forEach(player ->
                ServerPlayNetworking.send(player, payload)
        );
    }
}
