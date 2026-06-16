package dev.oum.oumlib.config;

import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public final class ConfigMigrationRegistry {
    private final TreeMap<Integer, Consumer<Map<String, Object>>> migrations = new TreeMap<>();

    public @NonNull ConfigMigrationRegistry add(int targetVersion, @NonNull Consumer<Map<String, Object>> migrationAction) {
        migrations.put(targetVersion, migrationAction);
        return this;
    }

    public TreeMap<Integer, Consumer<Map<String, Object>>> migrations() {
        return migrations;
    }
}
