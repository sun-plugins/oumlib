package dev.oum.oumlib.text;

import org.bukkit.Bukkit;

@Deprecated(forRemoval = true, since = "1.0.4")
public final class BukkitText {

    private BukkitText() {
    }

    public static void console(String message) {
        Bukkit.getConsoleSender().sendMessage(Text.parse(message));
    }

    public static void broadcast(String message) {
        Bukkit.getOnlinePlayers().forEach(p -> Text.send(p, message));
    }

    public static void broadcast(String message, String permission) {
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission(permission))
                .forEach(p -> Text.send(p, message));
    }
}
