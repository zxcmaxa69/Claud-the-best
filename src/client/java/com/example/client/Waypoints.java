package com.example.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class Waypoints {
    private record Point(double x, double y, double z, String dim) {}

    static final Module MODULE = new Module("Waypoint", true) {};

    private static final int COLOR_FROM = 0x1E9BFF;
    private static final int COLOR_TO = 0xA030FF;

    private static final Path FILE =
            FabricLoader.getInstance().getConfigDir().resolve("claud-waypoints.properties");
    private static final Map<String, Point> POINTS = new LinkedHashMap<>();

    static void init() {
        Modules.ALL.add(MODULE);
        load();
        HudRenderCallback.EVENT.register((gui, tick) -> render(gui));
    }

    // .wp add <nom>                (turgan joyingiz)
    // .wp add <nom> <x> <y> <z>    (o'zingiz yozgan koordinata)
    // .wp remove <nom>   .wp list   .wp clear
    static void handle(String[] p) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (p.length < 2) {
            Commands.say("Ishlatish: .wp add <nom> [x y z] | .wp remove <nom> | .wp list | .wp clear");
            return;
        }
        String sub = p[1].toLowerCase();

        if (sub.equals("add")) {
            if (p.length < 3) {
                Commands.say("Nom yozing: .wp add <nom> [x y z]");
                return;
            }
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();
            int nameEnd = p.length;

            if (p.length >= 6) {
                try {
                    double nx = Double.parseDouble(p[p.length - 3]);
                    double ny = Double.parseDouble(p[p.length - 2]);
                    double nz = Double.parseDouble(p[p.length - 1]);
                    x = nx;
                    y = ny;
                    z = nz;
                    nameEnd = p.length - 3;
                } catch (NumberFormatException ignored) {
                }
            }

            String name = String.join(" ", Arrays.copyOfRange(p, 2, nameEnd));
            String dim = mc.level.dimension().location().toString();
            POINTS.put(name, new Point(x, y, z, dim));
            save();
            Commands.say("Waypoint saqlandi: " + name + " (" + (int) x + " " + (int) y + " " + (int) z + ")");
        } else if (sub.equals("remove") || sub.equals("del")) {
            if (p.length < 3) {
                Commands.say("Nom yozing: .wp remove <nom>");
                return;
            }
            String name = String.join(" ", Arrays.copyOfRange(p, 2, p.length));
            if (POINTS.remove(name) != null) {
                save();
                Commands.say("O'chirildi: " + name);
            } else {
                Commands.say("Bunday waypoint yo'q: " + name);
            }
        } else if (sub.equals("list")) {
            if (POINTS.isEmpty()) {
                Commands.say("Waypoint yo'q");
            }
            for (var e : POINTS.entrySet()) {
                Point pt = e.getValue();
                Commands.say(e.getKey() + ": " + (int) pt.x() + " " + (int) pt.y() + " " + (int) pt.z());
            }
        } else if (sub.equals("clear")) {
            POINTS.clear();
            save();
            Commands.say("Hammasi o'chirildi");
        } else {
            Commands.say("Noma'lum buyruq: " + sub);
        }
    }

    private static void render(GuiGraphics gui) {
        Minecraft mc = Minecraft.getInstance();
        if (!MODULE.enabled || mc.player == null || mc.level == null) return;

        String dim = mc.level.dimension().location().toString();
        Camera cam = mc.gameRenderer.getMainCamera();
        Vec3 cp = cam.getPosition();
        Vector3f look = cam.getLookVector();
        Vector3f up = cam.getUpVector();
        Vector3f left = cam.getLeftVector();

        double tan = Math.tan(Math.toRadians(mc.options.fov().get()) / 2.0);
        int w = gui.guiWidth();
        int h = gui.guiHeight();
        double aspect = (double) w / h;
        Font font = mc.font;

        for (var e : POINTS.entrySet()) {
            Point pt = e.getValue();
            if (!pt.dim().equals(dim)) continue;

            double rx = pt.x() - cp.x;
            double ry = pt.y() + 1.0 - cp.y;
            double rz = pt.z() - cp.z;

            double depth = rx * look.x() + ry * look.y() + rz * look.z();
            if (depth < 0.1) continue;

            double lx = rx * left.x() + ry * left.y() + rz * left.z();
            double uy = rx * up.x() + ry * up.y() + rz * up.z();

            double sx = w / 2.0 - (lx / (depth * tan * aspect)) * (w / 2.0);
            double sy = h / 2.0 - (uy / (depth * tan)) * (h / 2.0);
            if (sx < -60 || sx > w + 60 || sy < -30 || sy > h + 30) continue;

            double dist = Math.sqrt(rx * rx + ry * ry + rz * rz);
            drawLabel(gui, font, e.getKey(), dist, (int) sx, (int) sy);
        }
    }

    private static void drawLabel(GuiGraphics gui, Font font, String name, double dist, int sx, int sy) {
        float scale = 1.4f;
        int nameW = font.width(name);
        String d = String.format(Locale.ROOT, "%.1fm", dist);
        int dw = font.width(d);
        int x0 = -(nameW / 2) - 4;

        gui.pose().pushPose();
        gui.pose().translate(sx, sy, 0);
        gui.pose().scale(scale, scale, 1.0f);

        gui.fill(x0, -13, x0 + nameW + 8, -1, 0xCC1A2040);
        drawGradient(gui, font, name, -(nameW / 2), -11);

        gui.fill(x0, -1, x0 + dw + 6, 10, 0xCC0F2A2A);
        gui.drawString(font, d, x0 + 3, 1, 0xFFFFFFFF);

        gui.pose().popPose();
    }

    private static void drawGradient(GuiGraphics gui, Font font, String text, int x, int y) {
        int n = text.length();
        for (int i = 0; i < n; i++) {
            float t = n <= 1 ? 0f : (float) i / (n - 1);
            int r = lerp((COLOR_FROM >> 16) & 0xFF, (COLOR_TO >> 16) & 0xFF, t);
            int g = lerp((COLOR_FROM >> 8) & 0xFF, (COLOR_TO >> 8) & 0xFF, t);
            int b = lerp(COLOR_FROM & 0xFF, COLOR_TO & 0xFF, t);
            String ch = String.valueOf(text.charAt(i));
            gui.drawString(font, ch, x, y, 0xFF000000 | (r << 16) | (g << 8) | b);
            x += font.width(ch);
        }
    }

    private static int lerp(int a, int b, float t) {
        return Math.round(a + (b - a) * t);
    }

    private static void save() {
        Properties props = new Properties();
        for (var e : POINTS.entrySet()) {
            Point pt = e.getValue();
            props.setProperty(e.getKey(), pt.x() + "," + pt.y() + "," + pt.z() + "," + pt.dim());
        }
        try (OutputStream out = Files.newOutputStream(FILE)) {
            props.store(out, "Claud-the-best waypoints");
        } catch (IOException ignored) {
        }
    }

    private static void load() {
        if (!Files.exists(FILE)) return;
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            props.load(in);
        } catch (IOException e) {
            return;
        }
        for (String name : props.stringPropertyNames()) {
            String[] s = props.getProperty(name).split(",", 4);
            if (s.length < 4) continue;
            try {
                POINTS.put(name, new Point(
                        Double.parseDouble(s[0]), Double.parseDouble(s[1]),
                        Double.parseDouble(s[2]), s[3]));
            } catch (NumberFormatException ignored) {
            }
        }
    }
}
