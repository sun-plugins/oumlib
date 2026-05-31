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

- **`Scheduler.runSync(Runnable)`**: Schedules a task to execute on the next tick of the server's main thread.
- **`Scheduler.runSyncDelayed(Duration, Runnable)`**: Schedules a task to execute after a specified delay on the server's main thread.

```java
import dev.oum.oumlib.scheduler.Scheduler;
import org.bukkit.Bukkit;
import java.time.Duration;

Scheduler.runSyncDelayed(Duration.ofSeconds(2), () -> {
    // Bukkit API calls must be run on the sync thread
    Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<green>2 seconds elapsed!</green>"));
});
```

---

## 3. Folia Compatibility

On **Folia**, tasks scheduled asynchronously (`runAsync`) are fully supported and execute on virtual threads.
- Since Folia uses multi-threaded regional tick loops instead of a single main thread, OumLib executes synchronous tasks (`runSync`) on Folia's global thread pool.
- For tick-precise regional operations (like block changes at a specific location), you should use Folia's region scheduler API directly, as OumLib's sync tasks are executed globally.

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
