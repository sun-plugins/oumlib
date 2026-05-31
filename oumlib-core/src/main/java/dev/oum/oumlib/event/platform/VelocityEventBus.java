package dev.oum.oumlib.event.platform;

import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.oum.oumlib.event.EventBuilder;
import dev.oum.oumlib.event.EventPriority;
import dev.oum.oumlib.event.ListenerHandle;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public final class VelocityEventBus implements EventBusAdapter {

    private static VelocityEventBus instance;
    private final Object plugin;
    private final ProxyServer server;

    private VelocityEventBus(ProxyServer server, Object plugin) {
        this.server = server;
        this.plugin = plugin;
    }

    public static void initialize(ProxyServer server, Object plugin) {
        instance = new VelocityEventBus(server, plugin);
    }

    public static VelocityEventBus get() {
        if (instance == null) throw new IllegalStateException("VelocityEventBus not initialized.");
        return instance;
    }

    @Override
    public <E> @NonNull ListenerHandle register(@NonNull EventBuilder<E> builder) {
        AtomicInteger fireCount = new AtomicInteger();
        Instant expiry = builder.expireAfter() != null ? Instant.now().plus(builder.expireAfter()) : null;

        EventHandler<E> finalHandler = event -> {
            if (expiry != null && Instant.now().isAfter(expiry)) {
                return;
            }

            if (builder.shouldIgnoreCancelled() && isCancelled(event)) return;
            if (builder.shouldOnlyIfCancelled() && !isCancelled(event)) return;

            for (var filter : builder.filters()) {
                if (!filter.test(event)) return;
            }

            builder.handler().accept(event);

            if (builder.maxFires() > 0) {
                fireCount.incrementAndGet();
            }
        };

        server.getEventManager().register(
                plugin,
                builder.type(),
                toPriority(builder.priority()),
                finalHandler
        );

        return new ListenerHandle(() -> server.getEventManager().unregister(plugin, finalHandler));
    }

    private static boolean isCancelled(Object event) {
        if (event instanceof ResultedEvent<?> resulted) {
            Object result = resulted.getResult();
            if (result instanceof ResultedEvent.Result res) {
                return !res.isAllowed();
            }
        }
        return false;
    }

    private static short toPriority(@NonNull EventPriority priority) {
        return switch (priority) {
            case LOWEST -> (short) (Short.MAX_VALUE - 1);
            case LOW -> (short) (Short.MAX_VALUE / 2);
            case NORMAL -> 0;
            case HIGH -> (short) (Short.MIN_VALUE / 2);
            case HIGHEST, MONITOR -> (short) (Short.MIN_VALUE + 1);
        };
    }
}
