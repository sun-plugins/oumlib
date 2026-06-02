package dev.oum.oumlib.bridge.economy;

import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;

public interface EconomyProvider {

    /**
     * The unique name/identifier of this economy provider (e.g. "vault", "playerpoints").
     */
    @NonNull String name();

    /**
     * Checks if the player has at least the specified amount.
     */
    boolean has(@NonNull OfflinePlayer player, double amount);

    /**
     * Withdraws the specified amount from the player's account.
     * Returns true if successful.
     */
    boolean withdraw(@NonNull OfflinePlayer player, double amount);

    /**
     * Deposits the specified amount into the player's account.
     * Returns true if successful.
     */
    boolean deposit(@NonNull OfflinePlayer player, double amount);

    /**
     * Retrieves the player's current balance.
     */
    double balance(@NonNull OfflinePlayer player);
}
