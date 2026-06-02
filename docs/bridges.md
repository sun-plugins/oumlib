# Server and Integration Bridges

OumLib features high-performance, classloading-safe cross-plugin integration bridges, allowing your plugins to interact with multiple economies and custom item providers without compile-time dependencies.

---

## 1. Economy Bridge

The `EconomyBridge` provides simultaneous support for multiple currency/economy plugins (e.g. Vault and PlayerPoints). It handles background registration and safe fallback detection automatically.

### Checking or Modifying Player Balances

You can interact with the player's balance using the default provider, or query a specific provider:

```java
import dev.oum.oumlib.bridge.economy.EconomyBridge;
import org.bukkit.entity.Player;

Player player = ...;

// Query the player's balance using the default provider (e.g. Vault)
double vaultBalance = EconomyBridge.balance(player);

// Query using a specific provider (e.g. PlayerPoints)
double pointsBalance = EconomyBridge.balance("playerpoints", player);

// Checking if a player has enough currency
if (EconomyBridge.has(player, 100.0)) {
    // Withdraw currency
    boolean success = EconomyBridge.withdraw(player, 100.0);
}

// Depositing currency
EconomyBridge.deposit("playerpoints", player, 50.0);
```

### Registering Custom Economy Providers

You can register custom economy providers (e.g. for custom gems or tokens) by implementing `EconomyProvider`:

```java
import dev.oum.oumlib.bridge.economy.EconomyProvider;
import dev.oum.oumlib.bridge.economy.EconomyBridge;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;

public class GemEconomyProvider implements EconomyProvider {
    @Override
    public @NonNull String name() { return "gems"; }

    @Override
    public boolean has(@NonNull OfflinePlayer player, double amount) {
        return getGems(player) >= amount;
    }

    @Override
    public boolean withdraw(@NonNull OfflinePlayer player, double amount) {
        return takeGems(player, (int) amount);
    }

    @Override
    public boolean deposit(@NonNull OfflinePlayer player, double amount) {
        return giveGems(player, (int) amount);
    }

    @Override
    public double balance(@NonNull OfflinePlayer player) {
        return getGems(player);
    }
}

// Register it during plugin initialization
EconomyBridge.registerProvider(new GemEconomyProvider());
```

---

## 2. Item Bridge

The `ItemBridge` allows you to resolve `ItemStack` objects from various plugins (Minecraft, ItemsAdder, Oraxen, MMOItems, and MythicMobs) using a single, unified string configuration format (namespace strings).

### Resolving Custom Items

To fetch an item stack from any source, use `ItemBridge.getItem(String identifier)`:

```java
import dev.oum.oumlib.bridge.item.ItemBridge;
import org.bukkit.inventory.ItemStack;
import java.util.Optional;

// Resolve standard Minecraft items
Optional<ItemStack> diamond = ItemBridge.getItem("minecraft:diamond"); // or just "diamond"

// Resolve ItemsAdder items
Optional<ItemStack> customBlock = ItemBridge.getItem("itemsadder:ruby_ore");

// Resolve Oraxen items
Optional<ItemStack> customSword = ItemBridge.getItem("oraxen:mythic_sword");

// Resolve MMOItems (format: "mmoitems:TYPE:ID")
Optional<ItemStack> excalibur = ItemBridge.getItem("mmoitems:SWORD:EXCALIBUR");

// Resolve MythicMobs custom items
Optional<ItemStack> key = ItemBridge.getItem("mythicmobs:skeleton_key");

// Resolve Nexo custom items
Optional<ItemStack> nexoSword = ItemBridge.getItem("nexo:emerald_sword");
```

### Registering Custom Item Providers

You can register custom item systems to the bridge by implementing `ItemProvider`:

```java
import dev.oum.oumlib.bridge.item.ItemProvider;
import dev.oum.oumlib.bridge.item.ItemBridge;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import java.util.Optional;

public class CustomItemProvider implements ItemProvider {
    @Override
    public @NonNull String name() { return "myplugin"; }

    @Override
    public @NonNull Optional<ItemStack> getItem(@NonNull String id) {
        ItemStack item = MyPluginAPI.getItem(id);
        return Optional.ofNullable(item);
    }
}

// Register during initialization
ItemBridge.registerProvider(new CustomItemProvider());
```

---

## 3. Permissions Bridge

The `PermissionBridge` provides unified, cross-platform (Paper and Velocity) access to **LuckPerms** user metadata (prefixes, suffixes, groups, and custom meta).

### Querying Player Groups and Meta

Since the permissions bridge operates entirely using player `UUID`s, you can use the exact same methods on both platforms:

```java
import dev.oum.oumlib.bridge.permission.PermissionBridge;
import java.util.UUID;

UUID playerUuid = ...;

if (PermissionBridge.isAvailable()) {
    // Get primary group
    String primaryGroup = PermissionBridge.getPrimaryGroup(playerUuid); // e.g. "admin"

    // Get prefix/suffix
    String prefix = PermissionBridge.getPrefix(playerUuid); // e.g. "[Admin] "
    String suffix = PermissionBridge.getSuffix(playerUuid);

    // Get custom metadata value
    String coinsMultiplier = PermissionBridge.getMetaValue(playerUuid, "multiplier"); // e.g. "1.5"
}
```
