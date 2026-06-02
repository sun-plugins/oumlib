package dev.oum.example;

import com.velocitypowered.api.event.connection.PostLoginEvent;
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.event.Events;
import dev.oum.oumlib.text.Text;
import net.kyori.adventure.text.Component;

public final class VelocityAnnouncerHelper {

    private VelocityAnnouncerHelper() {
    }

    /**
     * Sends an actionbar message to all online proxy players.
     * Note: Adventure's {@code actionBar} on Velocity players is a no-op in most proxy setups.
     * As a fallback, this method can display the message as a subtitle (title fallback) or chat message.
     */
    public static void sendActionbarToAll(String actionbarMessage) {
        OumLib.proxy().getAllPlayers().forEach(player -> {
            // Direct action bar call (might be no-op depending on setup)
            Text.actionBar(player, actionbarMessage);

            // Fallback Option A: Send as a subtitle with an empty main title to simulate actionbar
            // Text.title(player, "", actionbarMessage, java.time.Duration.ofMillis(100), java.time.Duration.ofSeconds(2), java.time.Duration.ofMillis(100));

            // Fallback Option B: Send as a standard chat message
            // Text.send(player, actionbarMessage);
        });
    }

    public static void registerOnceListener(String message) {
        Events.listenOnce(PostLoginEvent.class, event ->
                Text.Preset.info(event.getPlayer(), "<yellow>Special Notice: </yellow>" + message)
        );
    }

    public static void broadcast(Component component) {
        OumLib.proxy().sendMessage(component);
    }
}
