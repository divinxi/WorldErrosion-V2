package com.divinxxii.erosion.command;

import com.divinxxii.erosion.ErosionManager;
import com.divinxxii.erosion.network.ErosionNetworking;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * Registers the /erosion command with the following sub-commands:
 *
 *   /erosion start <seconds>
 *   /erosion pause
 *   /erosion resume
 *   /erosion stop
 *   /erosion accelerate <factor> <minSeconds>
 */
public class ErosionCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("erosion")
                .requires(source -> source.hasPermissionLevel(2)) // op-level

                // /erosion start <seconds>
                .then(CommandManager.literal("start")
                    .then(CommandManager.argument("seconds", IntegerArgumentType.integer(1, 3600))
                        .executes(ctx -> executeStart(ctx,
                                IntegerArgumentType.getInteger(ctx, "seconds")))))

                // /erosion pause
                .then(CommandManager.literal("pause")
                    .executes(ErosionCommand::executePause))

                // /erosion resume
                .then(CommandManager.literal("resume")
                    .executes(ErosionCommand::executeResume))

                // /erosion stop
                .then(CommandManager.literal("stop")
                    .executes(ErosionCommand::executeStop))

                // /erosion accelerate <factor> <minSeconds>
                // factor is a float expressed as int percentage (e.g. 95 = 0.95)
                .then(CommandManager.literal("accelerate")
                    .then(CommandManager.argument("factor_percent", IntegerArgumentType.integer(10, 99))
                        .then(CommandManager.argument("min_seconds", IntegerArgumentType.integer(1, 60))
                            .executes(ctx -> executeAccelerate(ctx,
                                    IntegerArgumentType.getInteger(ctx, "factor_percent"),
                                    IntegerArgumentType.getInteger(ctx, "min_seconds"))))))
        );
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private static int executeStart(CommandContext<ServerCommandSource> ctx, int seconds) {
        ErosionManager mgr = ErosionManager.getInstance();
        boolean wasRunning = mgr.getState() == ErosionManager.State.RUNNING;

        mgr.start(seconds);

        // Sync HUD to all players immediately
        ErosionNetworking.broadcastState(ctx.getSource().getServer());

        String msg = wasRunning
                ? "§aErosion restarted§r — interval set to §e" + seconds + "s§r."
                : "§aErosion started§r — first wave in §e" + seconds + "s§r.";
        ctx.getSource().sendFeedback(() -> Text.literal(msg), true);
        return 1;
    }

    private static int executePause(CommandContext<ServerCommandSource> ctx) {
        ErosionManager mgr = ErosionManager.getInstance();

        if (mgr.getState() != ErosionManager.State.RUNNING) {
            ctx.getSource().sendFeedback(
                () -> Text.literal("§cErosion is not currently running."), false);
            return 0;
        }

        mgr.pause();
        ErosionNetworking.broadcastState(ctx.getSource().getServer());
        ctx.getSource().sendFeedback(() -> Text.literal("§eErosion paused."), true);
        return 1;
    }

    private static int executeResume(CommandContext<ServerCommandSource> ctx) {
        ErosionManager mgr = ErosionManager.getInstance();

        if (mgr.getState() != ErosionManager.State.PAUSED) {
            ctx.getSource().sendFeedback(
                () -> Text.literal("§cErosion is not paused."), false);
            return 0;
        }

        mgr.resume();
        ErosionNetworking.broadcastState(ctx.getSource().getServer());
        ctx.getSource().sendFeedback(() -> Text.literal("§aErosion resumed."), true);
        return 1;
    }

    private static int executeStop(CommandContext<ServerCommandSource> ctx) {
        ErosionManager mgr = ErosionManager.getInstance();

        if (mgr.getState() == ErosionManager.State.STOPPED) {
            ctx.getSource().sendFeedback(
                () -> Text.literal("§cErosion is already stopped."), false);
            return 0;
        }

        mgr.stop();
        ErosionNetworking.broadcastState(ctx.getSource().getServer());
        ctx.getSource().sendFeedback(() -> Text.literal("§cErosion stopped."), true);
        return 1;
    }

    private static int executeAccelerate(CommandContext<ServerCommandSource> ctx,
                                          int factorPercent, int minSeconds) {
        float factor = factorPercent / 100.0f;
        ErosionManager.getInstance().setAccelerating(true, factor, minSeconds);
        ErosionNetworking.broadcastState(ctx.getSource().getServer());
        ctx.getSource().sendFeedback(() -> Text.literal(
            "§aAccelerating erosion enabled§r — factor: §e" + factorPercent
            + "%§r, minimum interval: §e" + minSeconds + "s§r."), true);
        return 1;
    }
}
