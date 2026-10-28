package com.pulsevisual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class PulseVisualClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.player == null || client.world == null) {
                return;
            }

            renderHud(drawContext, client);
        });
    }

    private void renderHud(
            DrawContext context,
            MinecraftClient client
    ) {
        int x = 8;
        int y = 8;
        int lineHeight = 12;

        // HUD fonini chizish
        context.fill(
                x - 3,
                y - 3,
                x + 125,
                y + 57,
                0xB0101020
        );

        // Sarlavha
        context.drawText(
                client.textRenderer,
                Text.literal("PulseVisual"),
                x,
                y,
                0xFFD65BFF,
                true
        );

        y += lineHeight;

        // FPS
        context.drawText(
                client.textRenderer,
                Text.literal("FPS: " + client.getCurrentFps()),
                x,
                y,
                0xFFFFFFFF,
                true
        );

        y += lineHeight;

        // Koordinatalar
        if (client.player != null) {
            double px = client.player.getX();
            double py = client.player.getY();
            double pz = client.player.getZ();

            String coords = String.format(
                    "XYZ: %.0f %.0f %.0f",
                    px, py, pz
            );

            context.drawText(
                    client.textRenderer,
                    Text.literal(coords),
                    x,
                    y,
                    0xFF55FFFF,
                    true
            );
        }
    }
}
