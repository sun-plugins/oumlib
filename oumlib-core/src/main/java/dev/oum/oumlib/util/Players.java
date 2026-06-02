package dev.oum.oumlib.util;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class Players {

    private Players() {
    }

    /**
     * Gets the exact block the player is looking at within a maximum distance.
     */
    public static @Nullable Block getTargetBlock(@NonNull Player player, int maxDistance) {
        RayTraceResult result = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(),
                player.getLocation().getDirection(),
                maxDistance
        );
        return result != null ? result.getHitBlock() : null;
    }

    /**
     * Gets the entity the player is targeting within a maximum distance.
     */
    public static @Nullable Entity getTargetEntity(@NonNull Player player, int maxDistance) {
        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getLocation().getDirection(),
                maxDistance,
                entity -> !entity.equals(player)
        );
        return result != null ? result.getHitEntity() : null;
    }
}
