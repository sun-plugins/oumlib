package dev.oum.oumlib.event;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class EventBuilder<E> {

    private final Class<E> type;
    private final List<Predicate<E>> filters = new ArrayList<>();
    private EventPriority priority = EventPriority.NORMAL;
    private boolean ignoreCancelled;
    private boolean onlyIfCancelled;
    private int maxFires = -1;
    private Duration expireAfter;
    private Predicate<E> expireIf;
    private Consumer<E> handler;
    private boolean async;

    EventBuilder(Class<E> type) {
        this.type = type;
    }

    public EventBuilder<E> priority(EventPriority priority) {
        this.priority = priority;
        return this;
    }

    public EventBuilder<E> ignoreCancelled() {
        this.ignoreCancelled = true;
        return this;
    }

    public EventBuilder<E> onlyIfCancelled() {
        this.onlyIfCancelled = true;
        return this;
    }

    public EventBuilder<E> filter(Predicate<E> predicate) {
        filters.add(predicate);
        return this;
    }

    public EventBuilder<E> maxFires(int count) {
        this.maxFires = count;
        return this;
    }

    public EventBuilder<E> expireAfter(Duration duration) {
        this.expireAfter = duration;
        return this;
    }

    public EventBuilder<E> expireIf(Predicate<E> condition) {
        this.expireIf = condition;
        return this;
    }

    public @NonNull ListenerHandle handler(Consumer<E> handler) {
        this.handler = handler;
        return EventBus.register(this);
    }

    public Class<E> type() {
        return type;
    }

    public EventPriority priority() {
        return priority;
    }

    public boolean shouldIgnoreCancelled() {
        return ignoreCancelled;
    }

    public boolean shouldOnlyIfCancelled() {
        return onlyIfCancelled;
    }

    public int maxFires() {
        return maxFires;
    }

    public Duration expireAfter() {
        return expireAfter;
    }

    public Predicate<E> expireIf() {
        return expireIf;
    }

    public Consumer<E> handler() {
        return handler;
    }

    @Contract(pure = true)
    @NonNull
    @Unmodifiable
    public List<Predicate<E>> filters() {
        return List.copyOf(filters);
    }

    public EventBuilder<E> async() {
        this.async = true;
        return this;
    }

    public boolean isAsync() {
        return async;
    }
}