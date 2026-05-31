package dev.oum.oumlib.text.placeholder.bridge;

import dev.oum.oumlib.text.placeholder.PlaceholderRegistry;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public final class PapiPlaceholderBridge extends PlaceholderExpansion {

    private final Plugin plugin;
    private final PlaceholderRegistry registry;

    private PapiPlaceholderBridge(Plugin plugin, PlaceholderRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    public static void register(Plugin plugin, PlaceholderRegistry registry) {
        new PapiPlaceholderBridge(plugin, registry).register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getPluginMeta().getAuthors().stream().findFirst().orElse("Unknown");
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        for (String ns : registry.getNamespaces().keySet()) {
            String resolved = registry.resolve(ns, params, player, Map.of());
            if (resolved != null) return resolved;
        }
        return null;
    }
}