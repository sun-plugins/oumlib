package dev.oum.oumlib.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Method;
import java.util.UUID;

@Deprecated(since = "1.0.7", forRemoval = true)
public final class BukkitEvents {

    private BukkitEvents() {
    }

    public static <E extends Event> EventBuilder<E> listenFor(@NonNull Player player, Class<E> type) {
        UUID id = player.getUniqueId();
        return new EventBuilder<>(type).filter(event -> {
            try {
                Method m = event.getClass().getMethod("getPlayer");
                Object result = m.invoke(event);
                return result instanceof Player p && p.getUniqueId().equals(id);
            } catch (Exception e) {
                return false;
            }
        });
    }
}
