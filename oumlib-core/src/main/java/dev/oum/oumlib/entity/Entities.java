package dev.oum.oumlib.entity;

import dev.oum.oumlib.scheduler.Promise;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class Entities {

    private Entities() {
    }

    public static @Nullable Block getTargetBlock(@NonNull Player player, int maxDistance) {
        return getTargetBlock(player, maxDistance, FluidCollisionMode.NEVER);
    }

    public static @Nullable Block getTargetBlock(@NonNull Player player, int maxDistance,
                                                 @NonNull FluidCollisionMode fluidMode) {
        RayTraceResult result = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(),
                player.getLocation().getDirection(),
                maxDistance,
                fluidMode
        );
        return result != null ? result.getHitBlock() : null;
    }

    public static @Nullable Entity getTargetEntity(@NonNull Player player, int maxDistance) {
        return getTargetEntity(player, maxDistance, entity -> !entity.equals(player));
    }

    public static @Nullable Entity getTargetEntity(@NonNull Player player, int maxDistance,
                                                   @NonNull Predicate<Entity> filter) {
        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getLocation().getDirection(),
                maxDistance,
                filter
        );
        return result != null ? result.getHitEntity() : null;
    }

    public static @Nullable RayTraceResult rayTrace(@NonNull Location origin, @NonNull Vector direction,
                                                    double maxDistance, @NonNull Predicate<Entity> filter) {
        if (origin.getWorld() == null) return null;
        return origin.getWorld().rayTraceEntities(origin, direction, maxDistance, filter);
    }

    public static @NonNull List<Player> nearbyPlayers(@NonNull Location location, double radius) {
        return nearbyPlayers(location, radius, p -> true);
    }

    public static @NonNull List<Player> nearbyPlayers(@NonNull Location location, double radius,
                                                      @NonNull Predicate<Player> filter) {
        if (location.getWorld() == null) return List.of();
        return location.getWorld().getNearbyPlayers(location, radius).stream()
                .filter(filter)
                .toList();
    }

    public static <T extends Entity> @NonNull List<T> nearbyEntities(@NonNull Location location, double radius,
                                                                     @NonNull Class<T> type) {
        return nearbyEntities(location, radius, type, e -> true);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> @NonNull List<T> nearbyEntities(@NonNull Location location, double radius,
                                                                     @NonNull Class<T> type,
                                                                     @NonNull Predicate<T> filter) {
        if (location.getWorld() == null) return List.of();
        return location.getWorld().getNearbyEntities(location, radius, radius, radius).stream()
                .filter(type::isInstance)
                .map(e -> (T) e)
                .filter(filter)
                .toList();
    }

    public static @NonNull Optional<Player> closestPlayer(@NonNull Location location, double radius) {
        return nearbyPlayers(location, radius).stream()
                .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(location)));
    }

    public static <T extends Entity> @NonNull Optional<T> closestEntity(@NonNull Location location, double radius,
                                                                        @NonNull Class<T> type) {
        return nearbyEntities(location, radius, type).stream()
                .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(location)));
    }

    public static boolean isWithinAABB(@NonNull Entity entity, @NonNull Location min, @NonNull Location max) {
        return isWithinAABB(entity.getLocation(), min, max);
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

    public static @NonNull Promise<Boolean> teleportAsync(@NonNull Entity entity, @NonNull Location location) {
        return teleportAsync(entity, location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public static @NonNull Promise<Boolean> teleportAsync(@NonNull Entity entity, @NonNull Location location,
                                                          PlayerTeleportEvent.@NonNull TeleportCause cause) {
        return Promise.fromCompletableFuture(entity.teleportAsync(location, cause));
    }

    public static void setGlowing(@NonNull Entity entity, boolean glowing) {
        entity.setGlowing(glowing);
    }

    public static void freeze(@NonNull Entity entity) {
        entity.setFreezeTicks(entity.getMaxFreezeTicks());
    }

    public static void unfreeze(@NonNull Entity entity) {
        entity.setFreezeTicks(0);
    }

    public static void setInvulnerable(@NonNull Entity entity, boolean invulnerable) {
        entity.setInvulnerable(invulnerable);
    }

    public static void setInvisible(@NonNull LivingEntity entity, boolean invisible) {
        entity.setInvisible(invisible);
    }

    public static void removeAllEffects(@NonNull LivingEntity entity) {
        Collection<PotionEffect> effects = entity.getActivePotionEffects();
        for (PotionEffect effect : effects) {
            entity.removePotionEffect(effect.getType());
        }
    }

    public static void setMaxHealth(@NonNull LivingEntity entity, double maxHealth) {
        AttributeInstance attr = entity.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(maxHealth);
        }
    }

    public static void heal(@NonNull LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            entity.setHealth(attr.getValue());
        }
    }

    public static void heal(@NonNull LivingEntity entity, double amount) {
        AttributeInstance attr = entity.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = attr != null ? attr.getValue() : entity.getHealth();
        entity.setHealth(Math.min(entity.getHealth() + amount, maxHealth));
    }

    public static void setSpeed(@NonNull Player player, double speed) {
        player.setWalkSpeed((float) Math.clamp(speed, -1.0, 1.0));
    }

    public static void resetSpeed(@NonNull Player player) {
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
    }

    public static void resetAttributes(@NonNull LivingEntity entity) {
        for (Attribute attribute : Registry.ATTRIBUTE) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance != null) {
                instance.setBaseValue(instance.getDefaultValue());
                instance.getModifiers().forEach(instance::removeModifier);
            }
        }
    }
}
