# Configuration System

OumLib allows developers to define configuration files as Java `record` types. The library handles YAML parsing, key-merging on upgrades, custom/unknown user key preservation, and filesystem monitoring.

---

## 1. Nested Configurations

You can organize configuration files by nesting records. Any record that implements `ConfigSection` can be used as a component of another configuration record:

```java
import dev.oum.oumlib.config.Comment;
import dev.oum.oumlib.config.ConfigSection;

public record DatabaseSettings(
    String host,
    int port,
    String username,
    String password
) implements ConfigSection {}

public record PluginConfig(
    @Comment("Database configuration settings.")
    DatabaseSettings database,

    @Comment("Enable debug logs.")
    boolean debug
) implements ConfigSection {}
```

This will automatically generate a nested YAML format:
```yaml
# Database configuration settings.
database:
  host: "localhost"
  port: 3306
  username: "root"
  password: "password"

# Enable debug logs.
debug: true
```

---

## 2. Supported Data Types
When parsing configuration files, OumLib reads values and automatically converts them to their corresponding Java types:
- **Strings**: Surrounded by single/double quotes, or left raw. Quotes are stripped and internal escapes (e.g. `\"`) are resolved.
- **Numbers**: Values with decimal points are parsed as `Double`. Values without decimals are parsed as `Integer`.
- **Booleans**: Case-insensitive matches for `true` and `false`.
- **Nulls**: Case-insensitive matches for `null`.

---

## 3. Preserving Custom/Unknown Keys
If a user adds custom keys to the YAML configuration file (e.g., custom metadata or variables for external integrations), OumLib detects that they do not belong to the Java record class.
- Rather than discarding them when the configuration is rewritten or saved, OumLib **tracks them internally**.
- When `save()` is executed, the library appends all unknown keys back into the file under an `# Additional keys` comment header, preserving user data.

---

## 4. How the Auto-Reload Watcher Works
When you call `.enableAutoReload()`:
1. OumLib registers a Java `WatchService` on the plugin's data folder directory path.
2. It starts a platform-safe **Virtual Thread** to monitor standard filesystem events in the background.
3. When the service catches an `ENTRY_MODIFY` event for the specific configuration file (e.g., `config.yml`), it triggers the load sequence.
4. **Key Mismatch Safety**: The loader parses the modified file, merges any missing default values, and determines if it needs to rewrite the file. If all keys are present, it loads the file silently into memory. It only writes to disk if keys are missing, preventing recursive write-watch loops.
5. If the reload succeeds, any registered `onReload(Consumer<T>)` callbacks are invoked on the virtual thread.

---

## 5. Configuration Schema Auto-Migration

To handle config changes as your plugin evolves, OumLib provides a step-based migration system using `ConfigMigrationRegistry`. This allows you to rename keys, set default values, or restructure existing files on start-up.

### Defining and Registering Migrations:
Register sequential upgrade blocks targeting specific config version numbers:

```java
import dev.oum.oumlib.config.ConfigManager;
import dev.oum.oumlib.config.ConfigMigrationRegistry;

ConfigManager<PluginConfig> manager = ConfigManager.of(
    PluginConfig.class,
    "config.yml",
    PluginConfig::new
);

manager.migrate(new ConfigMigrationRegistry()
    .add(2, map -> {
        // Version 2 migration: Rename 'old-cooldown' to 'cooldown-seconds'
        if (map.containsKey("old-cooldown")) {
            map.put("cooldown-seconds", map.remove("old-cooldown"));
        }
    })
    .add(3, map -> {
        // Version 3 migration: Set default value for a new configuration key
        map.putIfAbsent("enable-mysql", false);
    })
);
```

When OumLib loads the config:
1. It reads the current `config-version` inside the YAML file (defaults to `1` if not found).
2. It applies each registered migration step with a key higher than the file's current version sequentially.
3. It updates `config-version` to the highest migrated version.
4. It saves the modified YAML structure back to disk automatically.

