package dev.oum.oumlib.text.placeholder.bridge;

import dev.oum.oumlib.text.placeholder.PlaceholderRegistry;

public final class MiniPlaceholdersHelper {
    private MiniPlaceholdersHelper() {}

    public static void register(PlaceholderRegistry registry) {
        MiniPlaceholderBridge.register(registry);
    }
}
