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

ItemStack sword = ItemBuilder.of(Material.DIAMOND_SWORD)
    .name("<gold>Fire Sword</gold>")
    .pdc("item-id", "fire_sword") // String key/value
    .pdc("custom-damage", 15)     // Integer key/value
    .pdc("multiplier", 1.5)       // Double key/value
    .build();
```

### Reading PDC Values
Use the static methods in `Pdc` to fetch the metadata back from any `ItemStack`:

```java
import dev.oum.oumlib.util.Pdc;
import org.bukkit.inventory.ItemStack;

ItemStack clickedItem = player.getInventory().getItemInMainHand();

// Read values (returns null if key doesn't exist)
String itemId = Pdc.get(clickedItem, "item-id");
Integer customDamage = Pdc.getInt(clickedItem, "custom-damage");
Double multiplier = Pdc.getDouble(clickedItem, "multiplier");
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

// 3. Deserialization
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

```java
import dev.oum.oumlib.util.BossBars;
import net.kyori.adventure.bossbar.BossBar;
import java.time.Duration;

// Show a temporary BossBar that automatically vanishes after 10 seconds
BossBars.showTemporary(
    player, 
    "<red>Danger Zone</red>", 
    1.0f, // progress (0.0 to 1.0)
    BossBar.Color.RED, 
    BossBar.Overlay.PROGRESS, 
    Duration.ofSeconds(10)
);
```

---

## 6. Server Transfer (Velocity-only)

Send players between proxy servers using the `Proxy` utility:

```java
import dev.oum.oumlib.util.Proxy;
import com.velocitypowered.api.proxy.Player;

Player player = ...;

// Transfer the player to a server named "lobby"
boolean success = Proxy.connect(player, "lobby");
```
