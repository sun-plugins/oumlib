package dev.oum.oumlib.text;

import java.util.EnumMap;
import java.util.Map;

public final class PresetRegistry {

    private final Map<Preset, String> prefixes = new EnumMap<>(Preset.class);

    public PresetRegistry() {
        prefixes.put(Preset.SUCCESS, "<dark_green>✓ <gray>");
        prefixes.put(Preset.ERROR, "<red>✗ <gray>");
        prefixes.put(Preset.INFO, "<aqua>ℹ <gray>");
        prefixes.put(Preset.WARNING, "<yellow>⚠ <gray>");
    }

    public void register(Preset preset, String prefix) {
        prefixes.put(preset, prefix);
    }

    public String prefix(Preset preset) {
        return prefixes.getOrDefault(preset, "");
    }
}