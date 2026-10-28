package com.example.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClickGui extends Screen {
    private static final int PANEL_W = 170;
    private static final int TITLE_H = 20;
    private static final int ROW_H = 20;

    public ClickGui() {
        super(Component.literal("Claud-the-best"));
    }

    private int panelX() {
        return (this.width - PANEL_W) / 2;
    }

    private int panelY() {
        return 24;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        super.render(gui, mouseX, mouseY, delta);

        int x = panelX();
        int y = panelY();
        int rows = Modules.ALL.size() + 1;
        int h = TITLE_H + rows * ROW_H + 4;

        gui.fill(x - 2, y - 2, x + PANEL_W + 2, y + h + 2, 0xCC101018);
        gui.fill(x, y, x + PANEL_W, y + TITLE_H, 0xFF3A3A6A);
        gui.drawCenteredString(this.font, "Claud-the-best", x + PANEL_W / 2, y + 6, 0xFFFFFFFF);

        int ry = y + TITLE_H + 2;
        for (Module m : Modules.ALL) {
            boolean hover = inside(mouseX, mouseY, x, ry, PANEL_W, ROW_H);
            int bg = m.enabled ? (hover ? 0xFF3FA66B : 0xFF2E8B57)
                               : (hover ? 0xFF4A4A5A : 0xFF3A3A4A);
            gui.fill(x + 2, ry, x + PANEL_W - 2, ry + ROW_H - 2, bg);
            gui.drawString(this.font, m.name, x + 8, ry + 6, 0xFFFFFFFF);
            String state = m.enabled ? "YOQ" : "O'CH";
            gui.drawString(this.font, state,
                    x + PANEL_W - 8 - this.font.width(state), ry + 6, 0xFFDDDDDD);
            ry += ROW_H;
        }

        boolean hover = inside(mouseX, mouseY, x, ry, PANEL_W, ROW_H);
        gui.fill(x + 2, ry, x + PANEL_W - 2, ry + ROW_H - 2, hover ? 0xFF5A5A9A : 0xFF4A4A8A);
        gui.drawCenteredString(this.font, "HUD joyini o'zgartirish",
                x + PANEL_W / 2, ry + 6, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = panelX();
        int ry = panelY() + TITLE_H + 2;

        for (Module m : Modules.ALL) {
            if (inside(mouseX, mouseY, x, ry, PANEL_W, ROW_H)) {
                m.enabled = !m.enabled;
                Config.save();
                return true;
            }
            ry += ROW_H;
        }

        if (inside(mouseX, mouseY, x, ry, PANEL_W, ROW_H)) {
            this.minecraft.setScreen(new WatermarkEditScreen());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public void removed() {
        Config.save();
    }
                       }
