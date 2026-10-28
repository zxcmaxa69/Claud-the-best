package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

public class Trails {
    // Rangni o'zgartirish: END_ROD (oq), SOUL_FIRE_FLAME (ko'k), WITCH (binafsha),
    // HAPPY_VILLAGER (yashil), FLAME (olov), CLOUD (bulut)
    private static final ParticleOptions PARTICLE = ParticleTypes.WITCH;

    static final Module MODULE = new Module("Trails", true) {
        @Override
        void onTick(Minecraft mc) {
            if (mc.player == null || mc.level == null) return;
            var v = mc.player.getDeltaMovement();
            if (v.x * v.x + v.z * v.z < 0.0004) return;

            double x = mc.player.getX();
            double y = mc.player.getY() + 0.1;
            double z = mc.player.getZ();
            for (int i = 0; i < 2; i++) {
                mc.level.addParticle(PARTICLE,
                        x + (Math.random() - 0.5) * 0.4, y,
                        z + (Math.random() - 0.5) * 0.4,
                        0, 0.01, 0);
            }
        }
    };

    static void init() {
        Modules.ALL.add(MODULE);
    }
      }
