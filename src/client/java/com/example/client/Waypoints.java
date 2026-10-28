package com.example.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class Waypoints {
    private record Point(double x, double y, double z, String dim) {}

    static final Module MODULE = new Module("Waypoint", true) {};

    private static final Path FILE =
            FabricLoader.getInstance().getConfigDir().resolve("claud-waypoints.properties");
    private static final Map<String, Point> POINTS = new LinkedHashMap<>();

    static void init() {
        Modules.ALL.add(MODULE);
        load();
        HudRenderCallback.EVENT.register((gui, tick) -> render(gui));
    }

    // Chat buyruqlari: .wp add <nom>  .wp del <nom>  .wp list  .wp clear
    static void handle(String[] p) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (p.length < 2) {
            Commands.say("Ishlatish: .wp add <nom> | .wp del <nom> | .wp list | .wp clear");
            return;
        }
        String sub = p[1].toLowerCase();

        if (sub.equals("add")) {
            if (p.length < 3) {
                Commands.say("Nom yozing: .wp add <nom>");
                return;
            }
            String dim = mc.level.dimension().location().toString();
            POINTS.put(p[2], new Point(mc.player.getX(), mc.player.getY(), mc.player.getZ(), dim));
            save();
            Commands.say("Waypoint saqlandi: " + p[2]);
        } else if (sub.equals("del")) {
            if (p.length < 3) {
                Commands.say("Nom yozing: .wp del <nom>");
                return;
            }
            if (POINTS.remove(p[2]) != null) {
                save();
                Commands.say("O'chirildi: " + p[2]);
            } else {
                Commands.say("Bunday waypoint yo'q: " + p[2]);
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
        var font = mc.font;
        int y = 18;

        for (var e : POINTS.entrySet()) {
            Point pt = e.getValue();
            if (!pt.dim().equals(dim)) continue;

            double dx = pt.x() - mc.player.getX();
            double dz = pt.z() - mc.player.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            double target = Math.toDegrees(Math.atan2(-dx, dz));
            double diff = Mth.wrapDegrees(target - mc.player.getYRot());

            gui.drawString(font, e.getKey() + "  " + (int) dist + "m  " + direction(diff),
                    4, y, 0xFFFFD955);
            y += 11;
        }
    }

    private static String direction(double diff) {
        double a = Math.abs(diff);
        boolean right = diff > 0;
        if (a < 22.5) return "oldinda";
        if (a < 67.5) return right ? "o'ng-old" : "chap-old";
        if (a < 112.5) return right ? "o'ngda" : "chapda";
        if (a < 157.5) return right ? "o'ng-orqa" : "chap-orqa";
        return "orqada";
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
