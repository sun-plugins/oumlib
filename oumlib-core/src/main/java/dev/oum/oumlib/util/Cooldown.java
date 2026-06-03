package dev.oum.oumlib.util;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Cooldown {

    private final Map<UUID, Instant> timestamps = new ConcurrentHashMap<>();
    private final Duration duration;

    public Cooldown(Duration duration) {
        this.duration = duration;
    }

    @Contract("_ -> new")
    public static @NonNull Cooldown of(Duration duration) {
        return new Cooldown(duration);
    }

    public boolean isOnCooldown(UUID uuid) {
        Instant last = timestamps.get(uuid);
        return last != null && Instant.now().isBefore(last.plus(duration));
    }

    public Duration remaining(UUID uuid) {
        Instant last = timestamps.get(uuid);
        if (last == null) return Duration.ZERO;
        Duration rem = Duration.between(Instant.now(), last.plus(duration));
        return rem.isNegative() ? Duration.ZERO : rem;
    }

    public long remainingSeconds(UUID uuid) {
        return remaining(uuid).toSeconds();
    }

    public double remainingSecondsDouble(UUID uuid) {
        return remaining(uuid).toMillis() / 1000.0;
    }

    public void set(UUID uuid) {
        timestamps.put(uuid, Instant.now());
    }

    public void remove(UUID uuid) {
        timestamps.remove(uuid);
    }

    public void clear() {
        timestamps.clear();
    }
}
