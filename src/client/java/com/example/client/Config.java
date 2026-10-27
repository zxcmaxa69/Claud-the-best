package com.example.client;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Config {
    private static final Path FILE =
            FabricLoader.getInstance().getConfigDir().resolve("claud-the-best.properties");

    static void load() {
        if (!Files.exists(FILE)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
        } catch (IOException e) {
            return;
        }
        try {
            Hud.offX = Double.parseDouble(p.getProperty("hud.x", "0"));
            Hud.offY = Double.parseDouble(p.getProperty("hud.y", "0"));
        } catch (NumberFormatException ignored) {
        }
        for (Module m : Modules.ALL) {
            String v = p.getProperty("module." + m.name.toLowerCase());
            if (v != null) m.enabled = Boolean.parseBoolean(v);
        }
    }

    static void save() {
        Properties p = new Properties();
        p.setProperty("hud.x", String.valueOf(Hud.offX));
        p.setProperty("hud.y", String.valueOf(Hud.offY));
        for (Module m : Modules.ALL) {
            p.setProperty("module." + m.name.toLowerCase(), String.valueOf(m.enabled));
        }
        try (OutputStream out = Files.newOutputStream(FILE)) {
            p.store(out, "Claud-the-best");
        } catch (IOException ignored) {
        }
    }
                                     }
