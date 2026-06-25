package dev.oum.oumlib.inventory;

import dev.oum.oumlib.event.Events;
import dev.oum.oumlib.event.ListenerHandle;
import dev.oum.oumlib.scheduler.Scheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
    private final Function<Player, List<ItemStack>> itemsSupplier;
    private final PaginatedClickHandler clickHandler;
    private final Map<UUID, Integer> pages = new ConcurrentHashMap<>();
    private final Map<UUID, Inventory> open = new ConcurrentHashMap<>();

    private ListenerHandle clickHandle;
    private ListenerHandle closeHandle;

    private PaginatedMenu(@NonNull Builder builder) {
        this.title = builder.title;
        this.rows = builder.rows;
        this.contentSlots = builder.contentSlots;
        this.prevSlot = builder.prevSlot;
        this.nextSlot = builder.nextSlot;
        this.prevButton = builder.prevButton;
        this.nextButton = builder.nextButton;
        this.itemsSupplier = builder.itemsSupplier;
        this.clickHandler = builder.clickHandler;
    }

    @Contract(" -> new")
    @CheckReturnValue
    public static @NonNull Builder builder() {
        return new Builder();
    }

    public int totalPages() {
        try {
            return totalPages(null);
        } catch (Exception e) {
            return 1;
        }
    }

    public int totalPages(@Nullable Player player) {
        List<ItemStack> list = itemsSupplier.apply(player);
        int size = list != null ? list.size() : 0;
        return Math.max(1, (int) Math.ceil((double) size / contentSlots.length));
    }

    @Override
    public void open(@NonNull Player player) {
        Scheduler.runFor(player, () -> {
            pages.putIfAbsent(player.getUniqueId(), 1);
            registerListeners();
            reopen(player);
        });
    }

    @Override
    public void close(@NonNull Player player) {
        Scheduler.runFor(player, () -> {
            open.remove(player.getUniqueId());
            pages.remove(player.getUniqueId());
            player.closeInventory();
            if (open.isEmpty()) {
                unregisterListeners();
            }
        });
    }

    @SuppressWarnings("unused")
    public void refresh(@NonNull Player player) {
        Scheduler.runFor(player, () -> {
            Inventory inv = open.get(player.getUniqueId());
            if (inv == null) return;
            populateItems(player, inv, pages.getOrDefault(player.getUniqueId(), 1));
            player.updateInventory();
        });
    }

    private void reopen(@NonNull Player player) {
        Scheduler.runFor(player, () -> {
            int page = pages.getOrDefault(player.getUniqueId(), 1);
            String resolvedTitle = title
                    .replace("<page>", String.valueOf(page))
                    .replace("<total>", String.valueOf(totalPages(player)));
            var titleComponent = MM.deserialize(resolvedTitle);

            Inventory inv = open.get(player.getUniqueId());
            if (inv != null) {
                try {
                    var view = player.getOpenInventory();
                    try {
                        var method = view.getClass().getMethod("setTitle", Component.class);
                        method.invoke(view, titleComponent);
                    } catch (NoSuchMethodException e) {
                        view.setTitle(resolvedTitle);
                    }
                } catch (Exception ignored) {
                }
                populateItems(player, inv, page);
                player.updateInventory();
            } else {
                inv = Bukkit.createInventory(null, rows * 9, titleComponent);
                populateItems(player, inv, page);
                open.put(player.getUniqueId(), inv);
                player.openInventory(inv);
            }
        });
    }

    private void populateItems(@NonNull Player player, @NonNull Inventory inv, int page) {
        inv.clear();
        List<ItemStack> list = itemsSupplier.apply(player);
        if (list == null) list = List.of();
        int start = (page - 1) * contentSlots.length;
        for (int i = 0; i < contentSlots.length; i++) {
            int idx = start + i;
            if (idx < list.size()) inv.setItem(contentSlots[i], list.get(idx));
        }
        ItemStack prev = page > 1
                ? prevButton.apply(page)
                : ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name("<gray>Previous").build();
        ItemStack next = page < totalPages(player)
                ? nextButton.apply(page)
                : ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name("<gray>Next").build();
        inv.setItem(prevSlot, prev);
        inv.setItem(nextSlot, next);
    }

    private synchronized void registerListeners() {
        if (clickHandle != null && clickHandle.isActive()) return;
        MenuRegistry.register(this);

        clickHandle = Events.listen(InventoryClickEvent.class, event -> {
            if (!(event.getWhoClicked() instanceof Player player)) return;
            Inventory inv = open.get(player.getUniqueId());
            if (inv == null || !event.getInventory().equals(inv)) return;
            event.setCancelled(true);
            int slot = event.getSlot();
            int page = pages.getOrDefault(player.getUniqueId(), 1);
            if (slot == prevSlot && page > 1) {
                pages.put(player.getUniqueId(), page - 1);
                reopen(player);
                return;
            } else if (slot == nextSlot && page < totalPages(player)) {
                pages.put(player.getUniqueId(), page + 1);
                reopen(player);
                return;
            }

            if (clickHandler != null) {
                List<ItemStack> list = itemsSupplier.apply(player);
                if (list == null) list = List.of();
                for (int i = 0; i < contentSlots.length; i++) {
                    if (contentSlots[i] == slot) {
                        int idx = (page - 1) * contentSlots.length + i;
                        if (idx < list.size()) {
                            clickHandler.onClick(
                                    new ClickContext(player, ClickAction.from(event.getClick()), slot, this),
                                    list.get(idx),
                                    idx
                            );
                        }
                        break;
                    }
                }
            }
        });

        closeHandle = Events.listen(InventoryCloseEvent.class, event -> {
            if (!(event.getPlayer() instanceof Player player)) return;
            Inventory inv = open.get(player.getUniqueId());
            if (inv != null && event.getInventory().equals(inv)) {
                open.remove(player.getUniqueId());
                pages.remove(player.getUniqueId());
                if (open.isEmpty()) {
                    unregisterListeners();
                }
            }
        });
    }

    private synchronized void unregisterListeners() {
        MenuRegistry.unregister(this);
        if (clickHandle != null) {
            clickHandle.unregister();
            clickHandle = null;
        }
        if (closeHandle != null) {
            closeHandle.unregister();
            closeHandle = null;
        }
    }

    public void closeAll() {
        new ArrayList<>(open.keySet()).forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.closeInventory();
            }
        });
        open.clear();
        pages.clear();
        unregisterListeners();
    }

    @FunctionalInterface
    public interface PaginatedClickHandler {
        void onClick(ClickContext ctx, ItemStack item, int index);
    }

    public static final class Builder {

        private Function<Player, List<ItemStack>> itemsSupplier = p -> new ArrayList<>();
        private String title = "<gray>Page <page>/<total>";
        private int rows = 6;
        private int[] contentSlots = {10, 11, 12, 13, 14, 15, 16};
        private int prevSlot = 45;
        private int nextSlot = 53;
        private Function<Integer, ItemStack> prevButton = page -> ItemBuilder.of(Material.ARROW)
                .name("<gray>Previous").build();
        private Function<Integer, ItemStack> nextButton = page -> ItemBuilder.of(Material.ARROW)
                .name("<gray>Next").build();
        private PaginatedClickHandler clickHandler;

        @CheckReturnValue
        @SuppressWarnings("unused")
        public @NonNull Builder onClick(@NonNull PaginatedClickHandler handler) {
            this.clickHandler = handler;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder title(@NonNull String title) {
            this.title = title;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder rows(int rows) {
            this.rows = rows;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder contentSlots(int @NonNull ... slots) {
            this.contentSlots = slots;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder previousButton(@NonNull Function<@NonNull Integer, @NonNull ItemStack> fn, int slot) {
            prevButton = fn;
            prevSlot = slot;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder nextButton(@NonNull Function<@NonNull Integer, @NonNull ItemStack> fn, int slot) {
            nextButton = fn;
            nextSlot = slot;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder items(@NonNull List<@NonNull ItemStack> items) {
            this.itemsSupplier = player -> items;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder items(@NonNull Function<@NonNull Player, @NonNull List<@NonNull ItemStack>> supplier) {
            this.itemsSupplier = supplier;
            return this;
        }

        @Contract(" -> new")
        public @NonNull PaginatedMenu build() {
            return new PaginatedMenu(this);
        }
    }
}