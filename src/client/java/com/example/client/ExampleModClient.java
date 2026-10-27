package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ExampleModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyBinding editKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Watermark tahrirlash",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "Claud-the-best"
        ));

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            int cx = ctx.getScaledWindowWidth() / 2 + (int) Watermark.offX;
            int cy = ctx.getScaledWindowHeight() / 2 + (int) Watermark.offY;
            Watermark.draw(ctx, cx, cy);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (editKey.wasPressed()) {
                client.setScreen(new WatermarkEditScreen());
            }
        });
    }
}

class Watermark {
    static double offX = 0;
    static double offY = 0;
    static final int RADIUS = 28;

    static void draw(DrawContext ctx, int cx, int cy) {
        int color = 0xCC202030;
        for (int dy = -RADIUS; dy <= RADIUS; dy++) {
            int half = (int) Math.round(Math.sqrt(RADIUS * RADIUS - dy * dy));
            ctx.fill(cx - half, cy + dy, cx + half, cy + dy + 1, color);
        }
        var tr = MinecraftClient.getInstance().textRenderer;
        ctx.drawCenteredTextWithShadow(tr, "Claud", cx, cy - 4, 0xFFFFFFFF);
    }
}

class WatermarkEditScreen extends Screen {
    private boolean dragging = false;

    WatermarkEditScreen() {
        super(Text.literal("Watermark"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer,
                "Aylanani sudrang. Yopish: ESC", width / 2, 10, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double cx = width / 2.0 + Watermark.offX;
        double cy = height / 2.0 + Watermark.offY;
        double dx = mouseX - cx;
        double dy = mouseY - cy;
        if (dx * dx + dy * dy <= Watermark.RADIUS * Watermark.RADIUS) {
            dragging = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            Watermark.offX += deltaX;
            Watermark.offY += deltaY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
	}
