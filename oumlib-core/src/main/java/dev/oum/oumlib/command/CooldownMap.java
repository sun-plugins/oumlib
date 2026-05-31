package dev.oum.oumlib.command;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CooldownMap {

    private final Map<UUID, Instant> timestamps = new ConcurrentHashMap<>();
    private final Duration duration;

    public CooldownMap(Duration duration) {
        this.duration = duration;
    }

    public boolean isOnCooldown(UUID id) {
        Instant last = timestamps.get(id);
        return last != null && Instant.now().isBefore(last.plus(duration));
    }

    public long remainingSeconds(UUID id) {
        Instant last = timestamps.get(id);
        if (last == null) return 0;
        return Math.max(0, Duration.between(Instant.now(), last.plus(duration)).getSeconds());
    }

    public void set(UUID id) {
        timestamps.put(id, Instant.now());
    }

    public void clear(UUID id) {
        timestamps.remove(id);
    }
}