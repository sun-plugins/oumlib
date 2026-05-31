package dev.oum.oumlib.event.platform;

import dev.oum.oumlib.event.EventBuilder;
import dev.oum.oumlib.event.ListenerHandle;
import org.jspecify.annotations.NonNull;

public interface EventBusAdapter {
    <E> @NonNull ListenerHandle register(@NonNull EventBuilder<E> builder);
}
