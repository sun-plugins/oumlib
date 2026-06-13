package dev.oum.oumlib.config;

import dev.oum.oumlib.OumLib;
import org.jspecify.annotations.NonNull;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class YamlParser {

    private YamlParser() {
    }

    public static @NonNull Map<String, Object> parse(@NonNull File file) {
        if (!file.exists()) return new LinkedHashMap<>();
        try (InputStream stream = new FileInputStream(file)) {
            return parse(stream);
        } catch (Exception e) {
            OumLib.logError("Failed to parse YAML file: " + file.getPath(), e);
            return new LinkedHashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    public static @NonNull Map<String, Object> parse(@NonNull InputStream stream) {
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            Yaml yaml = new Yaml();
            Object parsed = yaml.load(reader);
            if (parsed instanceof Map) {
                return (Map<String, Object>) parsed;
            }
            return new LinkedHashMap<>();
        } catch (Exception e) {
            OumLib.logError("Failed to parse YAML stream", e);
            return new LinkedHashMap<>();
        }
    }
}
