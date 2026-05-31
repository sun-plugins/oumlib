package dev.oum.oumlib.inventory;

import dev.oum.oumlib.event.Events;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class AnvilMenu implements Menu {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final String title;
    private final String placeholder;
    private final BiConsumer<Player, String> onConfirm;
    private final Consumer<Player> onClose;
    private final Map<UUID, Inventory> open = new HashMap<>();

    private AnvilMenu(@NonNull Builder builder) {
        this.title = builder.title;
        this.placeholder = builder.placeholder;
        this.onConfirm = builder.onConfirm;
        this.onClose = builder.onClose;
        registerListeners();
    }

    @Override
    public void open(@NonNull Player player) {
        Inventory inv = player.getServer().createInventory(player, InventoryType.ANVIL, MM.deserialize(title));
        inv.setItem(0, ItemBuilder.of(Material.PAPER).name(placeholder).build());
        open.put(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    @Override
    public void close(@NonNull Player player) {
        open.remove(player.getUniqueId());
        player.closeInventory();
    }

    private void registerListeners() {
        Events.listen(InventoryClickEvent.class, event -> {
            if (!(event.getWhoClicked() instanceof Player player)) return;
            Inventory inv = open.get(player.getUniqueId());
            if (inv == null || !event.getInventory().equals(inv)) return;
            if (event.getSlot() != 2) {
                event.setCancelled(true);
                return;
            }
            if (!(event.getView() instanceof AnvilView anvilView)) return;
            var input = anvilView.getRenameText();
            open.remove(player.getUniqueId());
            player.closeInventory();
            if (onConfirm != null) onConfirm.accept(player, input != null ? input : "");
        });

        Events.listen(InventoryCloseEvent.class, event -> {
            if (!(event.getPlayer() instanceof Player player)) return;
            if (open.remove(player.getUniqueId()) != null && onClose != null) {
                onClose.accept(player);
            }
        });
    }

    @Contract(value = " -> new", pure = true)
    public static @NonNull Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String title = "Search";
        private String placeholder = "Enter text...";
        private BiConsumer<Player, String> onConfirm;
        private Consumer<Player> onClose;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder placeholder(String p) {
            this.placeholder = p;
            return this;
        }

        public Builder onConfirm(BiConsumer<Player, String> handler) {
            this.onConfirm = handler;
            return this;
        }

        public Builder onClose(Consumer<Player> handler) {
            this.onClose = handler;
            return this;
        }

        @Contract(" -> new")
        public @NonNull AnvilMenu build() {
            return new AnvilMenu(this);
        }
    }
}