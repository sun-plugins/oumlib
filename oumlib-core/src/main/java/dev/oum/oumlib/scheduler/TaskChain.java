package dev.oum.oumlib.scheduler;

import dev.oum.oumlib.OumLib;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Supplier;

public final class TaskChain<T> {

    private final Queue<ChainTask> tasks = new LinkedList<>();
    private Object currentValue;

    private TaskChain(@Nullable Object initialValue) {
        this.currentValue = initialValue;
    }

    @Contract(" -> new")
    public static <T> @NonNull TaskChain<T> create() {
        return new TaskChain<>(null);
    }

    @Contract("_ -> new")
    public static <T> @NonNull TaskChain<T> create(T initialValue) {
        return new TaskChain<>(initialValue);
    }

    public TaskChain<T> sync(@NonNull Runnable runnable) {
        tasks.add(new ChainTask(TaskType.SYNC, val -> {
            runnable.run();
            return val;
        }));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <R> TaskChain<R> sync(@NonNull Supplier<R> supplier) {
        tasks.add(new ChainTask(TaskType.SYNC, val -> supplier.get()));
        return (TaskChain<R>) this;
    }

    @SuppressWarnings("unchecked")
    public <R> TaskChain<R> sync(@NonNull Function<T, R> function) {
        tasks.add(new ChainTask(TaskType.SYNC, val -> function.apply((T) val)));
        return (TaskChain<R>) this;
    }

    public TaskChain<T> async(@NonNull Runnable runnable) {
        tasks.add(new ChainTask(TaskType.ASYNC, val -> {
            runnable.run();
            return val;
        }));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <R> TaskChain<R> async(@NonNull Supplier<R> supplier) {
        tasks.add(new ChainTask(TaskType.ASYNC, val -> supplier.get()));
        return (TaskChain<R>) this;
    }

    @SuppressWarnings("unchecked")
    public <R> TaskChain<R> async(@NonNull Function<T, R> function) {
        tasks.add(new ChainTask(TaskType.ASYNC, val -> function.apply((T) val)));
        return (TaskChain<R>) this;
    }

    public TaskChain<T> delay(@NonNull Duration duration) {
        tasks.add(new ChainTask(TaskType.DELAY, val -> duration));
        return this;
    }

    public TaskChain<T> delay(long ticks) {
        tasks.add(new ChainTask(TaskType.DELAY_TICKS, val -> ticks));
        return this;
    }

    public void execute() {
        executeNext();
    }

    private void executeNext() {
        ChainTask task = tasks.poll();
        if (task == null) return;

        switch (task.type) {
            case SYNC -> Scheduler.run(() -> {
                try {
                    currentValue = task.action.apply(currentValue);
                    executeNext();
                } catch (Exception e) {
                    OumLib.logError("Failed to execute, Scheduler:SYNC", e);
                }
            });
            case ASYNC -> Scheduler.runAsync(() -> {
                try {
                    currentValue = task.action.apply(currentValue);
                    executeNext();
                } catch (Exception e) {
                    OumLib.logError("Failed to execute, Scheduler:ASYNC", e);
                }
            });
            case DELAY -> {
                Duration duration = (Duration) task.action.apply(currentValue);
                Scheduler.runLater(duration, this::executeNext);
            }
            case DELAY_TICKS -> {
                long ticks = (long) task.action.apply(currentValue);
                Scheduler.runLater(ticks, this::executeNext);
            }
        }
    }

    private enum TaskType {
        SYNC, ASYNC, DELAY, DELAY_TICKS
    }

    private record ChainTask(TaskType type, Function<Object, Object> action) {
    }
}
