package dev.oum.oumlib.scheduler;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Promise<T> {

    private final CompletableFuture<T> future;

    private Promise(CompletableFuture<T> future) {
        this.future = future;
    }

    public static <U> @NonNull Promise<U> fromCompletableFuture(@NonNull CompletableFuture<U> future) {
        return new Promise<>(future);
    }

    @Contract("_ -> new")
    public static <U> @NonNull Promise<U> supplyAsync(@NonNull Supplier<U> supplier) {
        CompletableFuture<U> fut = new CompletableFuture<>();
        Scheduler.runAsync(() -> {
            try {
                fut.complete(supplier.get());
            } catch (Throwable t) {
                fut.completeExceptionally(t);
            }
        });
        return new Promise<>(fut);
    }

    public static @NonNull Promise<Void> runAsync(@NonNull Runnable runnable) {
        CompletableFuture<Void> fut = new CompletableFuture<>();
        Scheduler.runAsync(() -> {
            try {
                runnable.run();
                fut.complete(null);
            } catch (Throwable t) {
                fut.completeExceptionally(t);
            }
        });
        return new Promise<>(fut);
    }

    public static <U> @NonNull Promise<U> supplyVirtual(@NonNull Supplier<U> supplier) {
        CompletableFuture<U> fut = new CompletableFuture<>();
        Thread.ofVirtual().start(() -> {
            try {
                fut.complete(supplier.get());
            } catch (Throwable t) {
                fut.completeExceptionally(t);
            }
        });
        return new Promise<>(fut);
    }

    public static @NonNull Promise<Void> runVirtual(@NonNull Runnable runnable) {
        CompletableFuture<Void> fut = new CompletableFuture<>();
        Thread.ofVirtual().start(() -> {
            try {
                runnable.run();
                fut.complete(null);
            } catch (Throwable t) {
                fut.completeExceptionally(t);
            }
        });
        return new Promise<>(fut);
    }

    public <U> @NonNull Promise<U> map(@NonNull Function<T, U> mapper) {
        return new Promise<>(future.thenApply(mapper));
    }

    public <U> @NonNull Promise<U> mapSync(@NonNull Function<T, U> mapper) {
        CompletableFuture<U> next = new CompletableFuture<>();
        future.whenComplete((val, err) -> {
            if (err != null) {
                next.completeExceptionally(err);
            } else {
                Scheduler.run(() -> {
                    try {
                        next.complete(mapper.apply(val));
                    } catch (Throwable t) {
                        next.completeExceptionally(t);
                    }
                });
            }
        });
        return new Promise<>(next);
    }

    public @NonNull Promise<Void> thenAccept(@NonNull Consumer<T> action) {
        return new Promise<>(future.thenAccept(action));
    }

    public @NonNull Promise<Void> thenAcceptSync(@NonNull Consumer<T> action) {
        CompletableFuture<Void> next = new CompletableFuture<>();
        future.whenComplete((val, err) -> {
            if (err != null) {
                next.completeExceptionally(err);
            } else {
                Scheduler.run(() -> {
                    try {
                        action.accept(val);
                        next.complete(null);
                    } catch (Throwable t) {
                        next.completeExceptionally(t);
                    }
                });
            }
        });
        return new Promise<>(next);
    }

    public @NonNull Promise<T> whenComplete(@Nullable Consumer<T> success, @Nullable Consumer<Throwable> failure) {
        future.whenComplete((val, err) -> {
            if (err != null) {
                if (failure != null) failure.accept(err);
            } else {
                if (success != null) success.accept(val);
            }
        });
        return this;
    }

    public @NonNull Promise<T> whenCompleteSync(@Nullable Consumer<T> success, @Nullable Consumer<Throwable> failure) {
        future.whenComplete((val, err) -> {
            Scheduler.run(() -> {
                if (err != null) {
                    if (failure != null) failure.accept(err);
                } else {
                    if (success != null) success.accept(val);
                }
            });
        });
        return this;
    }

    public @NonNull CompletableFuture<T> toCompletableFuture() {
        return future;
    }
}
