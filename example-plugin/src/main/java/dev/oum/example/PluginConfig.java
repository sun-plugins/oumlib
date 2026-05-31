package dev.oum.example;

import dev.oum.oumlib.config.Comment;
import dev.oum.oumlib.config.ConfigSection;

public record PluginConfig(
        @Comment("Prefix for all plugin chat messages")
        String chatPrefix,

        @Comment("Join message format. Supports %player%")
        String joinMessageFormat,

        @Comment("Enable periodic broadcasts")
        boolean autoBroadcastEnabled,

        @Comment("Message template for periodic broadcasts")
        String broadcastTemplate
) implements ConfigSection {
}
