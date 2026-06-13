package dev.oum.oumlib.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class Particles {

    private Particles() {
    }

    public static void spawn(@NonNull Location loc, @NonNull Particle particle) {
        spawn(loc, particle, 1, 0, 0, 0, 0, null);
    }

    public static void spawn(@NonNull Location loc, @NonNull Particle particle, int count) {
        spawn(loc, particle, count, 0, 0, 0, 0, null);
    }

    public static void spawn(@NonNull Location loc, @NonNull Particle particle, int count, double offset) {
        spawn(loc, particle, count, offset, offset, offset, 0, null);
    }

    public static void spawn(@NonNull Location loc, @NonNull Particle particle, int count, double offsetX, double offsetY, double offsetZ) {
        spawn(loc, particle, count, offsetX, offsetY, offsetZ, 0, null);
    }

    public static void spawn(@NonNull Location loc, @NonNull Particle particle, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        spawn(loc, particle, count, offsetX, offsetY, offsetZ, extra, null);
    }

    public static <T> void spawn(@NonNull Location loc, @NonNull Particle particle, int count, double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data) {
        if (loc.getWorld() == null) return;
        loc.getWorld().spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, extra, data);
    }

    public static void spawnDust(@NonNull Location loc, @NonNull Color color, float size) {
        spawnDust(loc, color, size, 1);
    }

    public static void spawnDust(@NonNull Location loc, @NonNull Color color, float size, int count) {
        spawn(loc, Particle.DUST, count, 0, 0, 0, 1, new Particle.DustOptions(color, size));
    }

    public static void spawnDust(@NonNull Location loc, @NonNull Color color, float size, int count, double offsetX, double offsetY, double offsetZ) {
        spawn(loc, Particle.DUST, count, offsetX, offsetY, offsetZ, 1, new Particle.DustOptions(color, size));
    }

    public static void spawnDustTransition(@NonNull Location loc, @NonNull Color fromColor, @NonNull Color toColor, float size) {
        spawnDustTransition(loc, fromColor, toColor, size, 1);
    }

    public static void spawnDustTransition(@NonNull Location loc, @NonNull Color fromColor, @NonNull Color toColor, float size, int count) {
        spawn(loc, Particle.DUST_COLOR_TRANSITION, count, 0, 0, 0, 1, new Particle.DustTransition(fromColor, toColor, size));
    }

    public static void spawnDustTransition(@NonNull Location loc, @NonNull Color fromColor, @NonNull Color toColor, float size, int count, double offsetX, double offsetY, double offsetZ) {
        spawn(loc, Particle.DUST_COLOR_TRANSITION, count, offsetX, offsetY, offsetZ, 1, new Particle.DustTransition(fromColor, toColor, size));
    }
}