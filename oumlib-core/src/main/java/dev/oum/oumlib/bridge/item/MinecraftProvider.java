package dev.oum.oumlib.bridge.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Locale;
import java.util.Optional;

public final class MinecraftProvider implements ItemProvider {

    @Override
    public @NonNull String name() {
        return "minecraft";
    }

    @Override
    public @NonNull Optional<ItemStack> getItem(@NonNull String id) {
        try {
            Material material = Material.valueOf(id.toUpperCase(Locale.ROOT));
            return Optional.of(new ItemStack(material));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
