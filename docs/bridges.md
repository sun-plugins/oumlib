# Integration & Plugin Bridges

OumLib features classloading-safe cross-plugin integration bridges, allowing your plugins to interact with multiple economies, custom item systems, and permission managers without compile-time dependencies.

---

## Real-world Example: VIP Rank Purchase

Here is a store manager that checks if a player has a primary LuckPerms group matching VIP, confirms their Vault economy points balance can cover the purchase, takes the coins, and adds the VIP group to the player:

```java
import dev.oum.oumlib.bridge.economy.EconomyBridge;
import dev.oum.oumlib.bridge.permission.PermissionBridge;
import dev.oum.oumlib.text.Text;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

public final class RankPurchaseManager {
    public void purchaseVipRank(Player player) {
        if (!PermissionBridge.isAvailable()) {
            Text.send(player, "<red>Permissions system is currently offline.</red>");
            return;
        }

        String primaryGroup = PermissionBridge.getPrimaryGroup(player.getUniqueId());
        if (primaryGroup.equalsIgnoreCase("vip") || primaryGroup.equalsIgnoreCase("admin")) {
            Text.send(player, "<red>You already own the VIP rank!</red>");
            return;
        }

        double price = 5000.0;
        double balance = EconomyBridge.balance(player);

        if (balance < price) {
            Text.send(player, "<red>You need " + (price - balance) + " more coins to purchase VIP!</red>");
            return;
        }

        boolean success = EconomyBridge.withdraw(player, price);
        if (success) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " parent set vip");
            Text.send(player, "<green>Congratulations! You are now a VIP rank member.</green>");
        } else {
            Text.send(player, "<red>Transaction declined by payment provider.</red>");
        }
    }
}
```

---

## Custom Item Bridging

Resolve `ItemStack` instances from Minecraft, ItemsAdder, Oraxen, MMOItems, MythicMobs, and Nexo dynamically:

```java
import dev.oum.oumlib.bridge.item.ItemBridge;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Optional;

public final class CustomItemLoader {
    public void giveCustomItems(Player player) {
        Optional<ItemStack> nexoSword = ItemBridge.getItem("nexo:emerald_sword");
        Optional<ItemStack> mythicKey = ItemBridge.getItem("mythicmobs:skeleton_key");
        Optional<ItemStack> standardDiamond = ItemBridge.getItem("minecraft:diamond");

        nexoSword.ifPresent(item -> player.getInventory().addItem(item));
        mythicKey.ifPresent(item -> player.getInventory().addItem(item));
        standardDiamond.ifPresent(item -> player.getInventory().addItem(item));
    }
}
```

---

## Registering Custom Economy Providers

Register custom economy tokens or custom coin providers to the global bridge:

```java
import dev.oum.oumlib.bridge.economy.EconomyProvider;
import dev.oum.oumlib.bridge.economy.EconomyBridge;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;

public class CustomTokenProvider implements EconomyProvider {
    @Override
    public @NonNull String name() { 
        return "customtokens"; 
    }

    @Override
    public boolean has(@NonNull OfflinePlayer player, double amount) {
        return getTokens(player) >= amount;
    }

    @Override
    public boolean withdraw(@NonNull OfflinePlayer player, double amount) {
        return modifyTokens(player, -(int) amount);
    }

    @Override
    public boolean deposit(@NonNull OfflinePlayer player, double amount) {
        return modifyTokens(player, (int) amount);
    }

    @Override
    public double balance(@NonNull OfflinePlayer player) {
        return getTokens(player);
    }

    private int getTokens(OfflinePlayer player) {
        return 1000;
    }

    private boolean modifyTokens(OfflinePlayer player, int amount) {
        return true;
    }
}

public class TokenInitializer {
    public void register() {
        EconomyBridge.registerProvider(new CustomTokenProvider());
    }
}
```
