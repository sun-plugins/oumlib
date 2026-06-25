# Entity Utilities & Display Builders

OumLib offers high-level entity utilities and a fluent display entity generator under the `dev.oum.oumlib.entity` package.

---

## Real-world Example: Holographic Stat Billboard

Spawn a billboard-aligned text hologram floating above an NPC's head displaying player rankings:

```java
import dev.oum.oumlib.entity.DisplayBuilder;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;

public final class HologramManager {
    public TextDisplay spawnStatsHologram(Location location) {
        Location spawnLoc = location.clone().add(0, 2.5, 0);
        
        return DisplayBuilder.text(spawnLoc, "<gold>Server Leaderboards</gold>\n<gray>1. sun_mc - 1,200 points</gray>")
            .billboard(Display.Billboard.CENTER)
            .shadow(true)
            .seeThrough(false)
            .backgroundColor(Color.fromARGB(150, 0, 0, 0))
            .scale(1.2F, 1.2F, 1.2F)
            .spawn();
    }
}
```

---

## Real-world Example: Spinning In-Game Shop Showcase

Spawns a glowing, double-sized block display (e.g. Diamond Block) floating above a shop chest, rotated at a 45-degree angle:

```java
import dev.oum.oumlib.entity.DisplayBuilder;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;

public final class ItemShowcaseManager {
    public BlockDisplay spawnChestShowcase(Location chestLoc) {
        Location spawnLoc = chestLoc.clone().add(-0.25, 1.2, -0.25);
        
        return DisplayBuilder.block(spawnLoc, Material.DIAMOND_BLOCK.createBlockData())
            .scale(0.5F, 0.5F, 0.5F)
            .leftRotation(0.0F, 0.382F, 0.0F, 0.924F)
            .glowing(true)
            .glowColor(Color.AQUA)
            .spawn();
    }
}
```

---

## Entities Target Raytracing

Perform entity scans or trace the exact block a player is aiming at:

```java
import dev.oum.oumlib.entity.Entities;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import java.util.List;

public final class WeaponScanner {
    public void execute(Player player) {
        Block targetBlock = Entities.getTargetBlock(player, 15);
        Entity targetEntity = Entities.getTargetEntity(player, 25);
        
        List<Player> nearby = Entities.nearbyPlayers(player.getLocation(), 10.0);
    }
}
```
