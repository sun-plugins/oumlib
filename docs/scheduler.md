# Scheduler System

OumLib features a platform-agnostic scheduler. It utilizes Java virtual threads for asynchronous execution and bridges to platform-specific tick loops for synchronous tasks.

---

## Real-world Example: Profile Auto-Save Manager

Here is a background manager that schedules a repeating database save task running on virtual threads, ensuring that blocking database calls never interrupt server tick performance, and gracefully cleans up resources when the plugin disables:

```java
import dev.oum.oumlib.database.Database;
import dev.oum.oumlib.scheduler.TaskGroup;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class ProfileAutoSaver {
    private final TaskGroup taskGroup = new TaskGroup();
    private final Database db;

    public ProfileAutoSaver(Database db) {
        this.db = db;
    }

    public void startSaveCycle() {
        taskGroup.runRepeating(Duration.ofSeconds(60), Duration.ofSeconds(60), () -> {
            List<Object[]> batchParams = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                int score = player.getLevel();
                batchParams.add(new Object[] { player.getUniqueId().toString(), score, score });
            }

            if (!batchParams.isEmpty()) {
                db.executeBatch("INSERT INTO profiles (uuid, level) VALUES (?, ?) ON DUPLICATE KEY UPDATE level = ?", batchParams);
            }
        });
    }

    public void stop() {
        taskGroup.cancelAll();
    }
}
```

---

## Real-world Example: Combat Tag Transition (TaskChains)

Teleport a player back to spawn after checking their combat status on the database and showing a visual countdown on the screen:

```java
import dev.oum.oumlib.database.Database;
import dev.oum.oumlib.scheduler.TaskChain;
import dev.oum.oumlib.text.Text;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.time.Duration;

public final class SpawnTeleportHandler {
    private final Database db;
    private final Location spawnLocation;

    public SpawnTeleportHandler(Database db, Location spawnLocation) {
        this.db = db;
        this.spawnLocation = spawnLocation;
    }

    public void initiateTeleport(Player player) {
        TaskChain.create(player.getUniqueId().toString())
            .async(uuid -> {
                var rows = db.executeQuery("SELECT combat_tagged FROM player_states WHERE uuid = ?", uuid).join();
                boolean tagged = !rows.isEmpty() && (int) rows.getFirst().get("combat_tagged") == 1;
                if (tagged) {
                    throw new IllegalStateException("You are in combat!");
                }
                return uuid;
            })
            .sync(uuid -> {
                Text.send(player, "<gray>Teleporting in 3 seconds...</gray>");
                return uuid;
            })
            .delay(Duration.ofSeconds(3))
            .sync(uuid -> {
                player.teleport(spawnLocation);
                Text.send(player, "<green>Teleported to spawn!</green>");
            })
            .onException((uuid, ex) -> {
                Text.send(player, "<red>Teleport failed: " + ex.getMessage() + "</red>");
            })
            .execute();
    }
}
```

---

## Folia Compatibility

Folia schedules synchronous tasks on its global thread pool. For tick-precise regional operations, use location or entity-aware methods, which fallback to standard main thread execution on non-Folia platforms:

```java
import dev.oum.oumlib.scheduler.Scheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public final class RegionalScheduler {
    public void executeRegionalAction(Location location, Entity entity, Runnable action) {
        Scheduler.runAt(location, action);
        Scheduler.runFor(entity, action);
    }
}
```

---

## Promises and Callback Chaining

Execute database queries asynchronously on virtual threads and pass the results to synchronous player interactions safely:

```java
import dev.oum.oumlib.database.Database;
import dev.oum.oumlib.scheduler.Scheduler;
import org.bukkit.entity.Player;

public final class ProfileLoader {
    private final Database db;

    public ProfileLoader(Database db) {
        this.db = db;
    }

    public void loadBalance(Player player) {
        Scheduler.supplyVirtual(() -> {
            var rows = db.executeQuery("SELECT coins FROM economy WHERE uuid = ?", player.getUniqueId().toString()).join();
            return rows.isEmpty() ? 0 : (int) rows.getFirst().get("coins");
        }).thenAcceptSync(coins -> {
            player.sendMessage("Balance: " + coins + " coins.");
        });
    }
}
```
