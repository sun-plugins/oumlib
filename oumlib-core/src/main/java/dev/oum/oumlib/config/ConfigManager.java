package dev.oum.oumlib.config;

import dev.oum.oumlib.OumLib;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ConfigManager<T extends Record & ConfigSection> {

    private final String fileName;
    private final Supplier<T> defaults;
    private final Class<T> type;
    private T current;
    private Consumer<T> reloadCallback;

    private ConfigManager(Class<T> type, String fileName, Supplier<T> defaults) {
        this.type = type;
        this.fileName = fileName;
        this.defaults = defaults;
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public static <T extends Record & ConfigSection> @NonNull ConfigManager<T> of(
            Class<T> type,
            String fileName,
            Supplier<T> defaults
    ) {
        return new ConfigManager<>(type, fileName, defaults);
    }

    public ConfigManager<T> onReload(Consumer<T> callback) {
        this.reloadCallback = callback;
        return this;
    }

    public ConfigManager<T> enableAutoReload() {
        File dir = OumLib.getDataFolder();
        try {
            WatchService ws = FileSystems.getDefault().newWatchService();
            dir.toPath().register(ws, StandardWatchEventKinds.ENTRY_MODIFY);
            Thread.ofVirtual().start(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        WatchKey key = ws.take();
                        boolean relevant = key.pollEvents().stream()
                                .anyMatch(e -> e.context().toString().equals(fileName));
                        key.reset();
                        if (relevant) {
                            current = load();
                            if (reloadCallback != null) reloadCallback.accept(current);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        } catch (IOException e) {
            OumLib.logError("Could not watch " + fileName + " for changes.", e);
        }
        return this;
    }

    public T get() {
        if (current == null) current = load();
        return current;
    }

    public void reload() {
        current = load();
        if (reloadCallback != null) reloadCallback.accept(current);
    }

    public void update(T newConfig) {
        this.current = newConfig;
        File file = new File(OumLib.getDataFolder(), fileName);
        save(file, newConfig, new LinkedHashMap<>());
        if (reloadCallback != null) reloadCallback.accept(newConfig);
    }

    private T load() {
        File file = new File(OumLib.getDataFolder(), fileName);
        T def = defaults.get();

        if (!file.exists()) {
            save(file, def, new LinkedHashMap<>());
            return def;
        }

        Map<String, Object> yaml = parseYaml(file);

        // Collect unknown keys the user may have added — preserved on save.
        Map<String, Object> unknownKeys = new LinkedHashMap<>();
        for (String yamlKey : yaml.keySet()) {
            boolean isKnown = false;
            for (RecordComponent comp : type.getRecordComponents()) {
                if (toKebab(comp.getName()).equals(yamlKey)) {
                    isKnown = true;
                    break;
                }
            }
            if (!isKnown) unknownKeys.put(yamlKey, yaml.get(yamlKey));
        }

        Map<String, Object> recordValues = mergeRecordValues(yaml, def);
        T loaded = reconstruct(recordValues, def);

        boolean needsSave = false;
        String missingKey = null;
        for (RecordComponent comp : type.getRecordComponents()) {
            String key = toKebab(comp.getName());
            if (!yaml.containsKey(key)) {
                needsSave = true;
                missingKey = key;
                break;
            }
        }
        if (needsSave) {
            OumLib.logError("ConfigManager: Saving config because of missing key '" + missingKey + "'. Keys in YAML: " + yaml.keySet());
            save(file, loaded, unknownKeys);
        }

        return loaded;
    }

    private @NonNull Map<String, Object> mergeRecordValues(Map<String, Object> yaml, T def) {
        Map<String, Object> result = new HashMap<>();
        for (RecordComponent comp : type.getRecordComponents()) {
            String key = toKebab(comp.getName());
            if (yaml.containsKey(key)) {
                result.put(comp.getName(), yaml.get(key));
            } else {
                try {
                    result.put(comp.getName(), comp.getAccessor().invoke(def));
                } catch (Exception e) {
                    result.put(comp.getName(), null);
                }
            }
        }
        return result;
    }

    private T reconstruct(Map<String, Object> values, T def) {
        RecordComponent[] comps = type.getRecordComponents();
        Object[] args = new Object[comps.length];
        Class<?>[] types = new Class<?>[comps.length];
        for (int i = 0; i < comps.length; i++) {
            args[i] = values.get(comps[i].getName());
            types[i] = comps[i].getType();
        }
        try {
            return type.getDeclaredConstructor(types).newInstance(args);
        } catch (Exception e) {
            OumLib.logError("Failed to reconstruct " + fileName + ", using defaults.", e);
            return def;
        }
    }

    private void save(@NonNull File file, T config, Map<String, Object> preservedUnknown) {
        file.getParentFile().mkdirs();
        StringBuilder yaml = new StringBuilder();

        for (RecordComponent comp : type.getRecordComponents()) {
            Comment comment = comp.getAnnotation(Comment.class);
            if (comment != null) {
                for (String line : comment.value()) {
                    yaml.append("# ").append(line).append('\n');
                }
            }
            try {
                Object value = comp.getAccessor().invoke(config);
                yaml.append(toKebab(comp.getName())).append(": ").append(toYamlValue(value)).append('\n');
            } catch (Exception ignored) {
            }
        }

        if (!preservedUnknown.isEmpty()) {
            yaml.append("\n# Additional keys\n");
            preservedUnknown.forEach((k, v) -> yaml.append(k).append(": ").append(toYamlValue(v)).append('\n'));
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(yaml.toString());
        } catch (IOException e) {
            OumLib.logError("Failed to save " + fileName + ".", e);
        }
    }

    private static Map<String, Object> parseYaml(File file) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (!file.exists()) return map;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int colon = line.indexOf(':');
                if (colon == -1) continue;
                String key = line.substring(0, colon).trim();
                String valStr = line.substring(colon + 1).trim();

                Object val = getObject(valStr);
                map.put(key, val);
            }
        } catch (IOException ignored) {
        }
        return map;
    }

    private static @Nullable Object getObject(@NonNull String valStr) {
        Object val;
        if (valStr.startsWith("\"") && valStr.endsWith("\"")) {
            val = valStr.substring(1, valStr.length() - 1).replace("\\\"", "\"");
        } else if (valStr.startsWith("'") && valStr.endsWith("'")) {
            val = valStr.substring(1, valStr.length() - 1);
        } else if (valStr.equalsIgnoreCase("true")) {
            val = true;
        } else if (valStr.equalsIgnoreCase("false")) {
            val = false;
        } else if (valStr.equalsIgnoreCase("null")) {
            val = null;
        } else {
            try {
                if (valStr.contains(".")) {
                    val = Double.parseDouble(valStr);
                } else {
                    val = Integer.parseInt(valStr);
                }
            } catch (NumberFormatException e) {
                val = valStr;
            }
        }
        return val;
    }

    private static String toYamlValue(Object value) {
        if (value instanceof String s) return "\"" + s.replace("\"", "\\\"") + "\"";
        if (value == null) return "null";
        return String.valueOf(value);
    }

    private static @NonNull String toKebab(@NonNull String camel) {
        return camel
                .replaceAll("([a-z])([A-Z])", "$1-$2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1-$2")
                .toLowerCase();
    }
}