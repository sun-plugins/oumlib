# Database Tools

OumLib includes a high-performance SQL database wrapper powered by HikariCP supporting both SQLite and MySQL. It utilizes virtual threads via `Promise` for non-blocking asynchronous operations.

---

## Real-world Example: Player Profile Management

Here is a profile system that loads players' statistics upon connection, updates their score values, and handles transaction-safe currency transfers between two players:

```java
import dev.oum.oumlib.database.Database;
import dev.oum.oumlib.text.Text;
import org.bukkit.entity.Player;
import java.io.File;
import java.util.UUID;

public record UserProfile(String uuid, int coins, int level) {}

public final class ProfileDatabaseManager {
    private final Database db;

    public ProfileDatabaseManager(File dataFolder) {
        db = Database.sqlite(new File(dataFolder, "data.db"));
        db.executeUpdate("CREATE TABLE IF NOT EXISTS profiles (uuid VARCHAR(36) PRIMARY KEY, coins INT, level INT)");
    }

    public void loadProfile(Player player) {
        db.executeQuery("SELECT uuid, coins, level FROM profiles WHERE uuid = ?", UserProfile.class, player.getUniqueId().toString())
            .thenAcceptSync(profiles -> {
                if (profiles.isEmpty()) {
                    createDefaultProfile(player);
                    return;
                }
                
                UserProfile profile = profiles.getFirst();
                Text.send(player, "<green>Profile loaded: Level " + profile.level() + " (" + profile.coins() + " coins)</green>");
            });
    }

    private void createDefaultProfile(Player player) {
        db.executeUpdate("INSERT INTO profiles (uuid, coins, level) VALUES (?, 100, 1)", player.getUniqueId().toString())
            .thenAcceptSync(v -> Text.send(player, "<green>Default profile created!</green>"));
    }

    public void transferCoins(Player sender, UUID targetUuid, int amount) {
        db.transaction(ctx -> {
            var senderRows = ctx.executeQuery("SELECT coins FROM profiles WHERE uuid = ?", sender.getUniqueId().toString());
            if (senderRows.isEmpty()) {
                throw new IllegalStateException("Profile not found");
            }
            
            int senderCoins = (int) senderRows.getFirst().get("coins");
            if (senderCoins < amount) {
                throw new IllegalStateException("Insufficient balance");
            }

            ctx.executeUpdate("UPDATE profiles SET coins = coins - ? WHERE uuid = ?", amount, sender.getUniqueId().toString());
            ctx.executeUpdate("UPDATE profiles SET coins = coins + ? WHERE uuid = ?", amount, targetUuid.toString());
            
            return true;
        }).whenCompleteSync(
            success -> Text.send(sender, "<green>Transferred " + amount + " coins successfully!</green>"),
            error -> Text.send(sender, "<red>Transaction aborted: " + error.getMessage() + "</red>")
        );
    }

    public void close() {
        db.close();
    }
}
```

---

## Connection Setup Reference

Establish optimized database connection pools:

### SQLite Connection
```java
import dev.oum.oumlib.database.Database;
import java.io.File;

public class SqliteConfig {
    public Database init(File dataFolder) {
        return Database.sqlite(new File(dataFolder, "data.db"));
    }
}
```

### MySQL Connection
```java
import dev.oum.oumlib.database.Database;

public class MysqlConfig {
    public Database init() {
        return Database.mysql("127.0.0.1", 3306, "my_database", "username", "password", config -> {
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setPoolName("Plugin-Pool");
        });
    }
}
```
