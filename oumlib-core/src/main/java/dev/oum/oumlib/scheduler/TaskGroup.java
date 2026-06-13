package dev.oum.oumlib.scheduler;

import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class TaskGroup {

    private final List<TaskHandle> handles = new ArrayList<>();
    private boolean managed = true;

    void add(TaskHandle handle) {
        handles.add(handle);
    }

    public @NonNull TaskHandle run(Runnable task) {
        TaskHandle handle = Scheduler.run(task);
        handles.add(handle);
        return handle;
    }

    public @NonNull TaskHandle runLater(Duration delay, Runnable task) {
        TaskHandle handle = Scheduler.runLater(delay, task);
        handles.add(handle);
        return handle;
    }

    public @NonNull TaskHandle runLater(long ticks, Runnable task) {
        TaskHandle handle = Scheduler.runLater(ticks, task);
        handles.add(handle);
        return handle;
    }

    public @NonNull TaskHandle runRepeating(Duration initialDelay, Duration interval, Runnable task) {
        TaskHandle handle = Scheduler.runRepeating(initialDelay, interval, task);
        handles.add(handle);
        return handle;
    }

    public @NonNull TaskHandle runRepeating(long initialTicks, long periodTicks, Runnable task) {
        TaskHandle handle = Scheduler.runRepeating(initialTicks, periodTicks, task);
        handles.add(handle);
        return handle;
    }

    public @NonNull TaskHandle runAsync(Runnable task) {
        TaskHandle handle = Scheduler.runAsync(task);
        handles.add(handle);
        return handle;
    }

    public @NonNull TaskHandle runAt(Object location, Runnable task) {
        TaskHandle handle = Scheduler.runAt(location, task);
        handles.add(handle);
        return handle;
    }

    public @NonNull TaskHandle runFor(Object entity, Runnable task) {
        TaskHandle handle = Scheduler.runFor(entity, task);
        handles.add(handle);
        return handle;
    }

    public void cancelAll() {
        handles.forEach(TaskHandle::cancel);
        handles.clear();
    }

    public TaskGroup unmanaged() {
        this.managed = false;
        return this;
    }

    boolean isManaged() {
        return managed;
    }
}