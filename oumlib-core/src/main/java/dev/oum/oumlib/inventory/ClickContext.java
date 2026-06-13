package dev.oum.oumlib.inventory;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record ClickContext(@NonNull Player player, @NonNull ClickAction action, int slot, @NonNull Menu menu) {

    @SuppressWarnings("unchecked")
    public <T> @Nullable T state(@NonNull String key) {
        if (menu instanceof ChestMenu chestMenu) {
            return (T) chestMenu.getState(player, key);
        }
        return null;
    }

    public void updateState(@NonNull String key, @Nullable Object value) {
        if (menu instanceof ChestMenu chestMenu) {
            chestMenu.updateState(player, key, value);
        }
    }
}