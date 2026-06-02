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

    /**
     * Registers a custom EconomyProvider to the bridge.
     */
    public static void registerProvider(@NonNull EconomyProvider provider) {
        providers.put(provider.name().toLowerCase(), provider);
    }

    /**
     * Retrieves a registered provider by its identifier name.
     */
    public static @NonNull Optional<EconomyProvider> getProvider(@NonNull String name) {
        return Optional.ofNullable(providers.get(name.toLowerCase()));
    }

    /**
     * Retrieves the current default provider.
     */
    public static @NonNull Optional<EconomyProvider> getDefaultProvider() {
        return Optional.ofNullable(providers.get(defaultProviderName));
    }

    /**
     * Sets the default provider name.
     */
    public static void setDefaultProvider(@NonNull String name) {
        defaultProviderName = name.toLowerCase();
    }

    /**
     * Gets player balance using the default economy provider.
     */
    public static double balance(@NonNull OfflinePlayer player) {
        return getDefaultProvider().map(p -> p.balance(player)).orElse(0.0);
    }

    /**
     * Gets player balance using a specific economy provider.
     */
    public static double balance(@NonNull String providerName, @NonNull OfflinePlayer player) {
        return getProvider(providerName).map(p -> p.balance(player)).orElse(0.0);
    }

    /**
     * Checks if player has balance using the default economy provider.
     */
    public static boolean has(@NonNull OfflinePlayer player, double amount) {
        return getDefaultProvider().map(p -> p.has(player, amount)).orElse(false);
    }

    /**
     * Checks if player has balance using a specific economy provider.
     */
    public static boolean has(@NonNull String providerName, @NonNull OfflinePlayer player, double amount) {
        return getProvider(providerName).map(p -> p.has(player, amount)).orElse(false);
    }

    /**
     * Withdraws from player using the default economy provider.
     */
    public static boolean withdraw(@NonNull OfflinePlayer player, double amount) {
        return getDefaultProvider().map(p -> p.withdraw(player, amount)).orElse(false);
    }

    /**
     * Withdraws from player using a specific economy provider.
     */
    public static boolean withdraw(@NonNull String providerName, @NonNull OfflinePlayer player, double amount) {
        return getProvider(providerName).map(p -> p.withdraw(player, amount)).orElse(false);
    }

    /**
     * Deposits to player using the default economy provider.
     */
    public static boolean deposit(@NonNull OfflinePlayer player, double amount) {
        return getDefaultProvider().map(p -> p.deposit(player, amount)).orElse(false);
    }

    /**
     * Deposits to player using a specific economy provider.
     */
    public static boolean deposit(@NonNull String providerName, @NonNull OfflinePlayer player, double amount) {
        return getProvider(providerName).map(p -> p.deposit(player, amount)).orElse(false);
    }
}
