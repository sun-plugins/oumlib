package dev.oum.oumlib.text.placeholder;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class PlaceholderRegistry {

    private final Map<String, Map<String, PlaceholderSupplier>> namespaces = new HashMap<>();
    private String currentNamespace;

    public PlaceholderRegistry forNamespace(String namespace) {
        this.currentNamespace = namespace;
        namespaces.putIfAbsent(namespace, new HashMap<>());
        return this;
    }

    public PlaceholderRegistry add(String key, Function<Object, String> fn) {
        ensureNamespace();
        namespaces.get(currentNamespace).put(key, PlaceholderSupplier.ofPlayer(fn));
        return this;
    }

    public PlaceholderRegistry add(String key, BiFunction<Object, Map<String, String>, String> fn) {
        ensureNamespace();
        namespaces.get(currentNamespace).put(key, PlaceholderSupplier.ofParam(fn));
        return this;
    }

    public PlaceholderRegistry addGlobal(String key, java.util.function.Supplier<String> fn) {
        ensureNamespace();
        namespaces.get(currentNamespace).put(key, PlaceholderSupplier.ofGlobal(fn));
        return this;
    }

    public void register() {
    }

    public @Nullable String resolve(String namespace, String key, Object player, Map<String, String> params) {
        Map<String, PlaceholderSupplier> suppliers = namespaces.get(namespace);
        if (suppliers == null) return null;
        PlaceholderSupplier supplier = suppliers.get(key);
        if (supplier == null) return null;
        return switch (supplier) {
            case PlaceholderSupplier.PlayerSupplier ps -> ps.fn().apply(player);
            case PlaceholderSupplier.ParamSupplier ps -> ps.fn().apply(player, params);
            case PlaceholderSupplier.GlobalSupplier gs -> gs.fn().get();
        };
    }

    @Contract(pure = true)
    public @NonNull @Unmodifiable Map<String, Map<String, PlaceholderSupplier>> getNamespaces() {
        return Map.copyOf(namespaces);
    }

    private void ensureNamespace() {
        if (currentNamespace == null) throw new IllegalStateException("Call forNamespace() first.");
    }
}