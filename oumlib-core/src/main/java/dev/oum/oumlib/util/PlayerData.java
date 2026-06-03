package dev.oum.oumlib.util;

import com.google.gson.Gson;
import dev.oum.oumlib.OumLib;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public final class PlayerData {

    private static final Gson GSON = new Gson();
    private final Player player;
    private final PersistentDataContainer pdc;

    private PlayerData(@NonNull Player player) {
        this.player = player;
        this.pdc = player.getPersistentDataContainer();
    }

    public static @NonNull PlayerData of(@NonNull Player player) {
        return new PlayerData(player);
    }

    public @NonNull Player player() {
        return player;
    }

    @Contract("_ -> new")
    private @NonNull NamespacedKey key(String key) {
        return new NamespacedKey(OumLib.plugin(), key);
    }

    public void set(@NonNull String key, @Nullable String value) {
        if (value == null) {
            pdc.remove(key(key));
        } else {
            pdc.set(key(key), PersistentDataType.STRING, value);
        }
    }

    public @Nullable String get(@NonNull String key) {
        return pdc.get(key(key), PersistentDataType.STRING);
    }

    public String getOrDefault(@NonNull String key, @NonNull String def) {
        String val = get(key);
        return val != null ? val : def;
    }

    public void setInt(@NonNull String key, int value) {
        pdc.set(key(key), PersistentDataType.INTEGER, value);
    }

    public @Nullable Integer getInt(@NonNull String key) {
        return pdc.get(key(key), PersistentDataType.INTEGER);
    }

    public int getIntOrDefault(@NonNull String key, int def) {
        Integer val = getInt(key);
        return val != null ? val : def;
    }

    public void setDouble(@NonNull String key, double value) {
        pdc.set(key(key), PersistentDataType.DOUBLE, value);
    }

    public @Nullable Double getDouble(@NonNull String key) {
        return pdc.get(key(key), PersistentDataType.DOUBLE);
    }

    public double getDoubleOrDefault(@NonNull String key, double def) {
        Double val = getDouble(key);
        return val != null ? val : def;
    }

    public void setBoolean(@NonNull String key, boolean value) {
        pdc.set(key(key), PersistentDataType.BYTE, (byte) (value ? 1 : 0));
    }

    public boolean getBoolean(@NonNull String key) {
        Byte b = pdc.get(key(key), PersistentDataType.BYTE);
        return b != null && b != 0;
    }

    public boolean getBooleanOrDefault(@NonNull String key, boolean def) {
        Byte b = pdc.get(key(key), PersistentDataType.BYTE);
        return b != null ? b != 0 : def;
    }

    public void setLong(@NonNull String key, long value) {
        pdc.set(key(key), PersistentDataType.LONG, value);
    }

    public @Nullable Long getLong(@NonNull String key) {
        return pdc.get(key(key), PersistentDataType.LONG);
    }

    public long getLongOrDefault(@NonNull String key, long def) {
        Long val = getLong(key);
        return val != null ? val : def;
    }

    public void setList(@NonNull String key, @Nullable List<String> value) {
        if (value == null) {
            pdc.remove(key(key));
        } else {
            pdc.set(key(key), PersistentDataType.STRING, GSON.toJson(value));
        }
    }

    public @Nullable List<String> getList(@NonNull String key) {
        String raw = pdc.get(key(key), PersistentDataType.STRING);
        if (raw == null) return null;
        if (raw.isEmpty()) return List.of();
        return Arrays.asList(GSON.fromJson(raw, String[].class));
    }

    public void remove(@NonNull String key) {
        pdc.remove(key(key));
    }

    public boolean has(@NonNull String key) {
        return pdc.has(key(key));
    }
}
