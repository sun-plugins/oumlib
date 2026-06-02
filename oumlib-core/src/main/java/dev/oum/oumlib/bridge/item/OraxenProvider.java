package dev.oum.oumlib.bridge.item;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public final class OraxenProvider implements ItemProvider {

    public OraxenProvider() throws ClassNotFoundException {
        Class.forName("io.thartmann.oraxen.api.OraxenItems");
    }

    @Override
    public @NonNull String name() {
        return "oraxen";
    }

    @Override
    public @NonNull Optional<ItemStack> getItem(@NonNull String id) {
        try {
            Class<?> oraxenClass = Class.forName("io.thartmann.oraxen.api.OraxenItems");
            ItemStack item = (ItemStack) oraxenClass.getMethod("getItemById", String.class).invoke(null, id);
            return Optional.ofNullable(item);
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }
}
