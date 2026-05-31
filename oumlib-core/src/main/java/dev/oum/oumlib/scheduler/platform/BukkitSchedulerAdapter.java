package dev.oum.oumlib.scheduler.platform;

import dev.oum.oumlib.scheduler.TaskHandle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.time.Duration;

public final class BukkitSchedulerAdapter implements SchedulerAdapter {

    private static BukkitSchedulerAdapter instance;
    private final Plugin plugin;
    private final BukkitScheduler scheduler;

    private static final boolean FOLIA;
    static {
        boolean folia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionScheduler");
            folia = true;
        } catch (ClassNotFoundException ignored) {}
        FOLIA = folia;
    }

    private BukkitSchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = Bukkit.getScheduler();
    }

    public static void initialize(Plugin plugin) {
        instance = new BukkitSchedulerAdapter(plugin);
    }

    public static BukkitSchedulerAdapter get() {
        if (instance == null) throw new IllegalStateException("BukkitSchedulerAdapter not initialized.");
        return instance;
    }

    @Contract("_ -> new")
    @Override
    public @NonNull TaskHandle run(Runnable task) {
        if (FOLIA) {
            var scheduled = Bukkit.getGlobalRegionScheduler().run(plugin, t -> task.run());
            return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
        }
        var scheduled = scheduler.runTask(plugin, task);
        return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
    }

    @Contract("_, _ -> new")
    @Override
    public @NonNull TaskHandle runLater(Duration delay, Runnable task) {
        if (FOLIA) {
            var scheduled = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> task.run(), toTicks(delay));
            return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
        }
        var scheduled = scheduler.runTaskLater(plugin, task, toTicks(delay));
        return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
    }

    @Contract("_, _, _ -> new")
    @Override
    public @NonNull TaskHandle runRepeating(Duration initialDelay, Duration interval, Runnable task) {
        if (FOLIA) {
            var scheduled = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> task.run(), toTicks(initialDelay), toTicks(interval));
            return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
        }
        var scheduled = scheduler.runTaskTimer(plugin, task, toTicks(initialDelay), toTicks(interval));
        return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
    }

    @Contract("_ -> new")
    @Override
    public @NonNull TaskHandle runAsync(Runnable task) {
        if (FOLIA) {
            var scheduled = Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run());
            return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
        }
        var scheduled = scheduler.runTaskAsynchronously(plugin, task);
        return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
    }

    @Contract("_, _ -> new")
    @Override
    public @NonNull TaskHandle runAt(Object location, Runnable task) {
        if (location instanceof Location loc) {
            if (FOLIA) {
                var scheduled = Bukkit.getRegionScheduler().run(plugin, loc, t -> task.run());
                return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
            }
        }
        return run(task);
    }

    @Contract("_, _ -> new")
    @Override
    public @NonNull TaskHandle runFor(Object entity, Runnable task) {
        if (entity instanceof Entity ent) {
            if (FOLIA) {
                var scheduled = ent.getScheduler().run(plugin, t -> task.run(), null);
                assert scheduled != null;
                return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
            }
        }
        return run(task);
    }

    private long toTicks(@NonNull Duration duration) {
        return Math.max(1L, duration.toMillis() / 50L);
    }
}