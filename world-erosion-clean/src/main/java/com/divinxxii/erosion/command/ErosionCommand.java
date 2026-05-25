package com.divinxxii.erosion.command;

import com.divinxxii.erosion.ErosionManager;
import com.divinxxii.erosion.network.ErosionNetworking;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ErosionCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("erosion")
                .requires(source -> source.hasPermissionLevel(2))

                .then(CommandManager.literal("start")
                    .then(CommandManager.argument("seconds", IntegerArgumentType.integer(1, 3600))
                        .executes(ctx -> executeStart(ctx, IntegerArgumentType.getInteger(ctx, "seconds")))))

                .then(CommandManager.literal("pause")
                    .executes(ErosionCommand::executePause))

                .then(CommandManager.literal("resume")
                    .executes(ErosionCommand::executeResume))

                .then(CommandManager.literal("stop")
                    .executes(ErosionCommand::executeStop))

                .then(CommandManager.literal("accelerate")
                    .then(CommandManager.argument("factor_percent", IntegerArgumentType.integer(10, 99))
                        .then(CommandManager.argument("min_seconds", IntegerArgumentType.integer(1, 60))
                            .executes(ctx -> executeAccelerate(ctx,
                                    IntegerArgumentType.getInteger(ctx, "factor_percent"),
                                    IntegerArgumentType.getInteger(ctx, "min_seconds"))))))
        );
    }

    private static int executeStart(CommandContext<ServerCommandSource> ctx, int seconds) {
        ErosionManager.getInstance().start(seconds);
        ErosionNetworking.broadcastState(ctx.getSource().getServer());
        ctx.getSource().sendFeedback(() ->
                Text.literal("§aErosion started — interval: §e" + seconds + "s"), true);
        return 1;
    }

    private static int executePause(CommandContext<ServerCommandSource> ctx) {
        if (ErosionManager.getInstance().getState() != ErosionManager.State.RUNNING) {
            ctx.getSource().sendFeedback(() -> Text.literal("§cErosion is not running."), false);
            return 0;
        }
        ErosionManager.getInstance().pause();
        ErosionNetworking.broadcastState(ctx.getSource().getServer());
        ctx.getSource().sendFeedback(() -> Text.literal("§eErosion paused."), true);
        return 1;
    }

    private static int executeResume(CommandContext<ServerCommandSource> ctx) {
        if (ErosionManager.getInstance().getState() != ErosionManager.State.PAUSED) {
            ctx.getSource().sendFeedback(() -> Text.literal("§cErosion is not paused."), false);
            return 0;
        }
        ErosionManager.getInstance().resume();
        ErosionNetworking.broadcastState(ctx.getSource().getServer());
        ctx.getSource().sendFeedback(() -> Text.literal("§aErosion resumed."), true);
        return 1;
    }

    private static int executeStop(CommandContext<ServerCommandSource> ctx) {
        if (ErosionManager.getInstance().getState() == ErosionManager.State.STOPPED) {
            ctx.getSource().sendFeedback(() -> Text.literal("§cErosion is already stopped."), false);
            return 0;
        }
        ErosionManager.getInstance().stop();
        ErosionNetworking.broadcastState(ctx.getSource().getServer());
        ctx.getSource().sendFeedback(() -> Text.literal("§cErosion stopped."), true);
        return 1;
    }

    private static int executeAccelerate(CommandContext<ServerCommandSource> ctx,
                                          int factorPercent, int minSeconds) {
        ErosionManager.getInstance().setAccelerating(true, factorPercent / 100.0f, minSeconds);
        ErosionNetworking.broadcastState(ctx.getSource().getServer());
        ctx.getSource().sendFeedback(() -> Text.literal(
                "§aAccelerating erosion enabled — factor: §e" + factorPercent
                + "%§a, min: §e" + minSeconds + "s"), true);
        return 1;
    }
}
