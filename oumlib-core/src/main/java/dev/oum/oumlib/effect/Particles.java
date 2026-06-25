package dev.oum.oumlib.effect;

import dev.oum.oumlib.math.Geometry3D;
import dev.oum.oumlib.math.Vector3D;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

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

    public static void spawnLine(@NonNull Location start, @NonNull Location end,
                                 @NonNull ParticleBuilder builder, int segments) {
        Vector3D startV = Vector3D.fromLocation(start);
        Vector3D endV = Vector3D.fromLocation(end);
        World world = start.getWorld();
        if (world == null) return;
        for (int i = 0; i <= segments; i++) {
            Vector3D pt = startV.lerp(endV, (double) i / segments);
            builder.spawn(pt.toLocation(world));
        }
    }

    public static void spawnBezier(@NonNull Location start, @NonNull Location control1,
                                   @NonNull Location control2, @NonNull Location end,
                                   @NonNull ParticleBuilder builder, int segments) {
        Vector3D startV = Vector3D.fromLocation(start);
        Vector3D c1 = Vector3D.fromLocation(control1);
        Vector3D c2 = Vector3D.fromLocation(control2);
        Vector3D endV = Vector3D.fromLocation(end);
        List<Vector3D> points = Geometry3D.bezier(startV, c1, c2, endV, segments);
        World world = start.getWorld();
        if (world == null) return;
        for (Vector3D pt : points) {
            builder.spawn(pt.toLocation(world));
        }
    }

    public static void spawnHelix(@NonNull Location center, double radius,
                                  double pitch, double height, int steps,
                                  @NonNull ParticleBuilder builder) {
        Vector3D centerV = Vector3D.fromLocation(center);
        List<Vector3D> points = Geometry3D.helix(centerV, radius, pitch, height, steps);
        World world = center.getWorld();
        if (world == null) return;
        for (Vector3D pt : points) {
            builder.spawn(pt.toLocation(world));
        }
    }
}