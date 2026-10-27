package com.example.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ExampleModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyMapping editKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "Watermark tahrirlash",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "Claud-the-best"
        ));

        HudRenderCallback.EVENT.register((gui, tickCounter) -> {
            int cx = gui.guiWidth() / 2 + (int) Watermark.offX;
            int cy = gui.guiHeight() / 2 + (int) Watermark.offY;
            Watermark.draw(gui, cx, cy);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (editKey.consumeClick()) {
                client.setScreen(new WatermarkEditScreen());
            }
        });
    }
}

class Watermark {
    static double offX = 0;
    static double offY = 0;
    static final int RADIUS = 28;

    static void draw(GuiGraphics gui, int cx, int cy) {
        int color = 0xCC202030;
        for (int dy = -RADIUS; dy <= RADIUS; dy++) {
            int half = (int) Math.round(Math.sqrt(RADIUS * RADIUS - dy * dy));
            gui.fill(cx - half, cy + dy, cx + half, cy + dy + 1, color);
        }
        var font = Minecraft.getInstance().font;
        gui.drawCenteredString(font, "Claud", cx, cy - 4, 0xFFFFFFFF);
    }
}

class WatermarkEditScreen extends Screen {
    private boolean dragging = false;

    WatermarkEditScreen() {
        super(Component.literal("Watermark"));
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        super.render(gui, mouseX, mouseY, delta);
        gui.drawCenteredString(this.font,
                "Aylanani sudrang. Yopish: ESC", this.width / 2, 10, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double cx = this.width / 2.0 + Watermark.offX;
        double cy = this.height / 2.0 + Watermark.offY;
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
