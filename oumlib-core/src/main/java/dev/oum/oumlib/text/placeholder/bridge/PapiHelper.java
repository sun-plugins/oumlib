package dev.oum.oumlib.text.placeholder.bridge;

import dev.oum.oumlib.text.placeholder.PlaceholderRegistry;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public final class PapiHelper {
    private PapiHelper() {
    }

    public static void register(Plugin plugin, @NonNull PlaceholderRegistry registry) {
        for (String namespace : registry.getNamespaces().keySet()) {
            PapiPlaceholderBridge.register(plugin, registry, namespace);
        }
    }
}
