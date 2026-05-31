package dev.oum.oumlib.command;

import net.kyori.adventure.audience.Audience;
import org.jspecify.annotations.NonNull;

public record CommandContext(
        Object source,
        Audience sender,
        String label,
        ArgumentMap args
) {

    private static final Class<?> BUKKIT_PLAYER;
    private static final Class<?> VELOCITY_PLAYER;
    private static final Class<?> BUKKIT_CONSOLE;
    private static final Class<?> VELOCITY_CONSOLE;

    static {
        Class<?> bp = null;
        try {
            bp = Class.forName("org.bukkit.entity.Player");
        } catch (ClassNotFoundException ignored) {
        }
        BUKKIT_PLAYER = bp;

        Class<?> vp = null;
        try {
            vp = Class.forName("com.velocitypowered.api.proxy.Player");
        } catch (ClassNotFoundException ignored) {
        }
        VELOCITY_PLAYER = vp;

        Class<?> bc = null;
        try {
            bc = Class.forName("org.bukkit.command.ConsoleCommandSender");
        } catch (ClassNotFoundException ignored) {
        }
        BUKKIT_CONSOLE = bc;

        Class<?> vc = null;
        try {
            vc = Class.forName("com.velocitypowered.api.command.ConsoleCommandSource");
        } catch (ClassNotFoundException ignored) {
        }
        VELOCITY_CONSOLE = vc;
    }

    public @NonNull Audience sender() {
        return sender;
    }

    @SuppressWarnings("unchecked")
    public <P> P playerOrThrow() {
        if (isPlayer()) return (P) sender;
        throw new IllegalStateException("This command requires a player.");
    }

    public boolean isPlayer() {
        return (BUKKIT_PLAYER != null && BUKKIT_PLAYER.isInstance(sender))
                || (VELOCITY_PLAYER != null && VELOCITY_PLAYER.isInstance(sender));
    }

    public boolean isConsole() {
        return (BUKKIT_CONSOLE != null && BUKKIT_CONSOLE.isInstance(sender))
                || (VELOCITY_CONSOLE != null && VELOCITY_CONSOLE.isInstance(sender));
    }
}