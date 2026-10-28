package com.example.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JumpCircle {
    // Zarracha turini o'zgartirish uchun shu qatorni almashtiring:
    // ParticleTypes.END_ROD (oq), SOUL_FIRE_FLAME (ko'k), WITCH (binafsha),
    // HAPPY_VILLAGER (yashil), FLAME (olov)
    private static final ParticleOptions PARTICLE = ParticleTypes.END_ROD;

    private static final int POINTS = 28;
    private static final int MAX_AGE = 9;

    private static final List<double[]> RINGS = new ArrayList<>();
    private static boolean prevOnGround = true;

    static final Module MODULE = new Module("JumpCircle", true) {
        @Override
        void onTick(Minecraft mc) {
            if (mc.player == null || mc.level == null) return;

            boolean onGround = mc.player.onGround();
            double vy = mc.player.getDeltaMovement().y;

            if (prevOnGround && !onGround && vy > 0) {
                RINGS.add(new double[]{
                        mc.player.getX(), mc.player.getY() + 0.1, mc.player.getZ(), 0
                });
            }
            prevOnGround = onGround;

            Iterator<double[]> it = RINGS.iterator();
            while (it.hasNext()) {
                double[] r = it.next();
                double radius = 0.3 + r[3] * 0.25;
                for (int i = 0; i < POINTS; i++) {
                    double a = 2 * Math.PI * i / POINTS;
                    mc.level.addParticle(PARTICLE,
                            r[0] + Math.cos(a) * radius, r[1], r[2] + Math.sin(a) * radius,
                            0, 0, 0);
                }
                r[3]++;
                if (r[3] > MAX_AGE) it.remove();
            }
        }
    };

    static void init() {
        Modules.ALL.add(MODULE);
    }
}
