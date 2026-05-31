package dev.oum.oumlib.command;

import com.mojang.brigadier.context.CommandContext;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public final class ArgumentMap {

    private final CommandContext<?> ctx;
    private final Map<String, Object> cache = new HashMap<>();

    public ArgumentMap(CommandContext<?> ctx) {
        this.ctx = ctx;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(@NonNull Argument<T> argument) {
        return (T) cache.computeIfAbsent(argument.name(), k -> {
            try {
                Object raw = ctx.getArgument(k, Object.class);
                return argument.extractor().apply(raw, ctx);
            } catch (IllegalArgumentException e) {
                return null;
            }
        });
    }

    public CommandContext<?> raw() {
        return ctx;
    }
}