package com.example.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ExampleModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyMapping editKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "HUD tahrirlash",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "Claud-the-best"
        ));

        HudRenderCallback.EVENT.register((gui, tick) -> Hud.render(gui));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            for (Module m : Modules.ALL) {
                if (m.enabled) m.onTick(mc);
            }
            while (editKey.consumeClick()) {
                mc.setScreen(new WatermarkEditScreen());
            }
        });

        // Chat buyruqlari: .help  .list  .t sprint
        ClientSendMessageEvents.ALLOW_CHAT.register(msg -> {
            if (!msg.startsWith(".")) return true;
            Commands.handle(msg.substring(1).trim());
            return false;
        });
        JumpCircle.init();
                Config.load();
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING
                .register(mc -> Config.save());
    }
}

abstract class Module {
    final String name;
    boolean enabled;

    Module(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    void onTick(Minecraft mc) {}
}

class Modules {
    static final List<Module> ALL = new ArrayList<>();

    static final Module WATERMARK = new Module("Watermark", true) {};
    static final Module FPS = new Module("FPS", true) {};
    static final Module SPRINT = new Module("Sprint", false) {
        @Override
        void onTick(Minecraft mc) {
            if (mc.player != null && mc.options.keyUp.isDown()) {
                mc.options.keySprint.setDown(true);
            }
        }
    };

    static {
        ALL.add(WATERMARK);
        ALL.add(FPS);
        ALL.add(SPRINT);
    }

    static Module find(String name) {
        for (Module m : ALL) {
            if (m.name.equalsIgnoreCase(name)) return m;
        }
        return null;
    }
}

class Commands {
    static void say(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal("[Claud] " + text), false);
        }
    }

    static void handle(String line) {
        String[] p = line.split("\\s+");
        String cmd = p[0].toLowerCase();

        if (cmd.equals("t") || cmd.equals("toggle")) {
            if (p.length < 2) { say("Ishlatish: .t <modul>"); return; }
            Module m = Modules.find(p[1]);
            if (m == null) { say("Bunday modul yo'q: " + p[1]); return; }
            m.enabled = !m.enabled;
            say(m.name + (m.enabled ? " yoqildi" : " o'chirildi"));
        } else if (cmd.equals("list")) {
            for (Module m : Modules.ALL) {
                say(m.name + ": " + (m.enabled ? "yoqilgan" : "o'chirilgan"));
            }
        } else {
            say("Buyruqlar: .t <modul>, .list, .help");
        }
    }
}

class Hud {
    static double offX = 0;
    static double offY = 0;
    static final int RADIUS = 28;

    static int frames = 0;
    static int fps = 0;
    static long last = System.currentTimeMillis();

    static void render(GuiGraphics gui) {
        Minecraft mc = Minecraft.getInstance();
        var font = mc.font;

        frames++;
        long now = System.currentTimeMillis();
        if (now - last >= 1000) {
            fps = frames;
            frames = 0;
            last = now;
        }

        if (Modules.WATERMARK.enabled) {
            int cx = gui.guiWidth() / 2 + (int) offX;
            int cy = gui.guiHeight() / 2 + (int) offY;
            drawCircle(gui, cx, cy);
            gui.drawCenteredString(font, "Claud", cx, cy - 4, 0xFFFFFFFF);
        }

        if (Modules.FPS.enabled) {
            gui.drawString(font, "FPS: " + fps, 4, 4, 0xFFFFFFFF);
        }

        // Yoqilgan modullar ro'yxati (o'ng yuqori burchak)
        int y = 4;
        for (Module m : Modules.ALL) {
            if (!m.enabled || m == Modules.WATERMARK || m == Modules.FPS) continue;
            int w = font.width(m.name);
            gui.drawString(font, m.name, gui.guiWidth() - w - 4, y, 0xFF55FFFF);
            y += 11;
        }
    }

    static void drawCircle(GuiGraphics gui, int cx, int cy) {
        int color = 0xCC202030;
        for (int dy = -RADIUS; dy <= RADIUS; dy++) {
            int half = (int) Math.round(Math.sqrt(RADIUS * RADIUS - dy * dy));
            gui.fill(cx - half, cy + dy, cx + half, cy + dy + 1, color);
        }
    }
}

class WatermarkEditScreen extends Screen {
    private boolean dragging = false;

    WatermarkEditScreen() {
        super(Component.literal("HUD"));
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        super.render(gui, mouseX, mouseY, delta);
        gui.drawCenteredString(this.font,
                "Aylanani sudrang. Yopish: ESC", this.width / 2, 10, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double dx = mouseX - (this.width / 2.0 + Hud.offX);
        double dy = mouseY - (this.height / 2.0 + Hud.offY);
        if (dx * dx + dy * dy <= Hud.RADIUS * Hud.RADIUS) {
            dragging = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            Hud.offX += deltaX;
            Hud.offY += deltaY;
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
