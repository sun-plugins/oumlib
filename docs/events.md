# Events & Listeners Bus

OumLib features a modern, fluent event bus wrapper. It provides cleaner listener registration, conditional filtering, execution boundaries, and unregistration hooks.

---

## Real-world Example: Combat Tagging System

Here is a combat tagging module that flags players in combat upon entity damage, blocks teleportation requests, intercepts quit actions, and automatically expires when players log off or combat times out:

```java
import dev.oum.oumlib.event.Events;
import dev.oum.oumlib.text.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public final class CombatTagManager {
    private final Set<Player> activeCombat = new HashSet<>();

    public void initialize() {
        Events.listen(EntityDamageByEntityEvent.class)
            .filter(event -> event.getEntity() instanceof Player)
            .filter(event -> event.getDamager() instanceof Player)
            .handler(event -> {
                Player victim = (Player) event.getEntity();
                Player attacker = (Player) event.getDamager();
                
                tagPlayer(victim);
                tagPlayer(attacker);
            });

        Events.listen(PlayerTeleportEvent.class)
            .filter(event -> activeCombat.contains(event.getPlayer()))
            .filter(event -> event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND)
            .handler(event -> {
                event.setCancelled(true);
                Text.send(event.getPlayer(), "<red>You cannot teleport while in combat!</red>");
            });
    }

    private void tagPlayer(Player player) {
        if (activeCombat.add(player)) {
            Text.send(player, "<red>You are now in combat! Do not log out.</red>");
            
            Events.listen(PlayerQuitEvent.class)
                .playerFilter(PlayerQuitEvent::getPlayer, p -> p.equals(player))
                .maxFires(1)
                .expireAfter(Duration.ofSeconds(15))
                .handler(event -> {
                    System.out.println(player.getName() + " logged out while in combat!");
                    activeCombat.remove(player);
                });

            Events.listen(PlayerQuitEvent.class)
                .playerFilter(PlayerQuitEvent::getPlayer, p -> p.equals(player))
                .expireAfter(Duration.ofSeconds(15))
                .expireIf(event -> !activeCombat.contains(player))
                .handler(event -> activeCombat.remove(player));
        }
    }
}
```

---

## Cancellable Events & State Handling

Configure how the event listener behaves regarding cancelled events:
- **`ignoreCancelled()`**: Skip execution if another plugin has already cancelled the event.
- **`onlyIfCancelled()`**: Only fire if the event has already been cancelled.

```java
import dev.oum.oumlib.event.Events;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockLogger {
    public void register() {
        Events.listen(BlockBreakEvent.class)
            .ignoreCancelled()
            .handler(event -> {
                System.out.println("Block broken: " + event.getBlock().getType());
            });
    }
}
```

---

## Asynchronous Thread Listeners

Offload heavy I/O calculations (like database calls) to async thread pools:

```java
import dev.oum.oumlib.event.Events;
import org.bukkit.event.player.PlayerInteractEvent;

public class AsyncLogger {
    public void register() {
        Events.listen(PlayerInteractEvent.class)
            .async()
            .handler(event -> {
                System.out.println("Processing heavy interaction logs on virtual threads.");
            });
    }
}
```
*Note: Event modification/cancellation is not supported in async mode.*
