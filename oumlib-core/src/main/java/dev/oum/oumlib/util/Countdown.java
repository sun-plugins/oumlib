package dev.oum.oumlib.util;

import dev.oum.oumlib.effect.Sounds;
import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.oumlib.scheduler.TaskHandle;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Countdown {

    private final Audience audience;
    private final int totalSeconds;
    private final Display displayMode;
    private final Function<Integer, String> formatFunction;
    private final BiConsumer<Audience, Integer> onTick;
    private final Consumer<Audience> onComplete;
    private final Sounds.Effect tickSound;
    private final Predicate<Integer> displayFilter;
    private int secondsRemaining;
    private TaskHandle task;
    @Contract(pure = true)
    private Countdown(@NonNull Builder builder) {
        this.audience = builder.audience;
        this.totalSeconds = builder.totalSeconds;
        this.displayMode = builder.displayMode;
        this.formatFunction = builder.formatFunction;
        this.onTick = builder.onTick;
        this.onComplete = builder.onComplete;
        this.tickSound = builder.tickSound;
        this.displayFilter = builder.displayFilter;
        this.secondsRemaining = totalSeconds;
    }

    public static @NonNull Builder builder() {
        return new Builder();
    }

    public @NonNull TaskHandle start() {
        if (task != null) {
            return task;
        }

        task = Scheduler.runRepeating(Duration.ZERO, Duration.ofSeconds(1), () -> {
            if (secondsRemaining <= 0) {
                if (onComplete != null) {
                    onComplete.accept(audience);
                }
                cancel();
                return;
            }

            if (displayFilter.test(secondsRemaining)) {
                String formatted = formatFunction.apply(secondsRemaining);
                Component message = MiniMessage.miniMessage().deserialize(formatted);

                if (displayMode == Display.TITLE) {
                    audience.showTitle(Title.title(message, Component.empty()));
                } else if (displayMode == Display.ACTION_BAR) {
                    audience.sendActionBar(message);
                } else if (displayMode == Display.CHAT) {
                    audience.sendMessage(message);
                }

                if (tickSound != null && audience instanceof Player player) {
                    tickSound.play(player);
                }
            }

            if (onTick != null) {
                onTick.accept(audience, secondsRemaining);
            }

            secondsRemaining--;
        });

        return task;
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public enum Display {
        TITLE,
        ACTION_BAR,
        CHAT
    }

    public static final class Builder {
        private Audience audience;
        private int totalSeconds = 5;
        private Display displayMode = Display.TITLE;
        private Function<Integer, String> formatFunction = s -> "<yellow>" + s;
        private BiConsumer<Audience, Integer> onTick;
        private Consumer<Audience> onComplete;
        private Sounds.Effect tickSound;
        private Predicate<Integer> displayFilter;

        private Builder() {
        }

        public @NonNull Builder audience(@NonNull Audience audience) {
            this.audience = audience;
            return this;
        }

        public @NonNull Builder duration(@NonNull Duration duration) {
            this.totalSeconds = (int) duration.getSeconds();
            return this;
        }

        public @NonNull Builder seconds(int seconds) {
            this.totalSeconds = seconds;
            return this;
        }

        public @NonNull Builder display(@NonNull Display mode) {
            this.displayMode = mode;
            return this;
        }

        public @NonNull Builder format(@NonNull Function<Integer, String> format) {
            this.formatFunction = format;
            return this;
        }

        public @NonNull Builder format(@NonNull String template) {
            this.formatFunction = s -> template
                    .replace("%time%", String.valueOf(s))
                    .replace("%duration%", Format.duration(Duration.ofSeconds(s)))
                    .replace("%digital%", Format.digitalTime(Duration.ofSeconds(s)));
            return this;
        }

        public @NonNull Builder onTick(@Nullable BiConsumer<Audience, Integer> onTick) {
            this.onTick = onTick;
            return this;
        }

        public @NonNull Builder onComplete(@Nullable Consumer<Audience> onComplete) {
            this.onComplete = onComplete;
            return this;
        }

        public @NonNull Builder sound(Sounds.@Nullable Effect sound) {
            this.tickSound = sound;
            return this;
        }

        public @NonNull Builder displayFilter(@NonNull Predicate<Integer> filter) {
            this.displayFilter = filter;
            return this;
        }

        public @NonNull Builder intervals(int... seconds) {
            Set<Integer> set = new HashSet<>();
            for (int s : seconds) {
                set.add(s);
            }
            this.displayFilter = set::contains;
            return this;
        }

        public @NonNull Countdown build() {
            if (audience == null) {
                throw new IllegalStateException("Audience must be set.");
            }
            if (displayFilter == null) {
                if (displayMode == Display.CHAT) {
                    displayFilter = s -> s % 10 == 0 || s <= 5;
                } else {
                    displayFilter = s -> true;
                }
            }
            return new Countdown(this);
        }

        public @NonNull TaskHandle start() {
            return build().start();
        }
    }
}
