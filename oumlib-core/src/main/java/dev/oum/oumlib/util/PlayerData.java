package dev.oum.oumlib.util;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Deprecated(since = "1.0.4", forRemoval = true)
public final class PlayerData {

    private final Player player;
    private final Pdc.PdcHolder holder;

    private PlayerData(@NonNull Player player) {
        this.player = player;
        this.holder = Pdc.of(player);
    }

    public static @NonNull PlayerData of(@NonNull Player player) {
        return new PlayerData(player);
    }

    public @NonNull Player player() {
        return player;
    }

    public void set(@NonNull String key, @Nullable String value) {
        holder.set(key, value);
    }

    public @Nullable String get(@NonNull String key) {
        return holder.get(key);
    }

    public @NonNull String getOrDefault(@NonNull String key, @NonNull String def) {
        return holder.getOrDefault(key, def);
    }

    public void setInt(@NonNull String key, int value) {
        holder.setInt(key, value);
    }

    public @Nullable Integer getInt(@NonNull String key) {
        return holder.getInt(key);
    }

    public int getIntOrDefault(@NonNull String key, int def) {
        return holder.getIntOrDefault(key, def);
    }

    public void setDouble(@NonNull String key, double value) {
        holder.setDouble(key, value);
    }

    public @Nullable Double getDouble(@NonNull String key) {
        return holder.getDouble(key);
    }

    public double getDoubleOrDefault(@NonNull String key, double def) {
        return holder.getDoubleOrDefault(key, def);
    }

    public void setBoolean(@NonNull String key, boolean value) {
        holder.setBoolean(key, value);
    }

    public boolean getBoolean(@NonNull String key) {
        return holder.getBoolean(key);
    }

    public boolean getBooleanOrDefault(@NonNull String key, boolean def) {
        return holder.getBooleanOrDefault(key, def);
    }

    public void setLong(@NonNull String key, long value) {
        holder.setLong(key, value);
    }

    public @Nullable Long getLong(@NonNull String key) {
        return holder.getLong(key);
    }

    public long getLongOrDefault(@NonNull String key, long def) {
        return holder.getLongOrDefault(key, def);
    }

    public void setList(@NonNull String key, @Nullable List<String> value) {
        holder.setList(key, value);
    }

    public @Nullable List<String> getList(@NonNull String key) {
        return holder.getList(key);
    }

    public void remove(@NonNull String key) {
        holder.remove(key);
    }

    public boolean has(@NonNull String key) {
        return holder.has(key);
    }
}
