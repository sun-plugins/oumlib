package dev.oum.oumlib.event;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public final class Events {

    private Events() {
    }

    @Contract("_ -> new")
    public static <E> @NonNull EventBuilder<E> listen(Class<E> type) {
        return new EventBuilder<>(type);
    }

    public static <E> @NonNull ListenerHandle listen(Class<E> type, Consumer<E> handler) {
        return new EventBuilder<>(type).handler(handler);
    }

    public static <E> @NonNull ListenerHandle listenOnce(Class<E> type, Consumer<E> handler) {
        return new EventBuilder<>(type).maxFires(1).handler(handler);
    }
}