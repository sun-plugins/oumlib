package dev.oum.oumlib.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
}
