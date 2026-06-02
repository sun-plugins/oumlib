package dev.oum.oumlib.bridge.economy;

import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public final class PlayerPointsProvider implements EconomyProvider {

    private final Object api;

    public PlayerPointsProvider() throws Exception {
        Class<?> ppClass = Class.forName("org.black_ghost.playerpoints.PlayerPoints");
        Object ppInstance = ppClass.getMethod("getInstance").invoke(null);
        this.api = ppClass.getMethod("getAPI").invoke(ppInstance);
    }

    @Override
    public @NonNull String name() {
        return "playerpoints";
    }

    @Override
    public boolean has(@NonNull OfflinePlayer player, double amount) {
        return balance(player) >= amount;
    }

    @Override
    public boolean withdraw(@NonNull OfflinePlayer player, double amount) {
        try {
            int points = (int) Math.round(amount);
            return (boolean) api.getClass().getMethod("take", UUID.class, int.class).invoke(api, player.getUniqueId(), points);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deposit(@NonNull OfflinePlayer player, double amount) {
        try {
            int points = (int) Math.round(amount);
            return (boolean) api.getClass().getMethod("give", UUID.class, int.class).invoke(api, player.getUniqueId(), points);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public double balance(@NonNull OfflinePlayer player) {
        try {
            return (int) api.getClass().getMethod("look", UUID.class).invoke(api, player.getUniqueId());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
