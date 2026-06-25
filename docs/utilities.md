# General Utilities

OumLib contains helpers to manage item serialization, Persistent Data Containers, countdowns, cooldowns, and server proxy routing.

---

## Real-world Example: PDC Inventory Backpack

Here is a system that serializes player backpack inventories into base64 and stores them directly inside the player's Persistent Data Container (PDC) using OumLib's helper classes, persisting them across relogs:

```java
import dev.oum.oumlib.util.Pdc;
import dev.oum.oumlib.util.ItemSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PlayerBackpackSaver {
    public void saveBackpack(Player player, ItemStack[] contents) {
        String base64 = ItemSerializer.serializeArray(contents);
        Pdc.of(player)
            .namespaced("myplugin")
            .set("backpack_contents", base64);
        
        player.sendMessage("Backpack saved successfully!");
    }

    public ItemStack[] loadBackpack(Player player) {
        String base64 = Pdc.of(player)
            .namespaced("myplugin")
            .get("backpack_contents");

        if (base64 == null || base64.isEmpty()) {
            return new ItemStack[0];
        }

        return ItemSerializer.deserializeArray(base64);
    }
}
```

---

## Real-world Example: Match Lobby Countdown

Play tick sounds and announce remaining seconds on the screen during a game start countdown, executing startup actions once finished:

```java
import dev.oum.oumlib.util.Countdown;
import dev.oum.oumlib.effect.Sounds;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

public final class LobbyCountdownManager {
    public void startLobbyCountdown(Player player) {
        Countdown.builder(player, 10)
            .displayMode(Countdown.Display.TITLE)
            .format("<gold>Match starting in %duration%</gold>")
            .tickSound(Sounds.TICK)
            .onComplete(audience -> {
                if (audience instanceof Player p) {
                    p.sendMessage("Game started!");
                    Sounds.play(p, "entity.generic.explode", 1.0f, 1.0f);
                }
            })
            .start();
    }
}
```

---

## Standalone Cooldown System

A standalone map-based cooldown timer:

```java
import dev.oum.oumlib.util.Cooldown;
import java.time.Duration;
import java.util.UUID;

public final class SkillManager {
    private final Cooldown fireboltCooldown = Cooldown.of(Duration.ofSeconds(8));

    public boolean useFirebolt(UUID playerUuid) {
        if (fireboltCooldown.isOnCooldown(playerUuid)) {
            return false;
        }

        fireboltCooldown.set(playerUuid);
        return true;
    }
}
```

---

## Duration & Digital Clock Formats

Format times into readable clocks or text spans:

```java
import dev.oum.oumlib.util.Format;
import java.time.Duration;

public final class FormatPrinter {
    public void printStats() {
        String timeRemaining = Format.duration(Duration.ofSeconds(95));
        String clockFace = Format.digitalTime(Duration.ofSeconds(3665));
        String scoreCommas = Format.number(5000000);
    }
}
```
