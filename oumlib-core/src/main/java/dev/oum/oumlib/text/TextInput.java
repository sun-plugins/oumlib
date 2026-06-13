package dev.oum.oumlib.text;

import dev.oum.oumlib.event.Events;
import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.oumlib.scheduler.TaskHandle;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.CheckReturnValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

@SuppressWarnings({"unused"})
public final class TextInput {

    private static final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private static boolean listenersRegistered = false;

    private TextInput() {
    }

    private static void ensureListeners() {
        if (listenersRegistered) return;
        listenersRegistered = true;

        Events.listen(AsyncChatEvent.class, event -> {
            Player player = event.getPlayer();
            Session session = sessions.get(player.getUniqueId());
            if (session == null) return;

            event.setCancelled(true);
            String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

            Scheduler.runFor(player, () -> {
                Session activeSession = sessions.get(player.getUniqueId());
                if (activeSession == null) return;

                if (message.equalsIgnoreCase(activeSession.cancelWord)) {
                    cancel(player.getUniqueId());
                    if (activeSession.cancelCallback != null) {
                        activeSession.cancelCallback.accept(player);
                    }
                    return;
                }

                if (activeSession.inputCallback.test(player, message)) {
                    cancel(player.getUniqueId());
                }
            });
        });

        Events.listen(PlayerQuitEvent.class, event -> {
            Player player = event.getPlayer();
            Scheduler.runFor(player, () -> cancel(player.getUniqueId()));
        });
    }

    @CheckReturnValue
    public static @NonNull Builder builder() {
        ensureListeners();
        return new Builder();
    }

    public static void cancel(@NonNull Player player) {
        Scheduler.runFor(player, () -> cancel(player.getUniqueId()));
    }

    private static void cancel(UUID uuid) {
        Session session = sessions.remove(uuid);
        if (session != null) {
            session.taskHandle().cancel();
        }
    }

    public static boolean isActive(@NonNull Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public static int activeSessionsCount() {
        return sessions.size();
    }

    public static final class Builder {
        private Duration timeout = Duration.ofSeconds(10);
        private Component prompt;
        private BiPredicate<Player, String> inputCallback;
        private Consumer<Player> cancelCallback;
        private Consumer<Player> timeoutCallback;
        private String cancelWord = "cancel";

        private Builder() {
        }

        @CheckReturnValue
        public @NonNull Builder timeout(@NonNull Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder prompt(@Nullable Component prompt) {
            this.prompt = prompt;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder onInput(@NonNull BiPredicate<Player, String> inputCallback) {
            this.inputCallback = inputCallback;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder onCancel(@Nullable Consumer<Player> cancelCallback) {
            this.cancelCallback = cancelCallback;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder onTimeout(@Nullable Consumer<Player> timeoutCallback) {
            this.timeoutCallback = timeoutCallback;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder cancelWord(@Nullable String cancelWord) {
            this.cancelWord = cancelWord;
            return this;
        }

        public void start(@NonNull Player player) {
            if (inputCallback == null) {
                throw new IllegalStateException("inputCallback must be set via onInput before starting a TextInput session");
            }
            UUID uuid = player.getUniqueId();

            Scheduler.runFor(player, () -> {
                TextInput.cancel(uuid);

                if (prompt != null) {
                    player.sendMessage(prompt);
                }

                TaskHandle task = Scheduler.runLater(timeout, () -> {
                    Scheduler.runFor(player, () -> {
                        Session expiredSession = sessions.remove(uuid);
                        if (expiredSession != null) {
                            if (timeoutCallback != null) {
                                timeoutCallback.accept(player);
                            }
                        }
                    });
                });

                sessions.put(uuid, new Session(task, inputCallback, cancelCallback, timeoutCallback, cancelWord));
            });
        }
    }

    private record Session(
            TaskHandle taskHandle,
            BiPredicate<Player, String> inputCallback,
            Consumer<Player> cancelCallback,
            Consumer<Player> timeoutCallback,
            String cancelWord
    ) {
    }
}
