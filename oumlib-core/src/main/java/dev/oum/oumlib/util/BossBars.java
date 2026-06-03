package dev.oum.oumlib.util;

import dev.oum.oumlib.text.Text;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import org.jspecify.annotations.NonNull;

import java.time.Duration;

@Deprecated(since = "1.0.1", forRemoval = true)
public final class BossBars {

    private BossBars() {
    }

    @Deprecated(since = "1.0.1", forRemoval = true)
    public static @NonNull BossBar show(@NonNull Audience audience, @NonNull String titleMiniMessage,
                                        float progress, BossBar.@NonNull Color color, BossBar.@NonNull Overlay overlay) {
        return Text.bossBar(audience, titleMiniMessage, progress, color, overlay);
    }

    @Deprecated(since = "1.0.1", forRemoval = true)
    public static void showTemporary(@NonNull Audience audience, @NonNull String titleMiniMessage,
                                     float progress, BossBar.@NonNull Color color, BossBar.@NonNull Overlay overlay,
                                     @NonNull Duration duration) {
        Text.bossBarTemporary(audience, titleMiniMessage, progress, color, overlay, duration);
    }
}
