package dev.oum.oumlib.inventory;

import dev.oum.oumlib.event.Events;
import dev.oum.oumlib.event.ListenerHandle;
import dev.oum.oumlib.scheduler.Scheduler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings({"UnstableApiUsage", "unused"})
@ApiStatus.Obsolete
public final class AnvilMenu implements Menu {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final String title;
    private final String placeholder;
    private final BiConsumer<Player, String> onConfirm;
    private final Consumer<Player> onClose;
    private final Map<UUID, Inventory> open = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> originalLevels = new ConcurrentHashMap<>();
    private final Map<UUID, Float> originalExp = new ConcurrentHashMap<>();

    private ListenerHandle clickHandle;
    private ListenerHandle closeHandle;
    private ListenerHandle quitHandle;
    private ListenerHandle prepareHandle;

    private AnvilMenu(@NonNull Builder builder) {
        this.title = builder.title;
        this.placeholder = builder.placeholder;
        this.onConfirm = builder.onConfirm;
        this.onClose = builder.onClose;
    }

    @Contract(value = " -> new", pure = true)
    @CheckReturnValue
    public static @NonNull Builder builder() {
        return new Builder();
    }

    @Override
    public void open(@NonNull Player player) {
        Scheduler.runFor(player, () -> {
            Inventory inv = player.getServer().createInventory(player, InventoryType.ANVIL, MM.deserialize(title));
            inv.setItem(0, ItemBuilder.of(Material.PAPER).name(placeholder).build());

            originalLevels.put(player.getUniqueId(), player.getLevel());
            originalExp.put(player.getUniqueId(), player.getExp());
            if (player.getLevel() < 1) {
                player.setLevel(1);
                player.setExp(0.0f);
            }

            open.put(player.getUniqueId(), inv);
            registerListeners();
            player.openInventory(inv);
        });
    }

    @Override
    public void close(@NonNull Player player) {
        Scheduler.runFor(player, () -> {
            open.remove(player.getUniqueId());
            restoreExperience(player);
            player.closeInventory();
            if (open.isEmpty()) {
                unregisterListeners();
            }
        });
    }

    private void restoreExperience(@NonNull Player player) {
        Integer lvl = originalLevels.remove(player.getUniqueId());
        Float exp = originalExp.remove(player.getUniqueId());
        if (lvl != null && exp != null) {
            player.setLevel(lvl);
            player.setExp(exp);
        }
    }

    private synchronized void registerListeners() {
        if (clickHandle != null && clickHandle.isActive()) return;
        MenuRegistry.register(this);

        clickHandle = Events.listen(InventoryClickEvent.class, event -> {
            if (!(event.getWhoClicked() instanceof Player player)) return;
            Inventory inv = open.get(player.getUniqueId());
            if (inv == null) return;
            if (event.getClickedInventory() == null || !event.getClickedInventory().equals(inv)) {
                event.setCancelled(true);
                return;
            }
            if (event.getSlot() != 2) {
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
            if (!(event.getView() instanceof AnvilView anvilView)) return;
            var input = anvilView.getRenameText();
            open.remove(player.getUniqueId());
            restoreExperience(player);
            player.closeInventory();
            if (open.isEmpty()) {
                unregisterListeners();
            }
            if (onConfirm != null) onConfirm.accept(player, input != null ? input : "");
        });

        closeHandle = Events.listen(InventoryCloseEvent.class, event -> {
            if (!(event.getPlayer() instanceof Player player)) return;
            if (open.remove(player.getUniqueId()) != null) {
                restoreExperience(player);
                if (open.isEmpty()) {
                    unregisterListeners();
                }
                if (onClose != null) {
                    onClose.accept(player);
                }
            }
        });

        quitHandle = Events.listen(PlayerQuitEvent.class, event -> {
            Player player = event.getPlayer();
            if (open.remove(player.getUniqueId()) != null) {
                restoreExperience(player);
                if (open.isEmpty()) {
                    unregisterListeners();
                }
            }
        });

        prepareHandle = Events.listen(PrepareAnvilEvent.class, event -> {
            if (!(event.getView().getPlayer() instanceof Player player)) return;
            Inventory openInv = open.get(player.getUniqueId());
            if (openInv == null) return;
            if (!event.getInventory().equals(openInv)) return;
            AnvilView anvilView = event.getView();
            AnvilInventory inv = event.getInventory();
            ItemStack first = inv.getItem(0);
            if (first == null) return;

            String renameText = anvilView.getRenameText();
            if (renameText == null) {
                renameText = "";
            }

            ItemStack result = ItemBuilder.of(first.getType()).name(renameText).build();
            event.setResult(result);

            anvilView.setRepairCost(0);
            Scheduler.run(() -> {
                anvilView.setRepairCost(0);
            });
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
        if (quitHandle != null) {
            quitHandle.unregister();
            quitHandle = null;
        }
        if (prepareHandle != null) {
            prepareHandle.unregister();
            prepareHandle = null;
        }
    }

    public void closeAll() {
        new ArrayList<>(open.keySet()).forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                restoreExperience(p);
                p.closeInventory();
            }
        });
        open.clear();
        originalLevels.clear();
        originalExp.clear();
        unregisterListeners();
    }

    public static final class Builder {

        private String title = "Search";
        private String placeholder = "Enter text...";
        private BiConsumer<Player, String> onConfirm;
        private Consumer<Player> onClose;

        @CheckReturnValue
        public @NonNull Builder title(@NonNull String title) {
            this.title = title;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder placeholder(@NonNull String p) {
            this.placeholder = p;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder onConfirm(@NonNull BiConsumer<@NonNull Player, @NonNull String> handler) {
            this.onConfirm = handler;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder onClose(@Nullable Consumer<@NonNull Player> handler) {
            this.onClose = handler;
            return this;
        }

        @Contract(" -> new")
        public @NonNull AnvilMenu build() {
            return new AnvilMenu(this);
        }
    }
}