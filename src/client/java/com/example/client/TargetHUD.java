package com.example.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;

import java.util.Locale;

public class TargetHud {
    private static LivingEntity target;
    private static long lastSeen = 0;
    private static float shownHp = 0;

    static final Module MODULE = new Module("TargetHUD", true) {
        @Override
        void onTick(Minecraft mc) {
            if (mc.player == null) return;
            if (mc.crosshairPickEntity instanceof LivingEntity le && le.isAlive()) {
                if (le != target) shownHp = le.getHealth();
                target = le;
                lastSeen = System.currentTimeMillis();
            }
        }
    };

    static void init() {
        Modules.ALL.add(MODULE);
        HudRenderCallback.EVENT.register((gui, tick) -> render(gui));
    }

    private static void render(GuiGraphics gui) {
        Minecraft mc = Minecraft.getInstance();
        if (!MODULE.enabled || mc.player == null || target == null) return;
        if (!target.isAlive() || System.currentTimeMillis() - lastSeen > 3000) return;

        var font = mc.font;
        float hp = Math.max(0f, target.getHealth());
        float max = Math.max(1f, target.getMaxHealth());
        shownHp += (hp - shownHp) * 0.15f;

        int x = gui.guiWidth() / 2 + 14;
        int y = gui.guiHeight() / 2 + 14;
        int w = 130;
        int h = 38;

        gui.fill(x, y, x + w, y + h, 0xCC14141E);
        gui.fill(x, y, x + 2, y + h, 0xFF8A4DFF);

        String name = target.getName().getString();
        gui.drawString(font, name, x + 8, y + 5, 0xFFFFFFFF);

        float ratio = Math.min(1f, shownHp / max);
        int barX = x + 8;
        int barY = y + 17;
        int barW = w - 16;
        int color = ratio > 0.5f ? 0xFF3FD16B : (ratio > 0.25f ? 0xFFE8C53A : 0xFFE5484D);

        gui.fill(barX, barY, barX + barW, barY + 6, 0xFF2A2A3A);
        gui.fill(barX, barY, barX + (int) (barW * ratio), barY + 6, color);

        String info = String.format(Locale.ROOT, "HP %.1f / %.0f   %.1fm",
                hp, max, mc.player.distanceTo(target));
        gui.drawString(font, info, x + 8, y + 26, 0xFFCCCCDD);
    }
  }
