package dev.oum.oumlib.event;

import dev.oum.oumlib.event.platform.EventBusAdapter;
import org.jspecify.annotations.NonNull;

public final class EventBus {

    private static EventBusAdapter adapter;

    private EventBus() {
    }

    public static void initialize(EventBusAdapter a) {
        adapter = a;
    }

    public static <E> @NonNull ListenerHandle register(@NonNull EventBuilder<E> builder) {
        if (adapter == null) throw new IllegalStateException("EventBus not initialized.");
        return adapter.register(builder);
    }
}
