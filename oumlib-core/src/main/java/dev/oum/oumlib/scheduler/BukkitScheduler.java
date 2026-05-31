package dev.oum.oumlib.scheduler;

import dev.oum.oumlib.scheduler.platform.BukkitSchedulerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public final class BukkitScheduler {

    private BukkitScheduler() {
    }

    @Contract("_, _ -> new")
    public static @NonNull TaskHandle runAt(Location location, Runnable task) {
        return BukkitSchedulerAdapter.get().runAt(location, task);
    }

    @Contract("_, _ -> new")
    public static @NonNull TaskHandle runFor(Entity entity, Runnable task) {
        return BukkitSchedulerAdapter.get().runFor(entity, task);
    }

    public static void assertMainThread() {
        if (!isMainThread()) throw new IllegalStateException("Must be called on the main thread.");
    }

    public static void assertAsync() {
        if (isMainThread()) throw new IllegalStateException("Must not be called on the main thread.");
    }

    public static boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }
}
