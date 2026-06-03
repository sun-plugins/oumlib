package dev.oum.oumlib.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.util.Format;
import org.bukkit.Material;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Locale;

public final class Arguments {

    private Arguments() {
    }

    @Contract("_ -> new")
    public static @NonNull Argument<String> word(String name) {
        return new Argument<>(name, StringArgumentType.word(), (raw, ctx) -> (String) raw);
    }

    @Contract("_ -> new")
    public static @NonNull Argument<String> string(String name) {
        return new Argument<>(name, StringArgumentType.greedyString(), (raw, ctx) -> (String) raw);
    }

    @Contract("_ -> new")
    public static @NonNull Argument<Integer> integer(String name) {
        return new Argument<>(name, IntegerArgumentType.integer(), (raw, ctx) -> (Integer) raw);
    }

    @Contract("_, _, _ -> new")
    public static @NonNull Argument<Integer> integer(String name, int min, int max) {
        return new Argument<>(name, IntegerArgumentType.integer(min, max), (raw, ctx) -> (Integer) raw);
    }

    @Contract("_ -> new")
    public static @NonNull Argument<Double> decimal(String name) {
        return new Argument<>(name, DoubleArgumentType.doubleArg(), (raw, ctx) -> (Double) raw);
    }

    @Contract("_, _, _ -> new")
    public static @NonNull Argument<Double> decimal(String name, double min, double max) {
        return new Argument<>(name, DoubleArgumentType.doubleArg(min, max), (raw, ctx) -> (Double) raw);
    }

    @Contract("_ -> new")
    public static @NonNull Argument<Boolean> bool(String name) {
        return new Argument<>(name, BoolArgumentType.bool(), (raw, ctx) -> (Boolean) raw);
    }

    @Contract("_ -> new")
    @SuppressWarnings("DataFlowIssue")
    public static @NonNull Argument<?> player(String name) {
        try {
            Class.forName("io.papermc.paper.command.brigadier.argument.ArgumentTypes");
            return (Argument<?>) Class.forName("dev.oum.oumlib.command.platform.PaperCommandHelper")
                    .getDeclaredMethod("createPlayerArgument", String.class)
                    .invoke(null, name);
        } catch (Exception ignored) {
        }

        return new Argument<>(name, StringArgumentType.word(), (raw, ctx) -> {
            String nameStr = (String) raw;
            return OumLib.proxy().getPlayer(nameStr).orElse(null);
        });
    }

    @Contract("_ -> new")
    public static @NonNull Argument<Material> material(String name) {
        return new Argument<>(name, StringArgumentType.word(), (raw, ctx) -> {
            try {
                return Material.valueOf(raw.toString().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return null;
            }
        });
    }

    @Contract("_, _ -> new")
    public static <E extends Enum<E>> @NonNull Argument<E> enumValue(String name, Class<E> type) {
        return new Argument<>(name, StringArgumentType.word(), (raw, ctx) -> {
            try {
                return Enum.valueOf(type, raw.toString().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return null;
            }
        });
    }

    @Contract("_ -> new")
    public static @NonNull Argument<Duration> duration(String name) {
        return new Argument<>(name, StringArgumentType.word(), (raw, ctx)
                -> Format.parseDuration(raw.toString()));
    }

    @Deprecated(since = "1.0.1", forRemoval = true)
    public static Duration parseDuration(@NonNull String input) {
        return Format.parseDuration(input);
    }
}