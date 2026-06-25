# Configuration System

OumLib allows developers to define configuration files as Java `record` types. The library handles YAML parsing, key-merging on upgrades, custom key preservation, and filesystem monitoring.

---

## Real-world Example: Minigame Arena Config

Here is a configuration record for a minigame arena, utilizing comments, nested sections, and default value definitions:

```java
import dev.oum.oumlib.config.Comment;
import dev.oum.oumlib.config.ConfigSection;

public record MySQLCredentials(
    @Comment("Hostname or IP of the MySQL database") String host,
    @Comment("Database port") int port,
    @Comment("Database credentials") String username,
    String password
) implements ConfigSection {}

public record LobbyLocation(
    String world,
    double x,
    double y,
    double z
) implements ConfigSection {}

public record ArenaConfig(
    @Comment("Database connection pool configuration")
    MySQLCredentials database,

    @Comment("Arena lobby spawn location")
    LobbyLocation lobby,

    @Comment("Maximum players allowed inside this arena")
    int maxPlayers,

    @Comment("Whether debug messages are printed to the console")
    boolean debugMode
) implements ConfigSection {}
```

This generates the following structured YAML layout automatically:
```yaml
# Database connection pool configuration
database:
  # Hostname or IP of the MySQL database
  host: "127.0.0.1"
  # Database port
  port: 3306
  # Database credentials
  username: "root"
  password: "password"

# Arena lobby spawn location
lobby:
  world: "world"
  x: 0.0
  y: 64.0
  z: 0.0

# Maximum players allowed inside this arena
maxPlayers: 16

# Whether debug messages are printed to the console
debugMode: true
```

---

## Loading and Auto-Reload Watcher

Set up a configuration file mapping and enable background file watch services to automatically re-read values and fire updates:

```java
import dev.oum.oumlib.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ArenaManager {
    private ConfigManager<ArenaConfig> configManager;

    public void initialize(JavaPlugin plugin) {
        MySQLCredentials defaultDb = new MySQLCredentials("127.0.0.1", 3306, "root", "password");
        LobbyLocation defaultLobby = new LobbyLocation("world", 0.0, 64.0, 0.0);
        
        configManager = ConfigManager.of(ArenaConfig.class, "arena.yml", 
            () -> new ArenaConfig(defaultDb, defaultLobby, 16, true)
        ).enableAutoReload();

        configManager.onReload(newConfig -> {
            plugin.getLogger().info("Arena configurations re-read from disk successfully!");
            applyNewSettings(newConfig);
        });
    }

    private void applyNewSettings(ArenaConfig config) {
        System.out.println("Maximum players updated to: " + config.maxPlayers());
    }
}
```

---

## Configuration Schema Auto-Migration

To modify keys and values as your plugin version upgrades, register sequential version migrations:

```java
import dev.oum.oumlib.config.ConfigManager;
import dev.oum.oumlib.config.ConfigMigrationRegistry;

public final class ArenaUpgrader {
    public void setupMigrations(ConfigManager<ArenaConfig> manager) {
        manager.migrate(new ConfigMigrationRegistry()
            .add(2, map -> {
                if (map.containsKey("old-max-players")) {
                    map.put("maxPlayers", map.remove("old-max-players"));
                }
            })
            .add(3, map -> map.putIfAbsent("debugMode", false))
        );
    }
}
```

When OumLib loads the config:
1. It reads the current `config-version` inside the YAML file (defaults to `1` if not found).
2. It applies each registered migration step with a key higher than the file's current version sequentially.
3. It updates `config-version` to the highest migrated version.
4. It saves the modified YAML structure back to disk automatically.
