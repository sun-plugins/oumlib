package dev.oum.oumlib.inventory;

import dev.oum.oumlib.event.Events;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Function;

public final class PaginatedMenu implements Menu {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final String title;
    private final int rows;
    private final int[] contentSlots;
    private final int prevSlot;
    private final int nextSlot;
    private final Function<Integer, ItemStack> prevButton;
    private final Function<Integer, ItemStack> nextButton;
    private final List<ItemStack> items;
    private final Map<UUID, Integer> pages = new HashMap<>();
    private final Map<UUID, Inventory> open = new HashMap<>();

    private PaginatedMenu(@NonNull Builder builder) {
        this.title = builder.title;
        this.rows = builder.rows;
        this.contentSlots = builder.contentSlots;
        this.prevSlot = builder.prevSlot;
        this.nextSlot = builder.nextSlot;
        this.prevButton = builder.prevButton;
        this.nextButton = builder.nextButton;
        this.items = List.copyOf(builder.items);
        registerClickListener();
    }

    public int totalPages() {
        return Math.max(1, (int) Math.ceil((double) items.size() / contentSlots.length));
    }

    @Override
    public void open(@NonNull Player player) {
        pages.putIfAbsent(player.getUniqueId(), 1);
        reopen(player);
    }

    @Override
    public void close(@NonNull Player player) {
        open.remove(player.getUniqueId());
        player.closeInventory();
    }

    public void refresh(@NonNull Player player) {
        Inventory inv = open.get(player.getUniqueId());
        if (inv == null) return;
        populateItems(inv, pages.getOrDefault(player.getUniqueId(), 1));
        player.updateInventory();
    }

    private void reopen(@NonNull Player player) {
        int page = pages.getOrDefault(player.getUniqueId(), 1);
        String resolvedTitle = title
                .replace("<page>", String.valueOf(page))
                .replace("<total>", String.valueOf(totalPages()));
        Inventory inv = Bukkit.createInventory(null, rows * 9, MM.deserialize(resolvedTitle));
        populateItems(inv, page);
        open.put(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    private void populateItems(@NonNull Inventory inv, int page) {
        inv.clear();
        int start = (page - 1) * contentSlots.length;
        for (int i = 0; i < contentSlots.length; i++) {
            int idx = start + i;
            if (idx < items.size()) inv.setItem(contentSlots[i], items.get(idx));
        }
        ItemStack prev = page > 1
                ? prevButton.apply(page)
                : ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name("<gray>Previous").build();
        ItemStack next = page < totalPages()
                ? nextButton.apply(page)
                : ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name("<gray>Next").build();
        inv.setItem(prevSlot, prev);
        inv.setItem(nextSlot, next);
    }

    private void registerClickListener() {
        Events.listen(InventoryClickEvent.class, event -> {
            if (!(event.getWhoClicked() instanceof Player player)) return;
            Inventory inv = open.get(player.getUniqueId());
            if (inv == null || !event.getInventory().equals(inv)) return;
            event.setCancelled(true);
            int slot = event.getSlot();
            int page = pages.getOrDefault(player.getUniqueId(), 1);
            if (slot == prevSlot && page > 1) {
                pages.put(player.getUniqueId(), page - 1);
                reopen(player);
            } else if (slot == nextSlot && page < totalPages()) {
                pages.put(player.getUniqueId(), page + 1);
                reopen(player);
            }
        });
    }

    @Contract(" -> new")
    public static @NonNull Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String title = "<gray>Page <page>/<total>";
        private int rows = 6;
        private int[] contentSlots = {10, 11, 12, 13, 14, 15, 16};
        private int prevSlot = 45;
        private int nextSlot = 53;
        private Function<Integer, ItemStack> prevButton = _ -> ItemBuilder.of(Material.ARROW)
                .name("<gray>Previous").build();
        private Function<Integer, ItemStack> nextButton = _ -> ItemBuilder.of(Material.ARROW)
                .name("<gray>Next").build();
        private final List<ItemStack> items = new ArrayList<>();

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder rows(int rows) {
            this.rows = rows;
            return this;
        }

        public Builder contentSlots(int... slots) {
            this.contentSlots = slots;
            return this;
        }

        public Builder previousButton(Function<Integer, ItemStack> fn, int slot) {
            prevButton = fn;
            prevSlot = slot;
            return this;
        }

        public Builder nextButton(Function<Integer, ItemStack> fn, int slot) {
            nextButton = fn;
            nextSlot = slot;
            return this;
        }

        public Builder items(List<ItemStack> items) {
            this.items.addAll(items);
            return this;
        }

        @Contract(" -> new")
        public @NonNull PaginatedMenu build() {
            return new PaginatedMenu(this);
        }
    }
}