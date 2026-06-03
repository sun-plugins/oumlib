package dev.oum.oumlib.text;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.oumlib.text.placeholder.PlaceholderResolver;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.RecordComponent;
import java.time.Duration;
import java.util.List;

public final class Text {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private Text() {
    }

    public static void send(@NonNull Audience audience, String message, Object... pairs) {
        audience.sendMessage(parse(resolve(message, audience, pairs)));
    }

    public static void send(@NonNull Audience audience, String message) {
        audience.sendMessage(parse(resolve(message, audience)));
    }

    public static void send(Audience audience, String message, Record data) {
        String injected = injectRecord(message, data);
        String resolved = resolve(injected, audience);
        audience.sendMessage(parse(resolved));
    }

    public static void sendLines(Audience audience, @NonNull List<String> lines, Object... pairs) {
        lines.forEach(line -> send(audience, line, pairs));
    }

    public static @NonNull Component parse(String message) {
        return MM.deserialize(message);
    }

    public static @NonNull String strip(String message) {
        return MM.stripTags(message);
    }

    public static void actionBar(@NonNull Audience audience, String message, Object... pairs) {
        audience.sendActionBar(parse(resolve(message, audience, pairs)));
    }

    public static void title(@NonNull Audience audience, String title, String subtitle,
                             Duration fadeIn, Duration stay, Duration fadeOut) {
        audience.showTitle(Title.title(parse(title), parse(subtitle), Title.Times.times(fadeIn, stay, fadeOut)));
    }

    public static void title(@NonNull Audience audience, String title, String subtitle) {
        title(audience, title, subtitle, Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500));
    }

    public static void broadcast(String message, Object... pairs) {
        OumLib.players().sendMessage(parse(resolve(message, null, pairs)));
    }

    public static void broadcast(String message, Record data) {
        String injected = injectRecord(message, data);
        String resolved = resolve(injected, null);
        OumLib.players().sendMessage(parse(resolved));
    }

    public static void broadcastActionBar(String message, Object... pairs) {
        OumLib.players().sendActionBar(parse(resolve(message, null, pairs)));
    }

    public static void broadcastTitle(String title, String subtitle, Duration fadeIn, Duration stay, Duration fadeOut) {
        OumLib.players().showTitle(Title.title(parse(title), parse(subtitle), Title.Times.times(fadeIn, stay, fadeOut)));
    }

    public static void broadcastTitle(String title, String subtitle) {
        broadcastTitle(title, subtitle, Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500));
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull TextBuilder builder(String message) {
        return new TextBuilder(message);
    }

    public static Component clickable(String text, ClickEvent clickEvent, String hoverText) {
        Component c = parse(text).clickEvent(clickEvent);
        if (hoverText != null) c = c.hoverEvent(HoverEvent.showText(parse(hoverText)));
        return c;
    }

    private static String resolve(String input, Object player, Object @NonNull ... pairs) {
        String result = input;
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            result = result.replace("<" + pairs[i] + ">", MM.escapeTags(String.valueOf(pairs[i + 1])));
        }
        return PlaceholderResolver.resolveInternal(result, player);
    }

    private static String resolve(String input, Object player) {
        return PlaceholderResolver.resolveInternal(input, player);
    }

    private static String injectRecord(String input, Record data) {
        if (data == null) return input;
        String result = input;
        try {
            for (RecordComponent comp : data.getClass().getRecordComponents()) {
                Object val = comp.getAccessor().invoke(data);
                result = result.replace("<" + comp.getName() + ">",
                        val != null ? MM.escapeTags(String.valueOf(val)) : "");
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    public static @NonNull BossBar bossBar(@NonNull Audience audience, @NonNull String titleMiniMessage,
                                           float progress, BossBar.@NonNull Color color, BossBar.@NonNull Overlay overlay) {
        Component title = parse(titleMiniMessage);
        BossBar bar = BossBar.bossBar(title, progress, color, overlay);
        audience.showBossBar(bar);
        return bar;
    }

    public static void bossBarTemporary(@NonNull Audience audience, @NonNull String titleMiniMessage,
                                        float progress, BossBar.@NonNull Color color, BossBar.@NonNull Overlay overlay,
                                        @NonNull Duration duration) {
        BossBar bar = bossBar(audience, titleMiniMessage, progress, color, overlay);
        Scheduler.runDelayed(duration, () -> audience.hideBossBar(bar));
    }

    public static final class Preset {

        private Preset() {
        }

        public static void success(Audience audience, String message, Object... pairs) {
            send(audience, OumLib.presets().prefix(dev.oum.oumlib.text.Preset.SUCCESS) + message, pairs);
        }

        public static void error(Audience audience, String message, Object... pairs) {
            send(audience, OumLib.presets().prefix(dev.oum.oumlib.text.Preset.ERROR) + message, pairs);
        }

        public static void info(Audience audience, String message, Object... pairs) {
            send(audience, OumLib.presets().prefix(dev.oum.oumlib.text.Preset.INFO) + message, pairs);
        }

        public static void warning(Audience audience, String message, Object... pairs) {
            send(audience, OumLib.presets().prefix(dev.oum.oumlib.text.Preset.WARNING) + message, pairs);
        }

        public static void successBroadcast(String message, Object... pairs) {
            broadcast(OumLib.presets().prefix(dev.oum.oumlib.text.Preset.SUCCESS) + message, pairs);
        }

        public static void errorBroadcast(String message, Object... pairs) {
            broadcast(OumLib.presets().prefix(dev.oum.oumlib.text.Preset.ERROR) + message, pairs);
        }

        public static void infoBroadcast(String message, Object... pairs) {
            broadcast(OumLib.presets().prefix(dev.oum.oumlib.text.Preset.INFO) + message, pairs);
        }

        public static void warningBroadcast(String message, Object... pairs) {
            broadcast(OumLib.presets().prefix(dev.oum.oumlib.text.Preset.WARNING) + message, pairs);
        }
    }
}