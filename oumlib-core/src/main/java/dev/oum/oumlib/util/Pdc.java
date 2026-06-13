package dev.oum.oumlib.util;

import com.google.gson.Gson;
import dev.oum.oumlib.OumLib;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class Pdc {

    private static final Gson GSON = new Gson();

    public interface PdcChangeListener {
        void onChange(@NonNull Object target, @NonNull NamespacedKey key, @Nullable Object oldValue, @Nullable Object newValue);
    }

    private static final Map<NamespacedKey, List<PdcChangeListener>> listeners = new ConcurrentHashMap<>();

    private Pdc() {
    }

    public static void registerListener(@NonNull NamespacedKey key, @NonNull PdcChangeListener listener) {
        listeners.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public static void unregisterListener(@NonNull NamespacedKey key, @NonNull PdcChangeListener listener) {
        List<PdcChangeListener> list = listeners.get(key);
        if (list != null) {
            list.remove(listener);
        }
    }

    private static void triggerListeners(@NonNull Object target, @NonNull NamespacedKey key, @Nullable Object oldValue, @Nullable Object newValue) {
        List<PdcChangeListener> list = listeners.get(key);
        if (list != null) {
            for (PdcChangeListener listener : list) {
                try {
                    listener.onChange(target, key, oldValue, newValue);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Contract("_ -> new")
    public static @NonNull PdcHolder of(@NonNull PersistentDataHolder holder) {
        return new PdcHolder(holder);
    }

    @Contract("_ -> new")
    public static @NonNull PdcItem of(@NonNull ItemStack item) {
        return new PdcItem(item);
    }

    @Deprecated(since = "1.0.4", forRemoval = true)
    public static @Nullable String get(@NonNull ItemStack item, @NonNull String key) {
        return of(item).get(key);
    }

    @Deprecated(since = "1.0.4", forRemoval = true)
    public static @Nullable Integer getInt(@NonNull ItemStack item, @NonNull String key) {
        return of(item).getInt(key);
    }

    @Deprecated(since = "1.0.4", forRemoval = true)
    public static @Nullable Double getDouble(@NonNull ItemStack item, @NonNull String key) {
        return of(item).getDouble(key);
    }

    @Deprecated(since = "1.0.4", forRemoval = true)
    public static @Nullable Boolean getBoolean(@NonNull ItemStack item, @NonNull String key) {
        return of(item).getBoolean(key);
    }

    @Deprecated(since = "1.0.4", forRemoval = true)
    public static @Nullable Long getLong(@NonNull ItemStack item, @NonNull String key) {
        return of(item).getLong(key);
    }

    @Deprecated(since = "1.0.4", forRemoval = true)
    public static @Nullable List<String> getList(@NonNull ItemStack item, @NonNull String key) {
        return of(item).getList(key);
    }

    public static final class PdcHolder {
        private final PersistentDataHolder holder;
        private final PersistentDataContainer pdc;
        private final String prefix;

        private PdcHolder(@NonNull PersistentDataHolder holder) {
            this(holder, null);
        }

        private PdcHolder(@NonNull PersistentDataHolder holder, @Nullable String prefix) {
            this.holder = holder;
            this.pdc = holder.getPersistentDataContainer();
            this.prefix = prefix;
        }

        public @NonNull PdcHolder namespaced(@NonNull String subNamespace) {
            return new PdcHolder(holder, prefix == null ? subNamespace : prefix + "_" + subNamespace);
        }

        private @NonNull NamespacedKey nsk(String key) {
            String finalKey = prefix == null ? key : prefix + "_" + key;
            return new NamespacedKey(OumLib.plugin(), finalKey);
        }

        public @NonNull PersistentDataHolder holder() {
            return holder;
        }

        public @NonNull PdcHolder set(@NonNull String key, @Nullable String value) {
            return set(nsk(key), value);
        }

        public @NonNull PdcHolder set(@NonNull NamespacedKey key, @Nullable String value) {
            String oldValue = pdc.get(key, PersistentDataType.STRING);
            if (value == null) {
                pdc.remove(key);
            } else {
                pdc.set(key, PersistentDataType.STRING, value);
            }
            triggerListeners(holder, key, oldValue, value);
            return this;
        }

        public @Nullable String get(@NonNull String key) {
            return get(nsk(key));
        }

        public @Nullable String get(@NonNull NamespacedKey key) {
            return pdc.get(key, PersistentDataType.STRING);
        }

        public @NonNull String getOrDefault(@NonNull String key, @NonNull String def) {
            return getOrDefault(nsk(key), def);
        }

        public @NonNull String getOrDefault(@NonNull NamespacedKey key, @NonNull String def) {
            String val = get(key);
            return val != null ? val : def;
        }

        public @NonNull PdcHolder setInt(@NonNull String key, int value) {
            return setInt(nsk(key), value);
        }

        public @NonNull PdcHolder setInt(@NonNull NamespacedKey key, int value) {
            Integer oldValue = pdc.get(key, PersistentDataType.INTEGER);
            pdc.set(key, PersistentDataType.INTEGER, value);
            triggerListeners(holder, key, oldValue, value);
            return this;
        }

        public @Nullable Integer getInt(@NonNull String key) {
            return getInt(nsk(key));
        }

        public @Nullable Integer getInt(@NonNull NamespacedKey key) {
            return pdc.get(key, PersistentDataType.INTEGER);
        }

        public int getIntOrDefault(@NonNull String key, int def) {
            return getIntOrDefault(nsk(key), def);
        }

        public int getIntOrDefault(@NonNull NamespacedKey key, int def) {
            Integer val = getInt(key);
            return val != null ? val : def;
        }

        public @NonNull PdcHolder setDouble(@NonNull String key, double value) {
            return setDouble(nsk(key), value);
        }

        public @NonNull PdcHolder setDouble(@NonNull NamespacedKey key, double value) {
            Double oldValue = pdc.get(key, PersistentDataType.DOUBLE);
            pdc.set(key, PersistentDataType.DOUBLE, value);
            triggerListeners(holder, key, oldValue, value);
            return this;
        }

        public @Nullable Double getDouble(@NonNull String key) {
            return getDouble(nsk(key));
        }

        public @Nullable Double getDouble(@NonNull NamespacedKey key) {
            return pdc.get(key, PersistentDataType.DOUBLE);
        }

        public double getDoubleOrDefault(@NonNull String key, double def) {
            return getDoubleOrDefault(nsk(key), def);
        }

        public double getDoubleOrDefault(@NonNull NamespacedKey key, double def) {
            Double val = getDouble(key);
            return val != null ? val : def;
        }

        public @NonNull PdcHolder setBoolean(@NonNull String key, boolean value) {
            return setBoolean(nsk(key), value);
        }

        public @NonNull PdcHolder setBoolean(@NonNull NamespacedKey key, boolean value) {
            Byte b = pdc.get(key, PersistentDataType.BYTE);
            Boolean oldValue = b != null ? b != 0 : null;
            pdc.set(key, PersistentDataType.BYTE, (byte) (value ? 1 : 0));
            triggerListeners(holder, key, oldValue, value);
            return this;
        }

        public boolean getBoolean(@NonNull String key) {
            return getBoolean(nsk(key));
        }

        public boolean getBoolean(@NonNull NamespacedKey key) {
            Byte b = pdc.get(key, PersistentDataType.BYTE);
            return b != null && b != 0;
        }

        public boolean getBooleanOrDefault(@NonNull String key, boolean def) {
            return getBooleanOrDefault(nsk(key), def);
        }

        public boolean getBooleanOrDefault(@NonNull NamespacedKey key, boolean def) {
            Byte b = pdc.get(key, PersistentDataType.BYTE);
            return b != null ? b != 0 : def;
        }

        public @NonNull PdcHolder setLong(@NonNull String key, long value) {
            return setLong(nsk(key), value);
        }

        public @NonNull PdcHolder setLong(@NonNull NamespacedKey key, long value) {
            Long oldValue = pdc.get(key, PersistentDataType.LONG);
            pdc.set(key, PersistentDataType.LONG, value);
            triggerListeners(holder, key, oldValue, value);
            return this;
        }

        public @Nullable Long getLong(@NonNull String key) {
            return getLong(nsk(key));
        }

        public @Nullable Long getLong(@NonNull NamespacedKey key) {
            return pdc.get(key, PersistentDataType.LONG);
        }

        public long getLongOrDefault(@NonNull String key, long def) {
            return getLongOrDefault(nsk(key), def);
        }

        public long getLongOrDefault(@NonNull NamespacedKey key, long def) {
            Long val = getLong(key);
            return val != null ? val : def;
        }

        public @NonNull PdcHolder setList(@NonNull String key, @Nullable List<String> value) {
            return setList(nsk(key), value);
        }

        public @NonNull PdcHolder setList(@NonNull NamespacedKey key, @Nullable List<String> value) {
            List<String> oldValue = getList(key);
            if (value == null) {
                pdc.remove(key);
            } else {
                pdc.set(key, PersistentDataType.STRING, GSON.toJson(value));
            }
            triggerListeners(holder, key, oldValue, value);
            return this;
        }

        public @Nullable List<String> getList(@NonNull String key) {
            return getList(nsk(key));
        }

        public @Nullable List<String> getList(@NonNull NamespacedKey key) {
            String raw = pdc.get(key, PersistentDataType.STRING);
            if (raw == null) return null;
            if (raw.isEmpty()) return List.of();
            return Arrays.asList(GSON.fromJson(raw, String[].class));
        }

        public <T> @NonNull PdcHolder setObject(@NonNull String key, @Nullable T value) {
            return setObject(nsk(key), value);
        }

        public <T> @NonNull PdcHolder setObject(@NonNull NamespacedKey key, @Nullable T value) {
            Object oldValue = pdc.get(key, PersistentDataType.STRING);
            if (value == null) {
                pdc.remove(key);
            } else {
                pdc.set(key, PersistentDataType.STRING, GSON.toJson(value));
            }
            triggerListeners(holder, key, oldValue, value);
            return this;
        }

        public <T> @Nullable T getObject(@NonNull String key, @NonNull Class<T> type) {
            return getObject(nsk(key), type);
        }

        public <T> @Nullable T getObject(@NonNull NamespacedKey key, @NonNull Class<T> type) {
            String raw = pdc.get(key, PersistentDataType.STRING);
            if (raw == null) return null;
            return GSON.fromJson(raw, type);
        }

        public @NonNull PdcHolder remove(@NonNull String key) {
            return remove(nsk(key));
        }

        public @NonNull PdcHolder remove(@NonNull NamespacedKey key) {
            pdc.remove(key);
            triggerListeners(holder, key, null, null);
            return this;
        }

        public boolean has(@NonNull String key) {
            return has(nsk(key));
        }

        public boolean has(@NonNull NamespacedKey key) {
            return pdc.has(key);
        }
    }

    public static final class PdcItem {
        private final ItemStack item;
        private final String prefix;

        private PdcItem(@NonNull ItemStack item) {
            this(item, null);
        }

        private PdcItem(@NonNull ItemStack item, @Nullable String prefix) {
            this.item = item;
            this.prefix = prefix;
        }

        public @NonNull PdcItem namespaced(@NonNull String subNamespace) {
            return new PdcItem(item, prefix == null ? subNamespace : prefix + "_" + subNamespace);
        }

        private @NonNull NamespacedKey nsk(String key) {
            String finalKey = prefix == null ? key : prefix + "_" + key;
            return new NamespacedKey(OumLib.plugin(), finalKey);
        }

        public @NonNull ItemStack item() {
            return item;
        }

        private boolean updateMeta(Consumer<ItemMeta> consumer) {
            if (!item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta == null) return false;
                consumer.accept(meta);
                return item.setItemMeta(meta);
            }
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return false;
            consumer.accept(meta);
            return item.setItemMeta(meta);
        }

        public @NonNull PdcItem set(@NonNull String key, @Nullable String value) {
            return set(nsk(key), value);
        }

        public @NonNull PdcItem set(@NonNull NamespacedKey key, @Nullable String value) {
            String oldValue = get(key);
            updateMeta(meta -> {
                if (value == null) {
                    meta.getPersistentDataContainer().remove(key);
                } else {
                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
                }
            });
            triggerListeners(item, key, oldValue, value);
            return this;
        }

        public @Nullable String get(@NonNull String key) {
            return get(nsk(key));
        }

        public @Nullable String get(@NonNull NamespacedKey key) {
            if (!item.hasItemMeta()) return null;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return null;
            return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        }

        public @NonNull String getOrDefault(@NonNull String key, @NonNull String def) {
            return getOrDefault(nsk(key), def);
        }

        public @NonNull String getOrDefault(@NonNull NamespacedKey key, @NonNull String def) {
            String val = get(key);
            return val != null ? val : def;
        }

        public @NonNull PdcItem setInt(@NonNull String key, int value) {
            return setInt(nsk(key), value);
        }

        public @NonNull PdcItem setInt(@NonNull NamespacedKey key, int value) {
            Integer oldValue = getInt(key);
            updateMeta(meta -> meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, value));
            triggerListeners(item, key, oldValue, value);
            return this;
        }

        public @Nullable Integer getInt(@NonNull String key) {
            return getInt(nsk(key));
        }

        public @Nullable Integer getInt(@NonNull NamespacedKey key) {
            if (!item.hasItemMeta()) return null;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return null;
            return meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        }

        public int getIntOrDefault(@NonNull String key, int def) {
            return getIntOrDefault(nsk(key), def);
        }

        public int getIntOrDefault(@NonNull NamespacedKey key, int def) {
            Integer val = getInt(key);
            return val != null ? val : def;
        }

        public @NonNull PdcItem setDouble(@NonNull String key, double value) {
            return setDouble(nsk(key), value);
        }

        public @NonNull PdcItem setDouble(@NonNull NamespacedKey key, double value) {
            Double oldValue = getDouble(key);
            updateMeta(meta -> meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, value));
            triggerListeners(item, key, oldValue, value);
            return this;
        }

        public @Nullable Double getDouble(@NonNull String key) {
            return getDouble(nsk(key));
        }

        public @Nullable Double getDouble(@NonNull NamespacedKey key) {
            if (!item.hasItemMeta()) return null;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return null;
            return meta.getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
        }

        public double getDoubleOrDefault(@NonNull String key, double def) {
            return getDoubleOrDefault(nsk(key), def);
        }

        public double getDoubleOrDefault(@NonNull NamespacedKey key, double def) {
            Double val = getDouble(key);
            return val != null ? val : def;
        }

        public @NonNull PdcItem setBoolean(@NonNull String key, boolean value) {
            return setBoolean(nsk(key), value);
        }

        public @NonNull PdcItem setBoolean(@NonNull NamespacedKey key, boolean value) {
            Boolean oldValue = getBoolean(key);
            updateMeta(meta -> meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) (value ? 1 : 0)));
            triggerListeners(item, key, oldValue, value);
            return this;
        }

        public boolean getBoolean(@NonNull String key) {
            return getBoolean(nsk(key));
        }

        public boolean getBoolean(@NonNull NamespacedKey key) {
            if (!item.hasItemMeta()) return false;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return false;
            Byte b = meta.getPersistentDataContainer().get(key, PersistentDataType.BYTE);
            return b != null && b != 0;
        }

        public boolean getBooleanOrDefault(@NonNull String key, boolean def) {
            return getBooleanOrDefault(nsk(key), def);
        }

        public boolean getBooleanOrDefault(@NonNull NamespacedKey key, boolean def) {
            if (!item.hasItemMeta()) return def;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return def;
            Byte b = meta.getPersistentDataContainer().get(key, PersistentDataType.BYTE);
            return b != null ? b != 0 : def;
        }

        public @NonNull PdcItem setLong(@NonNull String key, long value) {
            return setLong(nsk(key), value);
        }

        public @NonNull PdcItem setLong(@NonNull NamespacedKey key, long value) {
            Long oldValue = getLong(key);
            updateMeta(meta -> meta.getPersistentDataContainer().set(key, PersistentDataType.LONG, value));
            triggerListeners(item, key, oldValue, value);
            return this;
        }

        public @Nullable Long getLong(@NonNull String key) {
            return getLong(nsk(key));
        }

        public @Nullable Long getLong(@NonNull NamespacedKey key) {
            if (!item.hasItemMeta()) return null;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return null;
            return meta.getPersistentDataContainer().get(key, PersistentDataType.LONG);
        }

        public long getLongOrDefault(@NonNull String key, long def) {
            return getLongOrDefault(nsk(key), def);
        }

        public long getLongOrDefault(@NonNull NamespacedKey key, long def) {
            Long val = getLong(key);
            return val != null ? val : def;
        }

        public @NonNull PdcItem setList(@NonNull String key, @Nullable List<String> value) {
            return setList(nsk(key), value);
        }

        public @NonNull PdcItem setList(@NonNull NamespacedKey key, @Nullable List<String> value) {
            List<String> oldValue = getList(key);
            updateMeta(meta -> {
                if (value == null) {
                    meta.getPersistentDataContainer().remove(key);
                } else {
                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, GSON.toJson(value));
                }
            });
            triggerListeners(item, key, oldValue, value);
            return this;
        }

        public @Nullable List<String> getList(@NonNull String key) {
            return getList(nsk(key));
        }

        public @Nullable List<String> getList(@NonNull NamespacedKey key) {
            if (!item.hasItemMeta()) return null;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return null;
            String raw = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if (raw == null) return null;
            if (raw.isEmpty()) return List.of();
            return Arrays.asList(GSON.fromJson(raw, String[].class));
        }

        public <T> @NonNull PdcItem setObject(@NonNull String key, @Nullable T value) {
            return setObject(nsk(key), value);
        }

        public <T> @NonNull PdcItem setObject(@NonNull NamespacedKey key, @Nullable T value) {
            Object oldValue = get(key);
            updateMeta(meta -> {
                if (value == null) {
                    meta.getPersistentDataContainer().remove(key);
                } else {
                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, GSON.toJson(value));
                }
            });
            triggerListeners(item, key, oldValue, value);
            return this;
        }

        public <T> @Nullable T getObject(@NonNull String key, @NonNull Class<T> type) {
            return getObject(nsk(key), type);
        }

        public <T> @Nullable T getObject(@NonNull NamespacedKey key, @NonNull Class<T> type) {
            String raw = get(key);
            if (raw == null) return null;
            return GSON.fromJson(raw, type);
        }

        public @NonNull PdcItem remove(@NonNull String key) {
            return remove(nsk(key));
        }

        public @NonNull PdcItem remove(@NonNull NamespacedKey key) {
            updateMeta(meta -> meta.getPersistentDataContainer().remove(key));
            triggerListeners(item, key, null, null);
            return this;
        }

        public boolean has(@NonNull String key) {
            return has(nsk(key));
        }

        public boolean has(@NonNull NamespacedKey key) {
            if (!item.hasItemMeta()) return false;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return false;
            return meta.getPersistentDataContainer().has(key);
        }
    }
}
