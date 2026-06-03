package dev.oum.oumlib.bridge.item;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public final class MythicMobsProvider implements ItemProvider {

    public MythicMobsProvider() throws ClassNotFoundException {
        Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
    }

    @Override
    public @NonNull String name() {
        return "mythicmobs";
    }

    @Override
    public @NonNull Optional<ItemStack> getItem(@NonNull String id) {
        try {
            Class<?> mythicClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
            Object inst = mythicClass.getMethod("inst").invoke(null);
            Object itemManager = inst.getClass().getMethod("getItemManager").invoke(inst);
            ItemStack item = (ItemStack) itemManager.getClass().getMethod("getItemStack", String.class).invoke(itemManager, id);
            return Optional.ofNullable(item);
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }
}
