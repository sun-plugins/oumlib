package dev.oum.oumlib.bridge.item;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public final class NexoProvider implements ItemProvider {

    public NexoProvider() throws ClassNotFoundException {
        Class.forName("com.nexomc.nexo.api.NexoItems");
    }

    @Override
    public @NonNull String name() {
        return "nexo";
    }

    @Override
    public @NonNull Optional<ItemStack> getItem(@NonNull String id) {
        try {
            Class<?> nexoClass = Class.forName("com.nexomc.nexo.api.NexoItems");
            ItemStack item = (ItemStack) nexoClass.getMethod("getItem", String.class).invoke(null, id);
            return Optional.ofNullable(item);
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }
}
