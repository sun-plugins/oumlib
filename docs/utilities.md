# General Utilities

OumLib includes several practical helper classes to handle metadata, formatting, and location serialization without writing verbose boilerplate.

---

## 1. Persistent Data Container (PDC) Helpers

Attaching custom metadata to items, entities, chunks, block states, and more is a core requirement for plugins. OumLib provides a modern, unified, and type-safe PDC API that operates on both `ItemStack` and any `PersistentDataHolder`.

### Accessing the PDC Wrapper
Use `Pdc.of(item)` for items, or `Pdc.of(holder)` for entities, chunks, block states, etc.

```java
import dev.oum.oumlib.util.Pdc;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import java.util.List;

ItemStack sword = ...;

// Write values fluently
Pdc.of(sword)
    .set("item-id", "fire_sword")
    .set("custom-damage", 15)
    .set("multiplier", 1.5)
    .set("unlocked", true)
    .set("tags", List.of("legendary", "fire"));

// Read values (returns null/defaultValue if the key doesn't exist)
String itemId = Pdc.of(sword).get("item-id");
int customDamage = Pdc.of(sword).getOrDefault("custom-damage", 0);
double multiplier = Pdc.of(sword).getOrDefault("multiplier", 1.0);
boolean unlocked = Pdc.of(sword).getOrDefault("unlocked", false);
List<String> tags = Pdc.of(sword).getList("tags");
```

### Namespaced PDC Contexts
You can scope keys under a specific namespace (e.g. your plugin name) to avoid conflicts:
```java
// Writes to namespaced keys: "myplugin:cooldown" and "myplugin:uses"
Pdc.of(sword).namespaced("myplugin")
    .set("cooldown", 10)
    .set("uses", 5);

int cooldown = Pdc.of(sword).namespaced("myplugin").getOrDefault("cooldown", 0);
```

### PDC Change Listeners
You can register global listeners to execute logic whenever specific metadata changes on a player, block, or item:
```java
import org.bukkit.NamespacedKey;

NamespacedKey key = new NamespacedKey("myplugin", "coins");

Pdc.registerListener(key, (holder, k, oldValue, newValue) -> {
    if (holder instanceof Player player) {
        player.sendMessage("Your coins updated from " + oldValue + " to " + newValue + "!");
    }
});
```

> [!WARNING]
> Legacy static methods like `Pdc.get(item, key)` or `Pdc.getInt(...)` are deprecated and scheduled for removal in `v1.0.5`. Please migrate to the fluent `Pdc.of(...)` API.

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
> The old utility class `dev.oum.oumlib.util.BossBars` is deprecated since `v1.0.1` and marked for removal in `v1.0.9`. Please use `Text.bossBar` or `Text.bossBarTemporary` instead.

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

### Dynamic Server Registration & Unregistration
Register or unregister backend Minecraft servers dynamically at runtime without restarting the proxy:
```java
// Register a new game server instance
Proxy.registerServer("games-3", "192.168.1.15", 25568);

// Unregister a server when it shuts down
Proxy.unregisterServer("games-3");
```

### Async Online Status Check (Ping)
Check if a backend server is online and accepting connections asynchronously:
```java
Proxy.isOnline("lobby-1").thenAccept(online -> {
    if (online) {
        player.sendMessage("Lobby-1 is online!");
    }
});
```

### Targeted Server Broadcasts
Broadcast MiniMessage-formatted messages and titles directly to all players connected to a specific server:
```java
// Broadcast chat message
Proxy.broadcastTo("games-1", "<green>A new match is starting in 10 seconds!</green>");

// Broadcast title
Proxy.sendTitleTo("games-1", "<red>Match Started</red>", "<gray>Good luck!</gray>");
```

### Smart Load Balancer
Find the best server (the one online with the lowest player count) from a list of options:
```java
Proxy.getBestServer(List.of("lobby-1", "lobby-2", "lobby-3"))
    .thenAccept(optServer -> {
        optServer.ifPresent(server -> {
            player.sendMessage("Connecting to best server: " + server.getServerInfo().getName());
            player.createConnectionRequest(server).fireAndForget();
        });
    });
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

> [!WARNING]
> The `dev.oum.oumlib.util.PlayerData` helper wrapper is deprecated in `v1.0.3` and scheduled for removal in `v1.0.9`. Developers must migrate to the modern and unified `Pdc.of(player)` API.

### Migration Example:
```java
import dev.oum.oumlib.util.Pdc;

// Set player-bound persistent values using Pdc.of
Pdc.of(player)
    .set("rank", "MVP")
    .set("coins", 500)
    .set("claimed-reward", true);

// Get values
String rank = Pdc.of(player).getOrDefault("rank", "Default");
int coins = Pdc.of(player).getOrDefault("coins", 0);
boolean claimed = Pdc.of(player).getOrDefault("claimed-reward", false);
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

---

## 10. Base64 Item Serializer

OumLib features `ItemSerializer` to serialize and deserialize single `ItemStack` instances or `ItemStack[]` arrays to safe Base64 strings. It automatically uses component-aware modern Paper API serialization if running on Paper 1.20.6+, and falls back to standard object streams on legacy platforms.

### Serializing a single item:
```java
import dev.oum.oumlib.util.ItemSerializer;
import org.bukkit.inventory.ItemStack;

ItemStack sword = ...;
String base64 = ItemSerializer.serialize(sword);

// Deserializing back:
ItemStack restored = ItemSerializer.deserialize(base64);
```

### Serializing an array of items (e.g. inventories):
```java
ItemStack[] inventory = player.getInventory().getContents();
String base64 = ItemSerializer.serializeArray(inventory);

// Deserializing back:
ItemStack[] restoredInventory = ItemSerializer.deserializeArray(base64);
```

### Fluent Persistent Data Container (PDC) Integration:
You can read and write `ItemStack` and `ItemStack[]` values directly via `Pdc` helper objects without manual serialization:
```java
import dev.oum.oumlib.util.Pdc;

// Store an item directly in the player's PDC
Pdc.of(player).setItem("saved-sword", sword);

// Retrieve the item later
ItemStack restored = Pdc.of(player).getItem("saved-sword");

// Store or retrieve an entire item array (e.g. vaults/inventories)
Pdc.of(player).setItemArray("backpack", inventory);
ItemStack[] restoredBackpack = Pdc.of(player).getItemArray("backpack");
```

---

## 11. Fluent Effects & Particle Player

OumLib features a builder-based effects system (`Effects`) to spawn particles and play sounds with volume, pitch, velocity, colors, and specific target groups fluently.

### Spawning Particles:
```java
import dev.oum.oumlib.effect.Effects;
import org.bukkit.Particle;
import org.bukkit.Color;

// Spawn standard particle at a location
Effects.particle(Particle.HAPPY_VILLAGER)
    .count(15)
    .speed(0.1)
    .offset(0.5, 0.5, 0.5)
    .spawn(location);

// Spawn a colorized dust particle
Effects.particle(Particle.DUST)
    .color(Color.RED, 1.2f)
    .spawn(location);

// Spawn particles only visible to a specific player
Effects.particle(Particle.FLAME)
    .spawn(player, location);
```

### Playing Sounds:
```java
import dev.oum.oumlib.effect.Effects;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

// Play a sound with random pitch variance (e.g. 1.0 +/- 0.2)
Effects.sound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP)
    .category(SoundCategory.PLAYERS)
    .volume(0.8f)
    .pitch(1.0f)
    .pitchVariance(0.2f)
    .play(player);
```

