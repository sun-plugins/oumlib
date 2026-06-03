package dev.oum.oumlib.bridge.item;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public interface ItemProvider {

    @NonNull String name();

    @NonNull Optional<ItemStack> getItem(@NonNull String id);
}
