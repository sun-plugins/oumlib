package dev.oum.oumlib.scheduler.platform;

import dev.oum.oumlib.scheduler.TaskHandle;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.time.Duration;

public interface SchedulerAdapter {

    @Contract("_ -> new")
    @NonNull TaskHandle run(Runnable task);

    @Contract("_, _ -> new")
    @NonNull TaskHandle runLater(Duration delay, Runnable task);

    @Contract("_, _ -> new")
    @NonNull TaskHandle runLater(long ticks, Runnable task);

    @Contract("_, _, _ -> new")
    @NonNull TaskHandle runRepeating(Duration initialDelay, Duration interval, Runnable task);

    @Contract("_, _, _ -> new")
    @NonNull TaskHandle runRepeating(long initialTicks, long periodTicks, Runnable task);

    @Contract("_ -> new")
    @NonNull TaskHandle runAsync(Runnable task);

    @Contract("_, _ -> new")
    @NonNull TaskHandle runAt(Object location, Runnable task);

    @Contract("_, _, _ -> new")
    @NonNull TaskHandle runLaterAt(Object location, Duration delay, Runnable task);

    @Contract("_, _, _ -> new")
    @NonNull TaskHandle runLaterAt(Object location, long ticks, Runnable task);

    @Contract("_, _, _, _ -> new")
    @NonNull TaskHandle runRepeatingAt(Object location, Duration initialDelay, Duration period, Runnable task);

    @Contract("_, _, _, _ -> new")
    @NonNull TaskHandle runRepeatingAt(Object location, long initialTicks, long periodTicks, Runnable task);

    @Contract("_, _ -> new")
    @NonNull TaskHandle runFor(Object entity, Runnable task);

    @Contract("_, _, _, _ -> new")
    @NonNull TaskHandle runLaterFor(Object entity, Duration delay, Runnable task, Runnable retired);

    @Contract("_, _, _, _ -> new")
    @NonNull TaskHandle runLaterFor(Object entity, long ticks, Runnable task, Runnable retired);

    @Contract("_, _, _, _, _ -> new")
    @NonNull TaskHandle runRepeatingFor(Object entity, Duration initialDelay, Duration period, Runnable task, Runnable retired);

    @Contract("_, _, _, _, _ -> new")
    @NonNull TaskHandle runRepeatingFor(Object entity, long initialTicks, long periodTicks, Runnable task, Runnable retired);
}