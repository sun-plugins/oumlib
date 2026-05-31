package dev.oum.oumlib.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class Locations {

    private Locations() {}

    /**
     * Serializes a Location to a standard comma-separated String.
     * Format: world,x,y,z,yaw,pitch
     */
    public static @NonNull String serialize(@NonNull Location loc) {
        return (loc.getWorld() != null ? loc.getWorld().getName() : "world") + "," +
               loc.getX() + "," +
               loc.getY() + "," +
               loc.getZ() + "," +
               loc.getYaw() + "," +
               loc.getPitch();
    }

    /**
     * Serializes a Location to a block-only format (no yaw/pitch).
     * Format: world,x,y,z
     */
    public static @NonNull String serializeBlock(@NonNull Location loc) {
        return (loc.getWorld() != null ? loc.getWorld().getName() : "world") + "," +
               loc.getBlockX() + "," +
               loc.getBlockY() + "," +
               loc.getBlockZ();
    }

    /**
     * Deserializes a Location from a comma-separated String.
     */
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
}
