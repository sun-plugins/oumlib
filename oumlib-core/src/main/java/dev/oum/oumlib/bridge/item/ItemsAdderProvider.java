package dev.oum.oumlib.bridge.item;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public final class ItemsAdderProvider implements ItemProvider {

    public ItemsAdderProvider() throws ClassNotFoundException {
        Class.forName("dev.lone.itemsadder.api.CustomStack");
    }

    @Override
    public @NonNull String name() {
        return "itemsadder";
    }

    @Override
    public @NonNull Optional<ItemStack> getItem(@NonNull String id) {
        try {
            Class<?> csClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
            Object customStack = csClass.getMethod("getInstance", String.class).invoke(null, id);
            if (customStack != null) {
                ItemStack item = (ItemStack) customStack.getClass().getMethod("getItemStack").invoke(customStack);
                return Optional.ofNullable(item);
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }
}
