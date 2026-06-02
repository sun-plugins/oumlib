package dev.oum.oumlib.util;

import dev.oum.oumlib.scheduler.Scheduler;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jspecify.annotations.NonNull;

import java.time.Duration;

public final class BossBars {

    private BossBars() {
    }

    /**
     * Creates and displays a bossbar to an audience.
     */
    public static @NonNull BossBar show(@NonNull Audience audience, @NonNull String titleMiniMessage,
                                        float progress, BossBar.@NonNull Color color, BossBar.@NonNull Overlay overlay) {
        Component title = MiniMessage.miniMessage().deserialize(titleMiniMessage);
        BossBar bar = BossBar.bossBar(title, progress, color, overlay);
        audience.showBossBar(bar);
        return bar;
    }

    /**
     * Creates, displays, and automatically removes a bossbar after a specified duration.
     */
    public static void showTemporary(@NonNull Audience audience, @NonNull String titleMiniMessage,
                                     float progress, BossBar.@NonNull Color color, BossBar.@NonNull Overlay overlay,
                                     @NonNull Duration duration) {
        BossBar bar = show(audience, titleMiniMessage, progress, color, overlay);
        Scheduler.runDelayed(duration, () -> audience.hideBossBar(bar));
    }
}
