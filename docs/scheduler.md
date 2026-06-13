# Scheduler System

OumLib features a platform-agnostic scheduler. It utilizes Java **Virtual Threads** for asynchronous execution and bridges to platform-specific tick loops for synchronous tasks.

---

## 1. Virtual Threads for Async Tasks

All asynchronous tasks registered via `Scheduler.runAsync()` or `Scheduler.runRepeating()` run on **Java Virtual Threads** (`Thread.ofVirtual()`).
- Virtual threads are lightweight, managed by the JVM, and do not block expensive platform platform-worker threads.
- They allow you to perform blocking network operations, database queries, or disk writes without causing server lag or TPS drops.

```java
import dev.oum.oumlib.scheduler.Scheduler;

Scheduler.runAsync(() -> {
    // Perform database operations here
    // Running this in a blocking way is safe on virtual threads!
    db.saveUserData();
});
```

---

## 2. Sync Tick Scheduling on Paper

For operations that must interact with Bukkit/Paper API directly (like spawning entities or modifying blocks), you must run on the server's main thread:

- **`Scheduler.run(Runnable)`**: Schedules a task to execute on the next tick of the server's main thread.
- **`Scheduler.runLater(Duration, Runnable)`**: Schedules a task to execute after a specified delay on the server's main thread.
- **`Scheduler.runLater(long ticks, Runnable)`**: Schedules a task to execute after a specified amount of server ticks.

> [!WARNING]
> The method `Scheduler.runDelayed(...)` is deprecated in `v1.0.1` and scheduled for removal in `v1.0.9`. Developers must migrate to `Scheduler.runLater(...)`.

```java
import dev.oum.oumlib.scheduler.Scheduler;
import org.bukkit.Bukkit;
import java.time.Duration;
import net.kyori.adventure.text.minimessage.MiniMessage;

// Run tasks later using durations
Scheduler.runLater(Duration.ofSeconds(2), () -> {
    Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<green>2 seconds elapsed!</green>"));
});

// Or using ticks (e.g. 20 ticks = 1 second)
Scheduler.runLater(20L, () -> {
    Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<green>1 second elapsed!</green>"));
});
```

---

## 3. Folia Compatibility

On **Folia**, tasks scheduled asynchronously (`runAsync`) are fully supported and execute on virtual threads.
- Since Folia uses multi-threaded regional tick loops instead of a single main thread, OumLib executes synchronous tasks (`runSync`) on Folia's global thread pool.
- For tick-precise regional operations (like block changes at a specific location or modifications to entities), you should use OumLib's region-aware scheduling methods, which transparently fall back to standard synchronous main thread execution on non-Folia platforms:
  - `Scheduler.runAt(Location, Runnable)`: Schedules a task on the region corresponding to the location.
  - `Scheduler.runFor(Entity, Runnable)`: Schedules a task on the region corresponding to the entity.

These methods are also supported inside `TaskGroup`s for automatic lifecycle cleanup.

---

## 4. TaskGroups and Lifecycle Management

To avoid leaking background tasks when your plugin reloads or disables, use a `TaskGroup` to register and manage tasks.

```java
import dev.oum.oumlib.scheduler.TaskGroup;
import java.time.Duration;

public class Announcer {

    private final TaskGroup taskGroup = new TaskGroup();

    public void startAnnouncements() {
        // Automatically registers the repeating task to this group
        taskGroup.runRepeating(Duration.ZERO, Duration.ofSeconds(30), () -> {
            System.out.println("Announcing server rules...");
        });
    }

    public void stop() {
        // Instantly cancels all active repeating or delayed tasks in this group
        taskGroup.cancelAll();
    }
}
```
When your plugin disables, calling `taskGroup.cancelAll()` ensures all repeating loops stop immediately.

---

## 5. Promises and Callback Chaining

OumLib provides a `Promise` wrapper around Java's `CompletableFuture` that simplifies asynchronous operations with callbacks targeted automatically at the correct server tick thread.

This is extremely useful for retrieving data (e.g., database or web queries) asynchronously, and then updating the game state or sending messages on the synchronous main thread safely:

```java
import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.oumlib.scheduler.Promise;

// 1. Fetch values asynchronously
Scheduler.supplyAsync(() -> database.loadUserCoins(uuid))
    // 2. Perform callback on the sync main tick thread safely
    .thenAcceptSync(coins -> {
        player.sendMessage("Loaded " + coins + " coins from database!");
    });
```

### Virtual Threads (Java 21+)

For heavy blocking I/O operations (like HTTP requests, Web APIs, or database queries), you can leverage Java 21 Virtual Threads using the virtual scheduler methods to keep thread overhead at a minimum:

```java
// 1. Fetch values asynchronously on a JVM Virtual Thread
Scheduler.supplyVirtual(() -> database.loadUserCoins(uuid))
    // 2. Perform callback on the sync main tick thread safely
    .thenAcceptSync(coins -> {
        player.sendMessage("Loaded " + coins + " coins via Virtual Threads!");
    });
```

---

## 6. TaskChains (Sequential execution control)

`TaskChain` allows developers to build a sequence of tasks that execute step-by-step, shifting back and forth between synchronous and asynchronous threads with custom delays.

```java
import dev.oum.oumlib.scheduler.TaskChain;
import java.time.Duration;

TaskChain.create("Initial Value")
    // Step 1: Run asynchronously (e.g., fetch from DB or Web API)
    .async(value -> {
        return value + " -> Async Step";
    })
    // Step 2: Delay for 2 seconds
    .delay(Duration.ofSeconds(2))
    // Step 3: Run on the main sync server thread
    .sync(value -> {
        player.sendMessage("Current chain status: " + value);
        return value + " -> Sync Step";
    })
    // Step 4: Delay for another 10 server ticks
    .delay(10)
    // Step 5: Finish asynchronously
    .async(value -> {
        System.out.println("Chain finished: " + value);
    })
    .execute();
```
