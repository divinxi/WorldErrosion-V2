package com.divinxxii.erosion.hud;

import com.divinxxii.erosion.ErosionManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

/**
 * Renders the erosion countdown timer just above the hotbar.
 *
 * Display examples:
 *   "Next Erosion: 6s"   ← green, normal
 *   "Next Erosion: 3s"   ← red (warning threshold)
 *   "⏸ Paused"           ← grey
 *   (nothing)            ← when stopped
 */
public class ErosionHudRenderer {

    // Warning threshold in seconds — text turns red
    private static final int WARNING_SECONDS = 3;

    // Colors
    private static final int COLOR_NORMAL  = 0x55FF55; // bright green
    private static final int COLOR_WARNING = 0xFF5555; // red
    private static final int COLOR_PAUSED  = 0xAAAAAA; // grey
    private static final int COLOR_SHADOW  = 0x40000000; // semi-transparent black shadow bg

    public static void register() {
        HudRenderCallback.EVENT.register(ErosionHudRenderer::render);
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        ErosionManager.State state = ErosionHudState.getState();

        // Don't render anything when stopped
        if (state == ErosionManager.State.STOPPED) return;

        String displayText;
        int color;

        if (state == ErosionManager.State.PAUSED) {
            displayText = "⏸ Paused";
            color = COLOR_PAUSED;
        } else {
            int secs = ErosionHudState.secondsRemaining;
            displayText = "Next Erosion: " + secs + "s";
            color = (secs <= WARNING_SECONDS) ? COLOR_WARNING : COLOR_NORMAL;
        }

        // Position: horizontally centered, just above the hotbar
        int screenWidth  = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        int textWidth = client.textRenderer.getWidth(displayText);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight - 59; // above hotbar (hotbar is ~48px from bottom)

        // Draw a subtle dark pill background for readability
        context.fill(x - 4, y - 2, x + textWidth + 4, y + 10, COLOR_SHADOW);

        // Draw the text with shadow
        context.drawTextWithShadow(client.textRenderer, Text.literal(displayText), x, y, color);
    }
}
