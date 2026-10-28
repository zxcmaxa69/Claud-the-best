package com.example.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class WatermarkPill {
    private static final String NAME = "claude.uz";
    private static final String ICON_FILE = "claud_icon.png";
    private static final ResourceLocation ICON =
            ResourceLocation.fromNamespaceAndPath("modid", ICON_FILE);
    private static final int ICON_SIZE = 14;

    private static final int HEIGHT = 20;
    private static final int PAD = 6;

    private static int texW = 64;
    private static int texH = 64;
    private static boolean sized = false;

    private static void loadSize(Minecraft mc) {
        if (sized) return;
        sized = true;
        try {
            var res = mc.getResourceManager().getResource(ICON);
            if (res.isPresent()) {
                try (var in = res.get().open(); NativeImage img = NativeImage.read(in)) {
                    texW = img.getWidth();
                    texH = img.getHeight();
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static int ping(Minecraft mc) {
        if (mc.player == null || mc.getConnection() == null) return 0;
        var info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        return info == null ? 0 : info.getLatency();
    }

    private static String text(Minecraft mc) {
        return NAME + " | " + ping(mc) + "ms | " + Hud.fps + " FPS";
    }

    private static int width(Minecraft mc) {
        return PAD + ICON_SIZE + 5 + mc.font.width(text(mc)) + PAD + 2;
    }

    static boolean hit(double dx, double dy) {
        Minecraft mc = Minecraft.getInstance();
        return Math.abs(dx) <= width(mc) / 2.0 && Math.abs(dy) <= HEIGHT / 2.0;
    }

    static void draw(GuiGraphics gui, int cx, int cy) {
        Minecraft mc = Minecraft.getInstance();
        loadSize(mc);

        int w = width(mc);
        int x0 = cx - w / 2;
        int y0 = cy - HEIGHT / 2;

        pill(gui, x0, y0, x0 + w, y0 + HEIGHT, 0xCC14141E);

        gui.blit(RenderType::guiTextured, ICON,
                x0 + PAD, y0 + (HEIGHT - ICON_SIZE) / 2,
                0f, 0f, ICON_SIZE, ICON_SIZE, texW, texH, texW, texH);

        gui.drawString(mc.font, text(mc),
                x0 + PAD + ICON_SIZE + 5, y0 + (HEIGHT - 8) / 2, 0xFFFFFFFF);
    }

    private static void pill(GuiGraphics gui, int x0, int y0, int x1, int y1, int color) {
        int h = y1 - y0;
        double r = h / 2.0;
        for (int i = 0; i < h; i++) {
            double dy = i + 0.5 - r;
            int inset = (int) Math.round(r - Math.sqrt(Math.max(0, r * r - dy * dy)));
            gui.fill(x0 + inset, y0 + i, x1 - inset, y0 + i + 1, color);
        }
    }
                     }
