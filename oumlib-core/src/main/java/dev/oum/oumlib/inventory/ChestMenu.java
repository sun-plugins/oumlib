package dev.oum.oumlib.inventory;

import dev.oum.oumlib.event.Events;
import dev.oum.oumlib.event.ListenerHandle;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

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
    private final Map<UUID, Inventory> open = new HashMap<>();

    private ListenerHandle clickHandle;
    private ListenerHandle closeHandle;

    private ChestMenu(@NonNull Builder builder) {
        this.title = builder.title;
        this.rows = builder.rows;
        this.layout = builder.layout;
        this.slotHandlers = Map.copyOf(builder.slotHandlers);
    }

    @Contract(" -> new")
    public static @NonNull Builder builder() {
        return new Builder();
    }

    @Override
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, rows * 9, MM.deserialize(title));
        if (layout != null) layout.apply(inv);
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
        player.updateInventory();
    }

    public void setItem(@NonNull Player player, int slot, ItemStack item) {
        Inventory inv = open.get(player.getUniqueId());
        if (inv == null) return;
        inv.setItem(slot, item);
        player.updateInventory();
    }

    private synchronized void registerListeners() {
        if (clickHandle != null && clickHandle.isActive()) return;

        clickHandle = Events.listen(InventoryClickEvent.class, event -> {
            if (!(event.getWhoClicked() instanceof Player player)) return;
            Inventory inv = open.get(player.getUniqueId());
            if (inv == null) return;
            if (event.getClickedInventory() == null || !event.getClickedInventory().equals(inv)) return;
            event.setCancelled(true);
            Consumer<ClickContext> handler = slotHandlers.get(event.getSlot());
            if (handler != null) {
                handler.accept(new ClickContext(player, ClickAction.from(event.getClick()), event.getSlot()));
            }
        });

        closeHandle = Events.listen(InventoryCloseEvent.class, event -> {
            if (!(event.getPlayer() instanceof Player player)) return;
            Inventory inv = open.get(player.getUniqueId());
            if (inv != null && event.getInventory().equals(inv)) {
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
        if (closeHandle != null) {
            closeHandle.unregister();
            closeHandle = null;
        }
    }

    public static final class Builder {

        private final Map<Integer, Consumer<ClickContext>> slotHandlers = new HashMap<>();
        private String title = "<gray>Menu";
        private int rows = 3;
        private Layout layout;

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