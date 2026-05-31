package dev.oum.oumlib.event.platform;

import dev.oum.oumlib.event.EventBuilder;
import dev.oum.oumlib.event.ListenerHandle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public final class PaperEventBus implements EventBusAdapter {

    private static PaperEventBus instance;
    private final Plugin plugin;

    private PaperEventBus(Plugin p) {
        this.plugin = p;
    }

    public static void initialize(Plugin p) {
        instance = new PaperEventBus(p);
    }

    public static PaperEventBus get() {
        if (instance == null) throw new IllegalStateException("PaperEventBus not initialized.");
        return instance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> @NonNull ListenerHandle register(@NonNull EventBuilder<E> builder) {
        Class<? extends Event> eventClass = (Class<? extends Event>) builder.type();
        AtomicInteger fireCount = new AtomicInteger();
        Instant expiry = builder.expireAfter() != null ? Instant.now().plus(builder.expireAfter()) : null;

        Listener listener = new Listener() {};

        ListenerHandle handle = new ListenerHandle(() -> org.bukkit.event.HandlerList.unregisterAll(listener));

        plugin.getServer().getPluginManager().registerEvent(
                eventClass,
                listener,
                toBukkitPriority(builder.priority()),
                (l, event) -> {
                    if (!eventClass.isInstance(event)) return;
                    if (!handle.isActive()) return;
                    if (expiry != null && Instant.now().isAfter(expiry)) {
                        handle.unregister();
                        return;
                    }

                    E e = builder.type().cast(event);

                    if (builder.shouldIgnoreCancelled() && event instanceof Cancellable c && c.isCancelled()) return;
                    if (builder.shouldOnlyIfCancelled() && event instanceof Cancellable c && !c.isCancelled()) return;

                    for (var filter : builder.filters()) {
                        if (!filter.test(e)) return;
                    }

                    builder.handler().accept(e);

                    if (builder.maxFires() > 0 && fireCount.incrementAndGet() >= builder.maxFires()) {
                        handle.unregister();
                    }
                },
                plugin,
                false
        );

        return handle;
    }

    @Contract(pure = true)
    private static org.bukkit.event.EventPriority toBukkitPriority(dev.oum.oumlib.event.@NonNull EventPriority priority) {
        return switch (priority) {
            case LOWEST -> org.bukkit.event.EventPriority.LOWEST;
            case LOW -> org.bukkit.event.EventPriority.LOW;
            case NORMAL -> org.bukkit.event.EventPriority.NORMAL;
            case HIGH -> org.bukkit.event.EventPriority.HIGH;
            case HIGHEST -> org.bukkit.event.EventPriority.HIGHEST;
            case MONITOR -> org.bukkit.event.EventPriority.MONITOR;
        };
    }
}