# GUI & Chest Menus

OumLib includes a simple, lightweight inventory menu system for Paper/Bukkit. It uses layout patterns, item bindings, and click handlers to build custom menus.

---

## Real-world Example: Virtual Coin Shop

Here is a virtual store interface that reads a player's balance dynamically, checks if they can afford an item via `EconomyBridge`, deducts the balance, updates the menu state placeholders, and plays sound effects:

```java
import dev.oum.oumlib.bridge.economy.EconomyBridge;
import dev.oum.oumlib.effect.Effects;
import dev.oum.oumlib.inventory.ChestMenu;
import dev.oum.oumlib.inventory.ItemBuilder;
import dev.oum.oumlib.text.Text;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class CoinShopMenu {
    public static void open(Player player) {
        ChestMenu.builder()
            .title("<dark_gray>Coin Shop | Coins: {coins_balance}</dark_gray>")
            .rows(3)
            .state("coins_balance", p -> (int) EconomyBridge.balance(p))
            .pattern(
                "#########",
                "#  G S  #",
                "#########"
            )
            .bind('#', ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build())
            .bind('G', () -> ItemBuilder.of(Material.GOLD_INGOT).name("<gold>Gold Pack</gold>").lore("<yellow>Price: 100 points</yellow>").build())
            .bind('S', () -> ItemBuilder.of(Material.NETHER_STAR).name("<aqua>Server Booster</aqua>").lore("<yellow>Price: 500 points</yellow>").build())
            .onClick('G', click -> {
                double balance = EconomyBridge.balance(click.player());
                if (balance < 100.0) {
                    Text.send(click.player(), "<red>Insufficient points!</red>");
                    return;
                }
                EconomyBridge.withdraw(click.player(), 100.0);
                click.player().getInventory().addItem(new ItemStack(Material.GOLD_INGOT, 16));
                
                int newBalance = (int) EconomyBridge.balance(click.player());
                click.menu().updateState(click.player(), "coins_balance", newBalance);
                Effects.sound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP).volume(1.0F).pitch(1.0F).play(click.player());
            })
            .onClick('S', click -> {
                double balance = EconomyBridge.balance(click.player());
                if (balance < 500.0) {
                    Text.send(click.player(), "<red>Insufficient points!</red>");
                    return;
                }
                EconomyBridge.withdraw(click.player(), 500.0);
                
                int newBalance = (int) EconomyBridge.balance(click.player());
                click.menu().updateState(click.player(), "coins_balance", newBalance);
                Effects.sound(Sound.UI_TOAST_CHALLENGE_COMPLETE).volume(1.0F).pitch(1.0F).play(click.player());
            })
            .build()
            .open(player);
    }
}
```

---

## Real-world Example: Server Selector

Here is a multi-lobby server selector utilizing the `PaginatedMenu` controller to automatically distribute servers across pages:

```java
import dev.oum.oumlib.inventory.ItemBuilder;
import dev.oum.oumlib.inventory.PaginatedMenu;
import dev.oum.oumlib.util.Proxy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public final class LobbySelector {
    public static void open(Player player) {
        List<ItemStack> servers = new ArrayList<>();
        List<String> targetServers = List.of("lobby-1", "lobby-2", "lobby-3", "lobby-4");

        for (String server : targetServers) {
            int online = Proxy.getPlayerCount(server);
            servers.add(ItemBuilder.of(Material.BEACON)
                .name("<green>" + server + "</green>")
                .lore("<gray>Online Players: " + online + "</gray>", "<yellow>Click to connect!</yellow>")
                .build());
        }

        PaginatedMenu menu = PaginatedMenu.builder()
            .title("<dark_gray>Lobby List (<page>/<total>)</dark_gray>")
            .rows(4)
            .contentSlots(10, 11, 12, 13, 14, 15, 16)
            .items(servers)
            .onClick((context, item, index) -> {
                String targetServer = targetServers.get(index);
                player.sendMessage("Connecting to " + targetServer + "...");
                player.closeInventory();
            })
            .build();

        menu.open(player);
    }
}
```

---

## Click Protection Safeguards

To prevent GUI exploits, OumLib implements two safeguards internally:
1. **Auto-Cancellation**: Clicks on items inside the menu container are cancelled (`event.setCancelled(true)`) to prevent players from taking layout items.
2. **Player Inventory Isolation**: Clicks within the player's own inventory hotbar do not trigger GUI slot click handlers, preventing item duplication.
