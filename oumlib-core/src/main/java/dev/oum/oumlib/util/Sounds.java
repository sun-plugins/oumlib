package dev.oum.oumlib.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Locale;

@SuppressWarnings("PatternValidation")
@Deprecated(since = "1.0.5", forRemoval = true)
public final class Sounds {

    private Sounds() {
    }

    public static void play(@NonNull Player player, @NonNull String soundName, float volume, float pitch) {
        var sound1 = soundName.toLowerCase(Locale.ROOT).replace('_', '.');
        var key = Key.key(sound1);
        var sound = Sound.sound(key, Sound.Source.MASTER, volume, pitch);
        player.playSound(sound);
    }

    public static void play(@NonNull Player player, @NonNull Sound sound) {
        player.playSound(sound);
    }

    public static void play(@NonNull Location loc, @NonNull String soundName, float volume, float pitch) {
        if (loc.getWorld() == null) return;
        var sound1 = soundName.toLowerCase(Locale.ROOT).replace('_', '.');
        var key = Key.key(sound1);
        var sound = Sound.sound(key, Sound.Source.MASTER, volume, pitch);
        loc.getWorld().playSound(loc, sound.name().asString(), volume, pitch);
    }

    public static void play(@NonNull Location loc, @NonNull Sound sound) {
        if (loc.getWorld() == null) return;
        loc.getWorld().playSound(loc, sound.name().asString(), sound.volume(), sound.pitch());
    }

    public record Effect(@NonNull String sound, float volume, float pitch) {
        public void play(@NonNull Player player) {
            Sounds.play(player, sound, volume, pitch);
        }

        public void play(@NonNull Location loc) {
            Sounds.play(loc, sound, volume, pitch);
        }
    }
}
