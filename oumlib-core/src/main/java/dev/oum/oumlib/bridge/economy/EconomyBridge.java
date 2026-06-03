package dev.oum.oumlib.bridge.economy;

import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class EconomyBridge {

    private static final Map<String, EconomyProvider> providers = new HashMap<>();
    private static String defaultProviderName = "vault";

    static {
        try {
            registerProvider(new VaultProvider());
        } catch (Throwable ignored) {
        }

        try {
            registerProvider(new PlayerPointsProvider());
        } catch (Throwable ignored) {
        }

        if (providers.containsKey("vault")) {
            defaultProviderName = "vault";
        } else if (providers.containsKey("playerpoints")) {
            defaultProviderName = "playerpoints";
        } else if (!providers.isEmpty()) {
            defaultProviderName = providers.keySet().iterator().next();
        }
    }

    private EconomyBridge() {
    }

    public static void registerProvider(@NonNull EconomyProvider provider) {
        providers.put(provider.name().toLowerCase(), provider);
    }

    public static @NonNull Optional<EconomyProvider> getProvider(@NonNull String name) {
        return Optional.ofNullable(providers.get(name.toLowerCase()));
    }

    public static @NonNull Optional<EconomyProvider> getDefaultProvider() {
        return Optional.ofNullable(providers.get(defaultProviderName));
    }

    public static void setDefaultProvider(@NonNull String name) {
        defaultProviderName = name.toLowerCase();
    }

    public static double balance(@NonNull OfflinePlayer player) {
        return getDefaultProvider().map(p -> p.balance(player)).orElse(0.0);
    }

    public static double balance(@NonNull String providerName, @NonNull OfflinePlayer player) {
        return getProvider(providerName).map(p -> p.balance(player)).orElse(0.0);
    }

    public static boolean has(@NonNull OfflinePlayer player, double amount) {
        return getDefaultProvider().map(p -> p.has(player, amount)).orElse(false);
    }

    public static boolean has(@NonNull String providerName, @NonNull OfflinePlayer player, double amount) {
        return getProvider(providerName).map(p -> p.has(player, amount)).orElse(false);
    }

    public static boolean withdraw(@NonNull OfflinePlayer player, double amount) {
        return getDefaultProvider().map(p -> p.withdraw(player, amount)).orElse(false);
    }

    public static boolean withdraw(@NonNull String providerName, @NonNull OfflinePlayer player, double amount) {
        return getProvider(providerName).map(p -> p.withdraw(player, amount)).orElse(false);
    }

    public static boolean deposit(@NonNull OfflinePlayer player, double amount) {
        return getDefaultProvider().map(p -> p.deposit(player, amount)).orElse(false);
    }

    public static boolean deposit(@NonNull String providerName, @NonNull OfflinePlayer player, double amount) {
        return getProvider(providerName).map(p -> p.deposit(player, amount)).orElse(false);
    }
}
