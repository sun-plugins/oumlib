# Event System

OumLib features a modern, fluent event bus wrapper. It provides cleaner listener registration, custom conditional filtering, execution boundaries, and platform-level lifecycle management.

---

## 1. Registration & Custom Filtering

Use `.filter(...)` to add condition checks to your event listener. The callback handler will only execute if all filter checks return `true`.

```java
import dev.oum.oumlib.event.Events;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

Events.listen(PlayerInteractEvent.class)
    // Filter out off-hand clicks
    .filter(event -> event.getHand() == EquipmentSlot.HAND)
    // Filter to only trigger if the player is holding a diamond sword
    .filter(event -> event.getPlayer().getInventory().getItemInMainHand().getType().name().contains("DIAMOND_SWORD"))
    .handler(event -> {
        event.getPlayer().sendMessage("You swung a diamond sword!");
    });
```

### Player-Specific Filtering (Paper-only)
If you need to listen for events triggered specifically by a particular player (e.g. during a minigame, a chat prompt session, or an active GUI menu context), use `BukkitEvents.listenFor(Player, Class)`:

```java
import dev.oum.oumlib.event.BukkitEvents;
import org.bukkit.event.player.PlayerInteractEvent;

Player targetPlayer = ...;

BukkitEvents.listenFor(targetPlayer, PlayerInteractEvent.class)
    .handler(event -> {
        // This handler will only execute if event.getPlayer() matches the targetPlayer
        event.getPlayer().sendMessage("You interacted!");
    });
```

---

## 3. Cancellable Events & State Handling

You can configure how the event listener behaves regarding cancelled events:
- **`ignoreCancelled()`**: The listener will skip execution if another plugin has already cancelled the event.
- **`onlyIfCancelled()`**: The listener will *only* fire if the event has already been cancelled by another plugin.

```java
import org.bukkit.event.block.BlockBreakEvent;

Events.listen(BlockBreakEvent.class)
    .ignoreCancelled() // Skip if block break is protected/cancelled
    .handler(event -> {
        System.out.println("A block was successfully broken by " + event.getPlayer().getName());
    });
```

---

## 4. Automatic Unregistration & Memory Protection

To avoid memory leaks, you can configure listeners to unregister themselves automatically when certain thresholds or conditions are reached:
- **`maxFires(int count)`**: Automatically cleans up and unregisters the listener after it executes a set number of times.
- **`expireAfter(Duration duration)`**: Automatically unregisters the listener after a set period of time has elapsed since registration.
- **`expireIf(Predicate<E> condition)`**: Evaluates a custom condition on each event execution, unregistering the listener immediately if the predicate returns `true`.

```java
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.time.Duration;

// 1. Fire-once or timed expiration
Events.listen(PlayerQuitEvent.class)
    .maxFires(1) // Runs once, then cleans up. Perfect for single-event wait loops.
    .expireAfter(Duration.ofMinutes(10)) // Automatically unregisters after 10 minutes
    .handler(event -> {
        System.out.println("This message will print for at most one quit event within the next 10 minutes.");
    });

// 2. Conditional expiration (e.g. stop intercepting chat once user types 'exit')
Events.listen(AsyncPlayerChatEvent.class)
    .filter(event -> event.getPlayer().equals(targetPlayer))
    .expireIf(event -> event.getMessage().equalsIgnoreCase("exit"))
    .handler(event -> {
        targetPlayer.sendMessage("Intercepted message: " + event.getMessage());
        event.setCancelled(true);
    });
```

---

## 5. Thread Safety & Folia Compatibility

- **Paper/Spigot**: Event handlers run synchronously on the server's main tick thread (the standard Bukkit behavior), unless the event itself is marked as asynchronous (e.g. `AsyncChatEvent`).
- **Velocity**: Event handlers are run asynchronously on Netty thread pools. Velocity events do not run on a single main thread.
- **Folia**: Folia runs events on the thread executing the region tick where the event occurred. Since region tick threads change dynamically, ensure any external data mutations triggered inside your handlers are thread-safe (e.g. using `ConcurrentHashMap` or thread-safe atomic variables).

### Asynchronous Listener Support
To execute listener code on a background thread pool (or virtual thread on Paper/Folia), chain `.async()` on the builder:
```java
Events.listen(PlayerInteractEvent.class)
    .async() // Offloads execution to virtual/async threads
    .handler(event -> {
        // Safe to execute blocking database lookups or HTTP webhooks
        // Note: Event modification/cancellation is not supported in async mode
    });
```
