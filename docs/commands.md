# Commands & Brigadier Wrapper

OumLib features a builder-based wrapper for Brigadier, providing modern command registration with platform-agnostic structures, typed arguments, completions, and cooldowns.

---

## Real-world Example: Warp System

Here is a warp command system supporting coordinates storage, permissions, a teleportation cooldown, and rich hover tooltips for tab completion suggestions:

```java
import dev.oum.oumlib.command.Arguments;
import dev.oum.oumlib.command.Commands;
import dev.oum.oumlib.command.Argument;
import dev.oum.oumlib.command.RichSuggestion;
import dev.oum.oumlib.text.Text;
import dev.oum.oumlib.util.Permission;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WarpCommandRegistry {
    private final Map<String, Location> warps = new HashMap<>();

    public void register() {
        Permission warpPermission = Permission.builder("myplugin.warp.use").build();
        Permission adminPermission = Permission.builder("myplugin.warp.admin").build();

        Argument<String> warpArg = Arguments.string("warp")
            .suggestsRich(context -> List.of(
                RichSuggestion.of("spawn", "Teleport to the main server spawn"),
                RichSuggestion.of("pvp", "Teleport to the PvP combat arena"),
                RichSuggestion.of("shop", "Teleport to the server shop market")
            ));

        Commands.literal("warp")
            .permission(warpPermission)
            .cooldown(Duration.ofSeconds(10), "<red>Wait <remaining>s before warping again.</red>")
            .argument(warpArg)
            .executes(context -> {
                if (!context.isPlayer()) {
                    Text.send(context.sender(), "<red>Console cannot teleport!</red>");
                    return;
                }

                Player player = context.playerOrThrow();
                String warpName = context.args().get(warpArg);
                Location loc = warps.get(warpName);

                if (loc == null) {
                    Text.send(player, "<red>Warp '" + warpName + "' does not exist!</red>");
                    return;
                }

                player.teleport(loc);
                Text.send(player, "<green>Warped to " + warpName + "!</green>");
            })
            .subcommand(sub -> sub
                .label("set")
                .permission(adminPermission)
                .argument(Arguments.string("name"))
                .executes(context -> {
                    if (!context.isPlayer()) {
                        Text.send(context.sender(), "<red>Only players can set warps.</red>");
                        return;
                    }

                    Player player = context.playerOrThrow();
                    String warpName = context.args().getString("name");
                    warps.put(warpName, player.getLocation());
                    Text.send(player, "<green>Warp '" + warpName + "' has been set to your location!</green>");
                })
            )
            .register();
    }
}
```

---

## Command Context API Reference

The `CommandContext` object represents the execution environment:

- `context.sender()`: Returns the Kyori `Audience` representing the command executor.
- `context.playerOrThrow()`: Returns the player object cast to the appropriate platform type.
- `context.isPlayer()`: Returns `true` if the sender is a player.
- `context.isConsole()`: Returns `true` if the sender is the console.
- `context.args()`: Accessor for parsed command arguments:
  - `args.get(Argument<T>)`: Returns the type-safe parsed value.
  - `args.getString("name")`: Returns the parsed String, or `""` if not found.
  - `args.getInt("name")`: Returns the parsed integer, or `0` if not found.
  - `args.getDouble("name")`: Returns the parsed double, or `0.0` if not found.
  - `args.getBoolean("name")`: Returns the parsed boolean, or `false` if not found.
- `context.reply(Component)`: Sends a pre-built Adventure `Component` to the sender.
- `context.reply(String, TagResolver...)`: Parses a MiniMessage template and sends it.

---

## Cooldowns & Bypasses

Configure rate-limits per player UUID automatically:
```java
.cooldown(Duration.ofSeconds(10), "Cooldown active: <remaining>s")
```

Any player who possesses the bypass permission will not trigger the cooldown. The bypass permission is automatically calculated as:
`<command_permission>.bypass` (e.g. `myplugin.warp.use.bypass`)
If the command has no permission defined, it defaults to:
`<command_label>.bypass` (e.g. `warp.bypass`)

---

## Command Exception Handling

Configure a fallback error handler globally during OumLib initialization or define builder-specific callbacks:

### Global Command Error Handler
```java
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.text.Text;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandInitializer {
    public void setup(JavaPlugin plugin) {
        OumLib.init(plugin)
            .commandErrorHandler((context, exception) -> {
                Text.send(context.sender(), "<red>An error occurred executing this command: " + exception.getMessage() + "</red>");
            });
    }
}
```

### Builder-Specific Exception Handler
```java
import dev.oum.oumlib.command.Commands;
import dev.oum.oumlib.text.Text;

public class TransactionCommand {
    public void register() {
        Commands.literal("pay")
            .onException((context, exception) -> {
                Text.send(context.sender(), "<red>Payment failed: Transaction rolled back.</red>");
            })
            .executes(context -> {
                throw new RuntimeException("Bank server timed out");
            })
            .register();
    }
}
```
