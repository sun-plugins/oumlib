# General Utilities

OumLib includes several practical helper classes to handle metadata, formatting, and location serialization without writing verbose boilerplate.

---

## 1. Persistent Data Container (PDC) Helpers

Attaching custom metadata to items is a key requirement for modern Minecraft plugins. OumLib makes writing and reading from PDC values simple.

### Writing PDC Values
Use the chainable `.pdc(...)` methods on `ItemBuilder`:

```java
import dev.oum.oumlib.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.List;

ItemStack sword = ItemBuilder.of(Material.DIAMOND_SWORD)
    .name("<gold>Fire Sword</gold>")
    .pdc("item-id", "fire_sword") // String key/value
    .pdc("custom-damage", 15)     // Integer key/value
    .pdc("multiplier", 1.5)       // Double key/value
    .pdc("unlocked", true)        // Boolean key/value
    .pdc("created-at", 1717300000L) // Long key/value
    .pdc("tags", List.of("legendary", "fire")) // List<String> key/value
    .build();
```

### Reading PDC Values
Use the static methods in `Pdc` to fetch the metadata back from any `ItemStack`:

```java
import dev.oum.oumlib.util.Pdc;
import org.bukkit.inventory.ItemStack;
import java.util.List;

ItemStack clickedItem = player.getInventory().getItemInMainHand();

// Read values (returns null if key doesn't exist)
String itemId = Pdc.get(clickedItem, "item-id");
Integer customDamage = Pdc.getInt(clickedItem, "custom-damage");
Double multiplier = Pdc.getDouble(clickedItem, "multiplier");
Boolean unlocked = Pdc.getBoolean(clickedItem, "unlocked");
Long createdAt = Pdc.getLong(clickedItem, "created-at");
List<String> tags = Pdc.getList(clickedItem, "tags");
```

---

## 2. Formatting (Time & Numbers)

Use the `Format` class to display clean durations, digital timers, and compact numbers to players:

```java
import dev.oum.oumlib.util.Format;
import java.time.Duration;

// 1. Durations (e.g., for cooldowns or match timers)
Format.duration(Duration.ofSeconds(125)); // "2m 5s"
Format.duration(Duration.ofSeconds(45));  // "45s"

// 2. Digital Clocks (e.g., "02:05", "01:10:05")
Format.digitalTime(Duration.ofSeconds(125));  // "02:05"
Format.digitalTime(Duration.ofSeconds(3665)); // "01:01:05"

// 3. Number Formatting with commas
Format.number(1250000); // "1,250,000"

// 4. Compact Numbers (e.g., ELO stats or money balances)
Format.compactNumber(1500);    // "1.5k"
Format.compactNumber(2500000); // "2.5M"
```

---

## 3. Location Serializers

Saving locations to configs or databases is simplified using the `Locations` utility to convert Bukkit `Location` objects to and from database-friendly strings.

```java
import dev.oum.oumlib.util.Locations;
import org.bukkit.Location;

Location loc = player.getLocation();

// 1. Full serialization (world,x,y,z,yaw,pitch)
String serialized = Locations.serialize(loc); 
// Output example: "world,120.5,64.0,-250.3,90.0,0.0"

// 2. Block-only serialization (world,x,y,z)
String blockSerialized = Locations.serializeBlock(loc); 
// Output example: "world,120,64,-251"

// 3. Folia-compatible Region serialization (world,x,y,z,yaw,pitch,chunkX,chunkZ)
String regionSerialized = Locations.serializeRegion(loc);
// Output example: "world,120.5,64.0,-250.3,90.0,0.0,7,-16"

// 4. Deserialization (handles both standard and region serialization formats)
Location parsedLoc = Locations.deserialize(serialized);
```

---

## 4. Player Targeting & Raytracing (Paper-only)

Determining what block or entity a player is looking at is crucial for spells, guns, and interactive builds. The `Players` utility exposes clean wrappers using Bukkit's raytracing system:

```java
import dev.oum.oumlib.util.Players;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

// 1. Get exact targeted block within 30 blocks
Block block = Players.getTargetBlock(player, 30);

// 2. Get targeted entity within 50 blocks (automatically ignores the self player)
Entity entity = Players.getTargetEntity(player, 50);
```

---

## 5. Temporary BossBars

Kyori's native BossBar API is powerful but requires manual scheduler tracking to clean up. OumLib makes creating self-expiring bossbars easy on both Paper and Velocity:

> [!NOTE]
> The old utility class `dev.oum.oumlib.util.BossBars` is deprecated since `v1.0.1` and marked for removal in `v1.0.3`. Please use `Text.bossBar` or `Text.bossBarTemporary` instead.

```java
import dev.oum.oumlib.text.Text;
import net.kyori.adventure.bossbar.BossBar;
import java.time.Duration;

// Show a temporary BossBar that automatically vanishes after 10 seconds
Text.bossBarTemporary(
    player, 
    "<red>Danger Zone</red>", 
    1.0f, // progress (0.0 to 1.0)
    BossBar.Color.RED, 
    BossBar.Overlay.PROGRESS, 
    Duration.ofSeconds(10)
);
```

---

## 6. Proxy & Routing Utilities (Velocity-only)

OumLib includes built-in proxy utilities inside the `Proxy` class for handling server transfers, server fallback routing, player counts, and plugin messaging on Velocity.

### Server Connection / Player Transfer
Transfer players to registered backend servers:
```java
import dev.oum.oumlib.util.Proxy;
import com.velocitypowered.api.proxy.Player;

Proxy.connect(player, "lobby"); // returns true if connection initiated
```

### Auto-Fallback Routing
When players are kicked or disconnected from a backend server (e.g., during a server crash or restart), automatically redirect them to fallback/lobby servers instead of kicking them from the proxy entirely:
```java
import dev.oum.oumlib.util.Proxy;
import java.util.List;

// Run this during Proxy initialization
Proxy.registerFallbackRouter(this, List.of("lobby-1", "lobby-2", "hub"));
```

### Server Player Counts
Retrieve player counts for a specific server or across a cluster of servers:
```java
int lobbyCount = Proxy.getPlayerCount("lobby-1");
int totalHubPlayers = Proxy.getPlayerCount(List.of("lobby-1", "lobby-2", "hub"));
```

### Cross-Server Plugin Messaging
Send plugin message payloads to the player's active backend server connection without writing verbose registration boilerplate:
```java
byte[] messagePayload = ...;
Proxy.sendPluginMessage(player, "myplugin:sync", messagePayload);
```

---

## 7. Standalone Cooldowns

A standalone cooldown system mapping `UUID`s to durations, completely independent of command contexts.

```java
import dev.oum.oumlib.util.Cooldown;
import java.time.Duration;

// Create a cooldown that lasts for 5 seconds
Cooldown speedCooldown = Cooldown.of(Duration.ofSeconds(5));

if (speedCooldown.isOnCooldown(player.getUniqueId())) {
    player.sendMessage("Remaining time: " + speedCooldown.remainingSeconds(player.getUniqueId()) + "s");
} else {
    // Activate speed boost...
    speedCooldown.set(player.getUniqueId());
}
```

---

## 8. PlayerData Persistence (Paper-only)

A simple helper wrapper for player PersistentDataContainers, making read/write operations for player-bound data easy and clean.

```java
import dev.oum.oumlib.util.PlayerData;

PlayerData data = PlayerData.of(player);

// Set player-bound persistent values
data.set("rank", "MVP");
data.setInt("coins", 500);
data.setBoolean("claimed-reward", true);

// Get values
String rank = data.getOrDefault("rank", "Default");
int coins = data.getIntOrDefault("coins", 0);
boolean claimed = data.getBooleanOrDefault("claimed-reward", false);
```

---

## 9. Permission Builder

A cross-platform permission checker and builder that automatically registers permissions on Paper/Bukkit with default permission states.

```java
import dev.oum.oumlib.util.Permission;

Permission adminPerm = Permission.builder("myplugin.admin")
    .description("Allows admin commands access.")
    .defaultValue(Permission.Default.OP) // Auto-registers with OP default on Paper
    .build();

if (adminPerm.has(sender)) {
    // Perform admin actions...
}
```
