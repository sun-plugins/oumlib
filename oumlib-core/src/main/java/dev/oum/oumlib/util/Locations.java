package dev.oum.oumlib.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public final class Locations {

    private Locations() {
    }

    @Contract(pure = true)
    public static @NonNull String serialize(@NonNull Location loc) {
        return (loc.getWorld() != null ? loc.getWorld().getName() : "world") + "," +
                loc.getX() + "," +
                loc.getY() + "," +
                loc.getZ() + "," +
                loc.getYaw() + "," +
                loc.getPitch();
    }

    @Contract(pure = true)
    public static @NonNull String serializeBlock(@NonNull Location loc) {
        return (loc.getWorld() != null ? loc.getWorld().getName() : "world") + "," +
                loc.getBlockX() + "," +
                loc.getBlockY() + "," +
                loc.getBlockZ();
    }

    @Contract(pure = true)
    public static @Nullable Location deserialize(@NonNull String str) {
        String[] parts = str.split(",");
        if (parts.length < 4) return null;

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;

        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0f;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0f;
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Contract(pure = true)
    public static @NonNull String serializeRegion(@NonNull Location loc) {
        return serialize(loc) + "," + (loc.getBlockX() >> 4) + "," + (loc.getBlockZ() >> 4);
    }

    @Contract(pure = true)
    public static @NonNull String serializeVector(@NonNull Vector vector) {
        return vector.getX() + "," + vector.getY() + "," + vector.getZ();
    }

    @Contract(pure = true)
    public static @Nullable Vector deserializeVector(@NonNull String str) {
        String[] parts = str.split(",");
        if (parts.length < 3) return null;
        try {
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
            return new Vector(x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static double distance2D(@NonNull Location a, @NonNull Location b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static @NonNull Location midpoint(@NonNull Location a, @NonNull Location b) {
        return new Location(
                a.getWorld(),
                (a.getX() + b.getX()) / 2.0,
                (a.getY() + b.getY()) / 2.0,
                (a.getZ() + b.getZ()) / 2.0,
                (a.getYaw() + b.getYaw()) / 2.0f,
                (a.getPitch() + b.getPitch()) / 2.0f
        );
    }

    public static @NonNull Location centerBlock(@NonNull Location block) {
        return new Location(
                block.getWorld(),
                block.getBlockX() + 0.5,
                block.getBlockY() + 0.5,
                block.getBlockZ() + 0.5,
                block.getYaw(),
                block.getPitch()
        );
    }

    public static boolean isWithinAABB(@NonNull Location loc, @NonNull Location min, @NonNull Location max) {
        double minX = Math.min(min.getX(), max.getX());
        double minY = Math.min(min.getY(), max.getY());
        double minZ = Math.min(min.getZ(), max.getZ());
        double maxX = Math.max(min.getX(), max.getX());
        double maxY = Math.max(min.getY(), max.getY());
        double maxZ = Math.max(min.getZ(), max.getZ());

        return loc.getX() >= minX && loc.getX() <= maxX
                && loc.getY() >= minY && loc.getY() <= maxY
                && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

    public static @NonNull Location randomInRadius(@NonNull Location center, double radius) {
        double angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
        double r = ThreadLocalRandom.current().nextDouble() * radius;
        double x = center.getX() + r * Math.cos(angle);
        double z = center.getZ() + r * Math.sin(angle);
        return new Location(center.getWorld(), x, center.getY(), z, center.getYaw(), center.getPitch());
    }
}
