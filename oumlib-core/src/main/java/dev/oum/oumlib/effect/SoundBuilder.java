package dev.oum.oumlib.effect;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public final class SoundBuilder {
    private final Sound sound;
    private SoundCategory category = SoundCategory.MASTER;
    private float volume = 1.0f;
    private float pitch = 1.0f;
    private float pitchRange = 0.0f;

    public SoundBuilder(@NonNull Sound sound) {
        this.sound = sound;
    }

    public @NonNull SoundBuilder category(@NonNull SoundCategory category) {
        this.category = category;
        return this;
    }

    public @NonNull SoundBuilder volume(float volume) {
        this.volume = volume;
        return this;
    }

    public @NonNull SoundBuilder pitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    public @NonNull SoundBuilder pitchVariance(float range) {
        this.pitchRange = range;
        return this;
    }

    private float calculatePitch() {
        if (pitchRange <= 0.0f) return pitch;
        float min = pitch - pitchRange;
        float max = pitch + pitchRange;
        return (float) ThreadLocalRandom.current().nextDouble(min, max);
    }

    public void play(@NonNull Location location) {
        if (location.getWorld() != null) {
            location.getWorld().playSound(location, sound, category, volume, calculatePitch());
        }
    }

    public void play(@NonNull Player player) {
        player.playSound(player.getLocation(), sound, category, volume, calculatePitch());
    }

    public void play(@NonNull Player player, @NonNull Location location) {
        player.playSound(location, sound, category, volume, calculatePitch());
    }

    public void play(@NonNull Collection<? extends Player> players, @NonNull Location location) {
        float finalPitch = calculatePitch();
        for (Player p : players) {
            p.playSound(location, sound, category, volume, finalPitch);
        }
    }
}
