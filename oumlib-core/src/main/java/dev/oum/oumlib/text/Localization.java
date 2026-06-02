package dev.oum.oumlib.text;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.config.YamlParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public final class Localization {

    private static final Map<String, Map<String, String>> translations = new HashMap<>();
    private static String defaultLang = "en";

    private Localization() {
    }

    /**
     * Initializes the localization system by loading all .yml language files in the "lang" directory.
     * Extracts the default resource (e.g. "lang/en.yml") if the directory is empty.
     */
    public static void load(@NonNull String defaultLanguageCode) {
        defaultLang = defaultLanguageCode.toLowerCase();
        translations.clear();

        File dataFolder = OumLib.getDataFolder();
        File langFolder = new File(dataFolder, "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        // Try extracting default resource from classloader
        String defaultFileName = "lang/" + defaultLang + ".yml";
        File defaultFile = new File(dataFolder, defaultFileName);
        if (!defaultFile.exists()) {
            try (InputStream in = Localization.class.getClassLoader().getResourceAsStream(defaultFileName)) {
                if (in != null) {
                    Files.copy(in, defaultFile.toPath());
                }
            } catch (Exception ignored) {
            }
        }

        // Load all .yml / .yaml files in lang directory
        File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                String langCode = name.substring(0, name.lastIndexOf('.')).toLowerCase();
                try {
                    Map<String, Object> parsed = YamlParser.parse(file);
                    Map<String, String> langMap = new HashMap<>();
                    flatten("", parsed, langMap);
                    translations.put(langCode, langMap);
                } catch (Exception e) {
                    OumLib.logError("Failed to load localization file: " + file.getName(), e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void flatten(@NonNull String prefix, @NonNull Map<String, Object> map, @NonNull Map<String, String> target) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> subMap) {
                flatten(key, (Map<String, Object>) subMap, target);
            } else if (value != null) {
                target.put(key, String.valueOf(value));
            }
        }
    }

    /**
     * Translates a key using the default language, resolving MiniMessage tags.
     */
    public static @NonNull Component translate(@NonNull String key, TagResolver... resolvers) {
        return translateFor(defaultLang, key, resolvers);
    }

    /**
     * Translates a key for a specific language locale code, resolving MiniMessage tags.
     * Falls back to the default language if the key or locale is missing.
     */
    public static @NonNull Component translateFor(@NonNull String lang, @NonNull String key, TagResolver... resolvers) {
        String message = getRaw(lang, key);
        if (message == null) {
            return Component.text(key);
        }
        return MiniMessage.miniMessage().deserialize(message, resolvers);
    }

    /**
     * Translates a key using the locale of a specific Player (handles Bukkit or Velocity Player objects).
     */
    @SuppressWarnings("unchecked")
    public static @NonNull Component translateFor(@NonNull Object playerObj, @NonNull String key, TagResolver... resolvers) {
        String locale = defaultLang;
        try {
            if (playerObj instanceof org.bukkit.entity.Player p) {
                locale = p.locale().getLanguage();
            } else {
                Class<?> velocityPlayerClass = Class.forName("com.velocitypowered.api.proxy.Player");
                if (velocityPlayerClass.isInstance(playerObj)) {
                    Object profile = playerObj.getClass().getMethod("getPlayerProfile").invoke(playerObj);
                    if (profile != null) {
                        java.util.Optional<java.util.Locale> optLocale = (java.util.Optional<java.util.Locale>) profile.getClass().getMethod("getLocale").invoke(profile);
                        if (optLocale != null && optLocale.isPresent()) {
                            locale = optLocale.get().getLanguage();
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return translateFor(locale, key, resolvers);
    }

    /**
     * Safely gets the raw message string without deserialization, with fallback logic.
     */
    public static @Nullable String getRaw(@NonNull String lang, @NonNull String key) {
        String langCode = lang.toLowerCase();
        Map<String, String> map = translations.get(langCode);
        if (map != null && map.containsKey(key)) {
            return map.get(key);
        }
        // Fallback to default lang
        Map<String, String> defaultMap = translations.get(defaultLang);
        if (defaultMap != null) {
            return defaultMap.get(key);
        }
        return null;
    }
}
