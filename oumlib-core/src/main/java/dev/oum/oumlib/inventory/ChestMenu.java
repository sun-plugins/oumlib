package dev.oum.oumlib.inventory;

import dev.oum.oumlib.event.Events;
import dev.oum.oumlib.event.ListenerHandle;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ChestMenu implements Menu {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final String title;
    private final int rows;
    private final Layout layout;
    private final Map<Integer, Consumer<ClickContext>> slotHandlers;
    private final Map<Integer, Supplier<ItemStack>> slotItems;
    private final List<Integer> unlockedSlots;
    private final Consumer<Player> onClose;
    private final Map<UUID, Inventory> open = new HashMap<>();

    private ListenerHandle clickHandle;
    private ListenerHandle dragHandle;
    private ListenerHandle closeHandle;

    private ChestMenu(@NonNull Builder builder) {
        this.title = builder.title;
        this.rows = builder.rows;
        this.layout = builder.layout;
        this.slotHandlers = Map.copyOf(builder.slotHandlers);
        this.slotItems = Map.copyOf(builder.slotItems);
        this.unlockedSlots = List.copyOf(builder.unlockedSlots);
        this.onClose = builder.onClose;
    }

    @Contract(" -> new")
    public static @NonNull Builder builder() {
        return new Builder();
    }

    @Override
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, rows * 9, MM.deserialize(title));
        if (layout != null) layout.apply(inv);
        slotItems.forEach((slot, supplier) -> inv.setItem(slot, supplier.get()));
        open.put(player.getUniqueId(), inv);
        registerListeners();
        player.openInventory(inv);
    }

    @Override
    public void close(@NonNull Player player) {
        open.remove(player.getUniqueId());
        player.closeInventory();
        if (open.isEmpty()) {
            unregisterListeners();
        }
    }

    public void refresh(@NonNull Player player) {
        Inventory inv = open.get(player.getUniqueId());
        if (inv == null) return;
        if (layout != null) layout.apply(inv);
        slotItems.forEach((slot, supplier) -> inv.setItem(slot, supplier.get()));
        player.updateInventory();
    }

    public void setItem(@NonNull Player player, int slot, ItemStack item) {
        Inventory inv = open.get(player.getUniqueId());
        if (inv == null) return;
        inv.setItem(slot, item);
        player.updateInventory();
    }

    private ItemStack moveItemToUnlockedSlots(Inventory inv, ItemStack toMove) {
        ItemStack stack = toMove.clone();
        for (int slot : unlockedSlots) {
            ItemStack existing = inv.getItem(slot);
            if (existing != null && !existing.getType().isAir() && existing.isSimilar(stack)) {
                int room = existing.getMaxStackSize() - existing.getAmount();
                if (room > 0) {
                    int toAdd = Math.min(room, stack.getAmount());
                    existing.setAmount(existing.getAmount() + toAdd);
                    stack.setAmount(stack.getAmount() - toAdd);
                    if (stack.getAmount() <= 0) {
                        return null;
                    }
                }
            }
        }
        for (int slot : unlockedSlots) {
            ItemStack existing = inv.getItem(slot);
            if (existing == null || existing.getType().isAir()) {
                inv.setItem(slot, stack);
                return null;
            }
        }
        return stack;
    }

    private synchronized void registerListeners() {
        if (clickHandle != null && clickHandle.isActive()) return;

        clickHandle = Events.listen(InventoryClickEvent.class, event -> {
            if (!(event.getWhoClicked() instanceof Player player)) return;
            Inventory inv = open.get(player.getUniqueId());
            if (inv == null) return;

            if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                event.setCancelled(true);
                return;
            }

            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                if (event.getClickedInventory() != null && !event.getClickedInventory().equals(inv)) {
                    event.setCancelled(true);
                    ItemStack toMove = event.getCurrentItem();
                    if (toMove != null && !toMove.getType().isAir()) {
                        ItemStack remaining = moveItemToUnlockedSlots(inv, toMove);
                        event.setCurrentItem(remaining);
                        player.updateInventory();
                    }
                    return;
                }
            }

            if (event.getClickedInventory() == null || !event.getClickedInventory().equals(inv)) return;

            int slot = event.getSlot();
            if (!unlockedSlots.contains(slot)) {
                event.setCancelled(true);
                Consumer<ClickContext> handler = slotHandlers.get(slot);
                if (handler != null) {
                    handler.accept(new ClickContext(player, ClickAction.from(event.getClick()), slot));
                }
            } else {
                if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                        event.getAction() == InventoryAction.HOTBAR_SWAP) {
                    event.setCancelled(true);
                }
            }
        });

        dragHandle = Events.listen(InventoryDragEvent.class, event -> {
            if (!(event.getWhoClicked() instanceof Player player)) return;
            Inventory inv = open.get(player.getUniqueId());
            if (inv == null) return;
            if (!event.getInventory().equals(inv)) return;
            boolean touchesLocked = event.getRawSlots().stream()
                    .anyMatch(s -> s < inv.getSize() && !unlockedSlots.contains(s));
            if (touchesLocked) {
                event.setCancelled(true);
            }
        });

        closeHandle = Events.listen(InventoryCloseEvent.class, event -> {
            if (!(event.getPlayer() instanceof Player player)) return;
            Inventory inv = open.get(player.getUniqueId());
            if (inv != null && event.getInventory().equals(inv)) {
                if (onClose != null) {
                    onClose.accept(player);
                }
                open.remove(player.getUniqueId());
                if (open.isEmpty()) {
                    unregisterListeners();
                }
            }
        });
    }

    private synchronized void unregisterListeners() {
        if (clickHandle != null) {
            clickHandle.unregister();
            clickHandle = null;
        }
        if (dragHandle != null) {
            dragHandle.unregister();
            dragHandle = null;
        }
        if (closeHandle != null) {
            closeHandle.unregister();
            closeHandle = null;
        }
    }

    public static final class Builder {

        private final Map<Integer, Consumer<ClickContext>> slotHandlers = new HashMap<>();
        private final Map<Integer, Supplier<ItemStack>> slotItems = new HashMap<>();
        private final List<Integer> unlockedSlots = new ArrayList<>();
        private String title = "<gray>Menu";
        private int rows = 3;
        private Layout layout;
        private Consumer<Player> onClose;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder rows(int rows) {
            if (rows < 1 || rows > 6) throw new IllegalArgumentException("Rows must be 1–6.");
            this.rows = rows;
            return this;
        }

        public Builder pattern(String... rows) {
            this.layout = new Layout(rows);
            return this;
        }

        public Builder bind(char key, ItemStack item) {
            if (layout != null) layout.bind(key, item);
            return this;
        }

        public Builder bind(char key, Supplier<ItemStack> supplier) {
            if (layout != null) layout.bind(key, supplier);
            return this;
        }

        public Builder item(int slot, ItemStack item) {
            this.slotItems.put(slot, () -> item);
            return this;
        }

        public Builder item(int slot, Supplier<ItemStack> supplier) {
            this.slotItems.put(slot, supplier);
            return this;
        }

        @Contract("_ -> this")
        public Builder unlock(int @NonNull ... slots) {
            for (int s : slots) {
                this.unlockedSlots.add(s);
            }
            return this;
        }

        public Builder unlock(@NonNull List<Integer> slots) {
            this.unlockedSlots.addAll(slots);
            return this;
        }

        public Builder onClose(Consumer<Player> onClose) {
            this.onClose = onClose;
            return this;
        }

        public Builder onClick(int slot, Consumer<ClickContext> handler) {
            slotHandlers.put(slot, handler);
            return this;
        }

        public Builder onClick(char key, Consumer<ClickContext> handler) {
            if (layout != null) {
                layout.slotsFor(key).forEach(slot -> slotHandlers.put(slot, handler));
            }
            return this;
        }

        @Contract("_, _ -> this")
        public Builder onClick(@NonNull List<Integer> slots, Consumer<ClickContext> handler) {
            slots.forEach(slot -> slotHandlers.put(slot, handler));
            return this;
        }

        @Contract(" -> new")
        public @NonNull ChestMenu build() {
            return new ChestMenu(this);
        }
    }
}