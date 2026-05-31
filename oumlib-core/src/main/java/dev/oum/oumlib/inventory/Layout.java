package dev.oum.oumlib.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class Layout {

    private final String[] pattern;
    private final Map<Character, Supplier<ItemStack>> bindings = new HashMap<>();
    private final Map<Character, List<Integer>> slotMap = new HashMap<>();

    public Layout(String @NonNull ... rows) {
        if (rows.length > 6) throw new IllegalArgumentException("Max 6 rows.");
        this.pattern = rows;
        buildSlotMap();
    }

    public Layout bind(char key, ItemStack item) {
        bindings.put(key, () -> item);
        return this;
    }

    public Layout bind(char key, Supplier<ItemStack> supplier) {
        bindings.put(key, supplier);
        return this;
    }

    public List<Integer> slotsFor(char key) {
        return slotMap.getOrDefault(key, List.of());
    }

    public void apply(Inventory inventory) {
        bindings.forEach((key, supplier) ->
                slotsFor(key).forEach(slot -> inventory.setItem(slot, supplier.get()))
        );
    }

    private void buildSlotMap() {
        for (int row = 0; row < pattern.length; row++) {
            String line = pattern[row];
            for (int col = 0; col < Math.min(line.length(), 9); col++) {
                char c = line.charAt(col);
                int slot = row * 9 + col;
                slotMap.computeIfAbsent(c, _ -> new ArrayList<>()).add(slot);
            }
        }
    }
}