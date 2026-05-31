package dev.oum.oumlib.util;

import dev.oum.oumlib.OumLib;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class Pdc {

    private Pdc() {}

    /**
     * Retrieves a string value from the item's PersistentDataContainer.
     */
    public static @Nullable String get(@NonNull ItemStack item, @NonNull String key) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        return meta.getPersistentDataContainer().get(nsk, PersistentDataType.STRING);
    }

    /**
     * Retrieves an integer value from the item's PersistentDataContainer.
     */
    public static @Nullable Integer getInt(@NonNull ItemStack item, @NonNull String key) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        return meta.getPersistentDataContainer().get(nsk, PersistentDataType.INTEGER);
    }

    /**
     * Retrieves a double value from the item's PersistentDataContainer.
     */
    public static @Nullable Double getDouble(@NonNull ItemStack item, @NonNull String key) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        return meta.getPersistentDataContainer().get(nsk, PersistentDataType.DOUBLE);
    }
}
