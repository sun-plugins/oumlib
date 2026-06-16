package dev.oum.oumlib.command;

import dev.oum.oumlib.text.Localization;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

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

    @Contract("_ -> new")
    public static @NonNull CommandContext fromBrigadier(com.mojang.brigadier.context.@NonNull CommandContext<?> brigadierCtx) {
        Object source = brigadierCtx.getSource();
        Audience sender;
        String label = "";
        try {
            Class<?> cssClass = Class.forName("io.papermc.paper.command.brigadier.CommandSourceStack");
            if (cssClass.isInstance(source)) {
                sender = (Audience) cssClass.getMethod("getSender").invoke(source);
            } else {
                sender = (Audience) source;
            }
        } catch (Throwable t) {
            sender = (Audience) source;
        }

        try {
            String input = brigadierCtx.getInput();
            if (input != null && !input.isEmpty()) {
                String[] parts = input.split(" ");
                if (parts.length > 0) {
                    label = parts[0];
                    if (label.startsWith("/")) {
                        label = label.substring(1);
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        return new CommandContext(source, sender, label, new ArgumentMap(brigadierCtx));
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

    public void reply(@NonNull Component component) {
        sender.sendMessage(component);
    }

    public void reply(@NonNull String miniMessage, TagResolver... resolvers) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize(miniMessage, resolvers));
    }

    public void sendTranslated(@NonNull String key, TagResolver... resolvers) {
        sender.sendMessage(Localization.translateFor(sender, key, resolvers));
    }

    public void sendActionBar(@NonNull Component component) {
        sender.sendActionBar(component);
    }

    public void sendActionBar(@NonNull String miniMessage, TagResolver... resolvers) {
        sender.sendActionBar(MiniMessage.miniMessage().deserialize(miniMessage, resolvers));
    }

    public void sendTitle(@NonNull Component title, @NonNull Component subtitle) {
        sender.showTitle(Title.title(title, subtitle));
    }

    public void sendTitle(@NonNull Component title, @NonNull Component subtitle, Title.@Nullable Times times) {
        sender.showTitle(Title.title(title, subtitle, times));
    }

    public void sendTitle(@NonNull String title, @NonNull String subtitle, TagResolver... resolvers) {
        var mm = MiniMessage.miniMessage();
        sender.showTitle(Title.title(
                mm.deserialize(title, resolvers),
                mm.deserialize(subtitle, resolvers)
        ));
    }

    public void sendTitle(
            @NonNull String title,
            @NonNull String subtitle,
            @Nullable Duration fadeIn,
            @Nullable Duration stay,
            @Nullable Duration fadeOut,
            TagResolver... resolvers
    ) {
        var mm = MiniMessage.miniMessage();
        Title.Times times = null;
        if (fadeIn != null || stay != null || fadeOut != null) {
            times = Title.Times.times(
                    fadeIn != null ? fadeIn : Duration.ZERO,
                    stay != null ? stay : Duration.ZERO,
                    fadeOut != null ? fadeOut : Duration.ZERO
            );
        }
        sender.showTitle(Title.title(
                mm.deserialize(title, resolvers),
                mm.deserialize(subtitle, resolvers),
                times
        ));
    }

    public void clearTitle() {
        sender.clearTitle();
    }
}