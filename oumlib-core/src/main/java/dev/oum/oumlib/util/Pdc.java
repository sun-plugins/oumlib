package dev.oum.oumlib.util;

import com.google.gson.Gson;
import dev.oum.oumlib.OumLib;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public final class Pdc {

    private static final Gson GSON = new Gson();

    private Pdc() {
    }

    public static @Nullable String get(@NonNull ItemStack item, @NonNull String key) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        return meta.getPersistentDataContainer().get(nsk, PersistentDataType.STRING);
    }

    public static @Nullable Integer getInt(@NonNull ItemStack item, @NonNull String key) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        return meta.getPersistentDataContainer().get(nsk, PersistentDataType.INTEGER);
    }

    public static @Nullable Double getDouble(@NonNull ItemStack item, @NonNull String key) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        return meta.getPersistentDataContainer().get(nsk, PersistentDataType.DOUBLE);
    }

    public static @Nullable Boolean getBoolean(@NonNull ItemStack item, @NonNull String key) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        Byte b = meta.getPersistentDataContainer().get(nsk, PersistentDataType.BYTE);
        return b != null ? b != 0 : null;
    }

    public static @Nullable Long getLong(@NonNull ItemStack item, @NonNull String key) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        return meta.getPersistentDataContainer().get(nsk, PersistentDataType.LONG);
    }

    public static @Nullable List<String> getList(@NonNull ItemStack item, @NonNull String key) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        String raw = meta.getPersistentDataContainer().get(nsk, PersistentDataType.STRING);
        if (raw == null) return null;
        if (raw.isEmpty()) return List.of();
        return Arrays.asList(GSON.fromJson(raw, String[].class));
    }
}
