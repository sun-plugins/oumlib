package dev.oum.oumlib.scheduler;

import dev.oum.oumlib.scheduler.platform.SchedulerAdapter;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class Scheduler {

    private static final Set<TaskGroup> managedGroups = ConcurrentHashMap.newKeySet();
    private static SchedulerAdapter adapter;

    private Scheduler() {
    }

    public static void initialize(SchedulerAdapter a) {
        adapter = a;
    }

    private static SchedulerAdapter adapter() {
        if (adapter == null) throw new IllegalStateException("Scheduler not initialized.");
        return adapter;
    }

    @Contract("_ -> new")
    public static @NonNull TaskHandle run(Runnable task) {
        return adapter().run(task);
    }

    @Contract("_, _ -> new")
    public static @NonNull TaskHandle runLater(Duration delay, Runnable task) {
        return adapter().runLater(delay, task);
    }

    @Contract("_, _ -> new")
    public static @NonNull TaskHandle runLater(long ticks, Runnable task) {
        return adapter().runLater(ticks, task);
    }

    @Contract("_, _ -> new")
    @Deprecated(since = "1.0.4", forRemoval = true)
    public static @NonNull TaskHandle runDelayed(Duration delay, Runnable task) {
        return runLater(delay, task);
    }

    @Contract("_, _ -> new")
    @Deprecated(since = "1.0.4", forRemoval = true)
    public static @NonNull TaskHandle runDelayed(long ticks, Runnable task) {
        return runLater(ticks, task);
    }

    @Contract("_, _, _ -> new")
    public static @NonNull TaskHandle runRepeating(Duration initialDelay, Duration interval, Runnable task) {
        return adapter().runRepeating(initialDelay, interval, task);
    }

    @Contract("_, _, _ -> new")
    public static @NonNull TaskHandle runRepeating(long initialTicks, long periodTicks, Runnable task) {
        return adapter().runRepeating(initialTicks, periodTicks, task);
    }

    @Contract("_ -> new")
    public static @NonNull TaskHandle runAsync(Runnable task) {
        return adapter().runAsync(task);
    }

    @CheckReturnValue
    public static <T> @NonNull Promise<T> supplyAsync(@NonNull Supplier<T> supplier) {
        return Promise.supplyAsync(supplier);
    }

    public static void runVirtual(Runnable task) {
        Thread.ofVirtual().start(task);
    }

    @CheckReturnValue
    public static <T> @NonNull Promise<T> supplyVirtual(@NonNull Supplier<T> supplier) {
        return Promise.supplyVirtual(supplier);
    }

    @CheckReturnValue
    public static @NonNull Promise<Void> runVirtualAsync(@NonNull Runnable task) {
        return Promise.runVirtual(task);
    }

    @Contract("_, _ -> new")
    public static @NonNull TaskHandle runAt(Object location, Runnable task) {
        return adapter().runAt(location, task);
    }

    @Contract("_, _ -> new")
    public static @NonNull TaskHandle runFor(Object entity, Runnable task) {
        return adapter().runFor(entity, task);
    }

    @CheckReturnValue
    public static <T> @NonNull TaskChain<T> chain() {
        return TaskChain.create();
    }

    @CheckReturnValue
    public static <T> @NonNull TaskChain<T> chain(T initialValue) {
        return TaskChain.create(initialValue);
    }

    @CheckReturnValue
    public static @NonNull TaskGroup newGroup() {
        TaskGroup group = new TaskGroup();
        managedGroups.add(group);
        return group;
    }

    public static void shutdownAll() {
        managedGroups.stream().filter(TaskGroup::isManaged).forEach(TaskGroup::cancelAll);
        managedGroups.clear();
    }
}