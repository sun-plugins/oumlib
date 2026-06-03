package dev.oum.oumlib.config;

import dev.oum.oumlib.OumLib;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.io.File;
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

@SuppressWarnings("UnusedReturnValue")
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

    @SuppressWarnings("unchecked")
    private static Object reconstructValue(Object val, Class<?> targetType, Object defaultVal) {
        if (Record.class.isAssignableFrom(targetType) && ConfigSection.class.isAssignableFrom(targetType)) {
            Class<? extends Record> recordType = (Class<? extends Record>) targetType;
            Map<String, Object> valMap;
            if (val instanceof Map) {
                valMap = (Map<String, Object>) val;
            } else {
                valMap = new HashMap<>();
            }

            RecordComponent[] comps = recordType.getRecordComponents();
            Object[] args = new Object[comps.length];
            Class<?>[] types = new Class<?>[comps.length];
            for (int i = 0; i < comps.length; i++) {
                RecordComponent comp = comps[i];
                String key = toKebab(comp.getName());
                Object memberVal = valMap.get(key);
                Object memberDefault = null;
                if (defaultVal != null) {
                    try {
                        memberDefault = comp.getAccessor().invoke(defaultVal);
                    } catch (Exception ignored) {
                    }
                }

                args[i] = reconstructValue(memberVal != null ? memberVal : memberDefault, comp.getType(), memberDefault);
                types[i] = comp.getType();
            }

            try {
                return recordType.getDeclaredConstructor(types).newInstance(args);
            } catch (Exception e) {
                OumLib.logError("Failed to reconstruct nested record " + targetType.getSimpleName() + ", using defaults.", e);
                return defaultVal;
            }
        }
        return val != null ? val : defaultVal;
    }

    @SuppressWarnings("unchecked")
    private static boolean isMissingKey(Map<String, Object> yamlMap, Class<?> targetType) {
        if (yamlMap == null) return true;
        if (Record.class.isAssignableFrom(targetType) && ConfigSection.class.isAssignableFrom(targetType)) {
            for (RecordComponent comp : targetType.getRecordComponents()) {
                String key = toKebab(comp.getName());
                if (!yamlMap.containsKey(key)) {
                    return true;
                }
                Object subVal = yamlMap.get(key);
                if (Record.class.isAssignableFrom(comp.getType()) && ConfigSection.class.isAssignableFrom(comp.getType())) {
                    if (subVal instanceof Map<?, ?> subMap) {
                        if (isMissingKey((Map<String, Object>) subMap, comp.getType())) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
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

        Map<String, Object> yaml = YamlParser.parse(file);

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

        T loaded = type.cast(reconstructValue(yaml, type, def));

        boolean needsSave = isMissingKey(yaml, type);
        if (needsSave) {
            save(file, loaded, unknownKeys);
        }

        return loaded;
    }

    private void save(@NonNull File file, T config, @NonNull Map<String, Object> preservedUnknown) {
        File parent = file.getParentFile();
        if (parent != null) {
            boolean ignored = parent.mkdirs();
        }
        StringBuilder yaml = new StringBuilder();

        writeRecord(yaml, config, 0);

        if (!preservedUnknown.isEmpty()) {
            yaml.append("\n# Additional keys\n");
            writeMap(yaml, preservedUnknown, 0);
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(yaml.toString());
        } catch (IOException e) {
            OumLib.logError("Failed to save " + fileName + ".", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void writeRecord(StringBuilder yaml, @NonNull Record config, int indentLevel) {
        String indent = "  ".repeat(indentLevel);
        for (RecordComponent comp : config.getClass().getRecordComponents()) {
            Comment comment = comp.getAnnotation(Comment.class);
            if (comment != null) {
                for (String line : comment.value()) {
                    yaml.append(indent).append("# ").append(line).append('\n');
                }
            }
            try {
                Object value = comp.getAccessor().invoke(config);
                String key = toKebab(comp.getName());
                if (value instanceof Record subRecord && subRecord instanceof ConfigSection) {
                    yaml.append(indent).append(key).append(":\n");
                    writeRecord(yaml, subRecord, indentLevel + 1);
                } else if (value instanceof Map<?, ?> map) {
                    yaml.append(indent).append(key).append(":\n");
                    writeMap(yaml, (Map<String, Object>) map, indentLevel + 1);
                } else {
                    yaml.append(indent).append(key).append(": ").append(toYamlValue(value)).append('\n');
                }
            } catch (Exception ignored) {
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void writeMap(StringBuilder yaml, @NonNull Map<String, Object> map, int indentLevel) {
        String indent = "  ".repeat(indentLevel);
        map.forEach((k, v) -> {
            if (v instanceof Record subRecord && subRecord instanceof ConfigSection) {
                yaml.append(indent).append(k).append(":\n");
                writeRecord(yaml, subRecord, indentLevel + 1);
            } else if (v instanceof Map<?, ?> subMap) {
                yaml.append(indent).append(k).append(":\n");
                writeMap(yaml, (Map<String, Object>) subMap, indentLevel + 1);
            } else {
                yaml.append(indent).append(k).append(": ").append(toYamlValue(v)).append('\n');
            }
        });
    }
}