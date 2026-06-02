package dev.oum.oumlib.text.placeholder;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface PlaceholderSupplier {

    @Contract("_ -> new")
    static @NonNull PlaceholderSupplier ofPlayer(Function<Object, String> fn) {
        return new PlayerSupplier(fn);
    }

    @Contract("_ -> new")
    static @NonNull PlaceholderSupplier ofParam(BiFunction<Object, Map<String, String>, String> fn) {
        return new ParamSupplier(fn);
    }

    @Contract("_ -> new")
    static @NonNull PlaceholderSupplier ofGlobal(Supplier<String> fn) {
        return new GlobalSupplier(fn);
    }

    record PlayerSupplier(Function<Object, String> fn) implements PlaceholderSupplier {
    }

    record ParamSupplier(BiFunction<Object, Map<String, String>, String> fn) implements PlaceholderSupplier {
    }

    record GlobalSupplier(Supplier<String> fn) implements PlaceholderSupplier {
    }
}