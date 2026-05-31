package dev.oum.oumlib.text.placeholder.bridge;

import dev.oum.oumlib.text.placeholder.PlaceholderRegistry;
import dev.oum.oumlib.text.placeholder.PlaceholderSupplier;
import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public final class MiniPlaceholderBridge {

    private static final Class<?> BUKKIT_PLAYER;
    private static final Class<?> VELOCITY_PLAYER;

    static {
        Class<?> bp = null;
        try {
            bp = Class.forName("org.bukkit.entity.Player");
        } catch (ClassNotFoundException ignored) {
        }
        BUKKIT_PLAYER = bp;

        Class<?> vp = null;
        try {
            vp = Class.forName("com.velocitypowered.api.proxy.Player");
        } catch (ClassNotFoundException ignored) {
        }
        VELOCITY_PLAYER = vp;
    }

    private MiniPlaceholderBridge() {
    }

    private static boolean isPlayer(Object o) {
        return (BUKKIT_PLAYER != null && BUKKIT_PLAYER.isInstance(o))
                || (VELOCITY_PLAYER != null && VELOCITY_PLAYER.isInstance(o));
    }

    public static void register(@NonNull PlaceholderRegistry registry) {
        Expansion.Builder builder = Expansion.builder("oumlib");
        for (Map.Entry<String, Map<String, PlaceholderSupplier>> ns : registry.getNamespaces().entrySet()) {
            for (String key : ns.getValue().keySet()) {
                String tagName = ns.getKey() + "_" + key;
                String nsKey = ns.getKey();
                builder.audiencePlaceholder(tagName, (audience, queue, ctx) -> {
                    if (isPlayer(audience)) {
                        String resolved = registry.resolve(nsKey, key, audience, Map.of());
                        if (resolved != null) return Tag.inserting(Component.text(resolved));
                    }
                    return Tag.selfClosingInserting(Component.empty());
                });
            }
        }
        builder.build().register();
    }
}