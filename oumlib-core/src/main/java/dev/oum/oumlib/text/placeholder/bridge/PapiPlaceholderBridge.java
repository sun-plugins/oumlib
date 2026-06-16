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
    private final String namespace;

    private PapiPlaceholderBridge(Plugin plugin, PlaceholderRegistry registry, String namespace) {
        this.plugin = plugin;
        this.registry = registry;
        this.namespace = namespace;
    }

    public static void register(Plugin plugin, PlaceholderRegistry registry, String namespace) {
        new PapiPlaceholderBridge(plugin, registry, namespace).register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return namespace;
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getPluginMeta().getAuthors().stream().findFirst().orElse("sun-dev");
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
        return registry.resolve(namespace, params, player, Map.of());
    }
}