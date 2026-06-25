package dev.oum.oumlib.scheduler.platform;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.Scheduler;
import com.velocitypowered.api.scheduler.TaskStatus;
import dev.oum.oumlib.scheduler.TaskHandle;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class VelocitySchedulerAdapter implements SchedulerAdapter {

    private static VelocitySchedulerAdapter instance;
    private final Object plugin;
    private final Scheduler scheduler;

    private VelocitySchedulerAdapter(@NonNull ProxyServer server, Object plugin) {
        this.plugin = plugin;
        this.scheduler = server.getScheduler();
    }

    public static void initialize(ProxyServer server, Object plugin) {
        instance = new VelocitySchedulerAdapter(server, plugin);
    }

    public static VelocitySchedulerAdapter get() {
        if (instance == null) throw new IllegalStateException("VelocitySchedulerAdapter not initialized.");
        return instance;
    }

    @Contract("_ -> new")
    @Override
    public @NonNull TaskHandle run(Runnable task) {
        var scheduled = scheduler.buildTask(plugin, task).schedule();
        return new TaskHandle(scheduled::cancel, () -> scheduled.status() == TaskStatus.CANCELLED);
    }

    @Contract("_, _ -> new")
    @Override
    public @NonNull TaskHandle runLater(@NonNull Duration delay, Runnable task) {
        var scheduled = scheduler.buildTask(plugin, task)
                .delay(delay.toMillis(), TimeUnit.MILLISECONDS)
                .schedule();
        return new TaskHandle(scheduled::cancel, () -> scheduled.status() == TaskStatus.CANCELLED);
    }

    @Contract("_, _ -> new")
    @Override
    public @NonNull TaskHandle runLater(long ticks, Runnable task) {
        return runLater(Duration.ofMillis(ticks * 50L), task);
    }

    @Contract("_, _, _ -> new")
    @Override
    public @NonNull TaskHandle runRepeating(@NonNull Duration initialDelay, @NonNull Duration interval, Runnable task) {
        var scheduled = scheduler.buildTask(plugin, task)
                .delay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
                .repeat(interval.toMillis(), TimeUnit.MILLISECONDS)
                .schedule();
        return new TaskHandle(scheduled::cancel, () -> scheduled.status() == TaskStatus.CANCELLED);
    }

    @Contract("_, _, _ -> new")
    @Override
    public @NonNull TaskHandle runRepeating(long initialTicks, long periodTicks, Runnable task) {
        return runRepeating(Duration.ofMillis(initialTicks * 50L), Duration.ofMillis(periodTicks * 50L), task);
    }

    @Contract("_ -> new")
    @Override
    public @NonNull TaskHandle runAsync(Runnable task) {
        return run(task);
    }

    @Contract("_, _ -> fail")
    @Override
    public @NonNull TaskHandle runAt(Object location, Runnable task) {
        throw new UnsupportedOperationException("Folia regional scheduling is not supported on Velocity.");
    }

    @Contract("_, _, _ -> fail")
    @Override
    public @NonNull TaskHandle runLaterAt(Object location, Duration delay, Runnable task) {
        throw new UnsupportedOperationException("Folia regional scheduling is not supported on Velocity.");
    }

    @Contract("_, _, _ -> fail")
    @Override
    public @NonNull TaskHandle runLaterAt(Object location, long ticks, Runnable task) {
        throw new UnsupportedOperationException("Folia regional scheduling is not supported on Velocity.");
    }

    @Contract("_, _, _, _ -> fail")
    @Override
    public @NonNull TaskHandle runRepeatingAt(Object location, Duration initialDelay, Duration period, Runnable task) {
        throw new UnsupportedOperationException("Folia regional scheduling is not supported on Velocity.");
    }

    @Contract("_, _, _, _ -> fail")
    @Override
    public @NonNull TaskHandle runRepeatingAt(Object location, long initialTicks, long periodTicks, Runnable task) {
        throw new UnsupportedOperationException("Folia regional scheduling is not supported on Velocity.");
    }

    @Contract("_, _ -> fail")
    @Override
    public @NonNull TaskHandle runFor(Object entity, Runnable task) {
        throw new UnsupportedOperationException("Entity scheduling is not supported on Velocity.");
    }

    @Contract("_, _, _, _ -> fail")
    @Override
    public @NonNull TaskHandle runLaterFor(Object entity, Duration delay, Runnable task, Runnable retired) {
        throw new UnsupportedOperationException("Entity scheduling is not supported on Velocity.");
    }

    @Contract("_, _, _, _ -> fail")
    @Override
    public @NonNull TaskHandle runLaterFor(Object entity, long ticks, Runnable task, Runnable retired) {
        throw new UnsupportedOperationException("Entity scheduling is not supported on Velocity.");
    }

    @Contract("_, _, _, _, _ -> fail")
    @Override
    public @NonNull TaskHandle runRepeatingFor(Object entity, Duration initialDelay, Duration period, Runnable task, Runnable retired) {
        throw new UnsupportedOperationException("Entity scheduling is not supported on Velocity.");
    }

    @Contract("_, _, _, _, _ -> fail")
    @Override
    public @NonNull TaskHandle runRepeatingFor(Object entity, long initialTicks, long periodTicks, Runnable task, Runnable retired) {
        throw new UnsupportedOperationException("Entity scheduling is not supported on Velocity.");
    }
}
