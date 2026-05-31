package dev.oum.oumlib.text;

import org.bukkit.Bukkit;

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
