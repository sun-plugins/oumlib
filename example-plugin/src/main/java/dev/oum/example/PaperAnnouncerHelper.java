package dev.oum.example;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.event.Events;
import dev.oum.oumlib.inventory.ChestMenu;
import dev.oum.oumlib.inventory.ItemBuilder;
import dev.oum.oumlib.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PaperAnnouncerHelper {

    private static ChestMenu adminMenu;

    private PaperAnnouncerHelper() {
    }

    public static void sendActionbarToAll(String actionbarMessage) {
        OumLib.plugin().getServer().getOnlinePlayers().forEach(player ->
                Text.actionBar(player, actionbarMessage)
        );
    }

    public static void registerOnceListener(String message) {
        Events.listenOnce(PlayerJoinEvent.class, event ->
                Text.Preset.info(event.getPlayer(), "<yellow>Special Notice: </yellow>" + message)
        );
    }

    public static void broadcast(Component component) {
        OumLib.plugin().getServer().sendMessage(component);
    }

    public static void openMenu(Object playerObject) {
        if (!(playerObject instanceof Player player)) return;
        if (adminMenu == null) {
            adminMenu = ChestMenu.builder()
                    .title("<gold>OumLib Dashboard</gold>")
                    .rows(3)
                    .pattern(
                            "#########",
                            "# C B S #",
                            "#########"
                    )
                    .bind('#', ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build())
                    .bind('C', () -> ItemBuilder.of(Material.BOOK)
                            .name("<yellow>Auto-Broadcast Status</yellow>")
                            .lore(
                                    "Auto-Broadcast: " + (ExampleAnnouncer.isAutoBroadcastEnabled() ? "<green>Enabled</green>" : "<red>Disabled</red>"),
                                    "Click to toggle"
                            )
                            .build())
                    .bind('B', ItemBuilder.of(Material.PAPER)
                            .name("<green>Trigger Manual Broadcast</green>")
                            .lore("Click to send broadcast")
                            .build())
                    .bind('S', ItemBuilder.of(Material.COMPASS)
                            .name("<aqua>Server Statistics</aqua>")
                            .lore(
                                    "Joins Handled: <gold>" + ExampleAnnouncer.getJoinCount() + "</gold>",
                                    "Broadcasts Sent: <gold>" + ExampleAnnouncer.getBroadcastCount() + "</gold>",
                                    "Click to close"
                            )
                            .build())
                    .onClick('C', context -> {
                        ExampleAnnouncer.toggleAutoBroadcast();
                        adminMenu.refresh(context.player());
                    })
                    .onClick('B', context -> {
                        ExampleAnnouncer.triggerManualBroadcast();
                        adminMenu.refresh(context.player());
                    })
                    .onClick('S', context -> context.player().closeInventory())
                    .build();
        }
        adminMenu.open(player);
    }
}
