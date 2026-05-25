package com.divinxxii.erosion.hud;

import com.divinxxii.erosion.ErosionManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public class ErosionHudRenderer {

    private static final int COLOR_NORMAL  = 0x55FF55;
    private static final int COLOR_WARNING = 0xFF5555;
    private static final int COLOR_PAUSED  = 0xAAAAAA;
    private static final int COLOR_BG      = 0x40000000;
    private static final int WARNING_SECS  = 3;

    public static void register() {
        HudRenderCallback.EVENT.register(ErosionHudRenderer::render);
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        ErosionManager.State state = ErosionHudState.getState();
        if (state == ErosionManager.State.STOPPED) return;

        String displayText;
        int    color;

        if (state == ErosionManager.State.PAUSED) {
            displayText = "Paused";
            color       = COLOR_PAUSED;
        } else {
            int secs = ErosionHudState.secondsRemaining;
            displayText = "Next Erosion: " + secs + "s";
            color       = (secs <= WARNING_SECS) ? COLOR_WARNING : COLOR_NORMAL;
        }

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int textWidth = client.textRenderer.getWidth(displayText);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight - 59;

        context.fill(x - 4, y - 2, x + textWidth + 4, y + 10, COLOR_BG);
        context.drawTextWithShadow(client.textRenderer, Text.literal(displayText), x, y, color);
    }
}
