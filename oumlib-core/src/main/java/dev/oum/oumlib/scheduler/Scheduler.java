package dev.oum.oumlib.scheduler;

import dev.oum.oumlib.scheduler.platform.SchedulerAdapter;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    @Contract("_, _, _ -> new")
    public static @NonNull TaskHandle runRepeating(Duration initialDelay, Duration interval, Runnable task) {
        return adapter().runRepeating(initialDelay, interval, task);
    }

    @Contract("_ -> new")
    public static @NonNull TaskHandle runAsync(Runnable task) {
        return adapter().runAsync(task);
    }

    public static void runVirtual(Runnable task) {
        Thread.ofVirtual().start(task);
    }

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