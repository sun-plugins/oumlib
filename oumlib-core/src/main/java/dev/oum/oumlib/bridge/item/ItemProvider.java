package dev.oum.oumlib.bridge.item;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public interface ItemProvider {

    /**
     * The namespace name of this provider (e.g. "itemsadder", "oraxen").
     */
    @NonNull String name();

    /**
     * Resolves the custom item stack by its identifier.
     */
    @NonNull Optional<ItemStack> getItem(@NonNull String id);
}
