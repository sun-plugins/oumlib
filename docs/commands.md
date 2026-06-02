# Command System

OumLib features a builder-based wrapper for Brigadier, providing modern command registration with platform-agnostic structures, typed arguments, dynamic completions, and integrated cooldowns.

## Registering a Command

A command consists of literals (names), arguments (parameters), subcommands, execution blocks, and optional parameters like permissions and cooldowns.

```java
import dev.oum.oumlib.command.Arguments;
import dev.oum.oumlib.command.CommandBuilder;
import dev.oum.oumlib.text.Text;
import java.time.Duration;
import java.util.List;

public class MyCommands {

    public static void register() {
        // Define an argument with custom suggestions
        var modeArg = Arguments.string("mode")
            .suggests(context -> List.of("survival", "creative", "adventure", "spectator"));

        CommandBuilder.literal("gamemode")
            .permission("myplugin.gamemode")
            .cooldown(
                Duration.ofSeconds(5), 
                "<red>Please wait <remaining> seconds before changing gamemodes again.</red>"
            )
            .argument(modeArg)
            .executes(context -> {
                if (!context.isPlayer()) {
                    Text.Preset.error(context.sender(), "Only players can use this command.");
                    return;
                }
                
                String selectedMode = context.args().get(modeArg);
                Text.Preset.success(context.sender(), "Changed gamemode to: " + selectedMode);
            })
            .register();
    }
}
```

---

## The CommandContext Structure

The `CommandContext` object represents the execution environment:

- `context.sender()`: Returns the Kyori `Audience` representing the command executor. On Paper, this can be cast directly to a Bukkit `Player` or `ConsoleCommandSender`.
- `context.playerOrThrow()`: Returns the player object cast to the appropriate platform type, throwing an `IllegalStateException` if the sender is not a player.
- `context.isPlayer()`: Utility check returning `true` if the sender is a player.
- `context.isConsole()`: Utility check returning `true` if the sender is the console.
- `context.source()`: The underlying platform execution source. On Paper, this is Brigadier's `CommandSourceStack`. On Velocity, it is a `CommandSource`.
- `context.args()`: Accessor for parsed command arguments.

---

## Custom suggestions

To register tab completions for arguments dynamically, use the `.suggests(...)` method. You can supply a static list or compute suggestions dynamically using the executor:

```java
var warpArg = Arguments.string("warp")
    .suggests(context -> {
        // Return a list of strings to display in tab completion
        return List.of("spawn", "shop", "pvp", "lounge");
    });
```

---

## Cooldowns & Bypasses

When you configure a command cooldown:
```java
.cooldown(Duration.ofSeconds(10), "Cooldown active: <remaining>s")
```
OumLib handles rate-limiting per player UUID automatically.
- **Bypass Permission**: Any player who possesses the bypass permission will not trigger the cooldown. The bypass permission is automatically calculated as:
  `<command_permission>.bypass` (e.g. `myplugin.gamemode.bypass`)
  If the command has no permission defined, it defaults to:
  `<command_label>.bypass` (e.g. `gamemode.bypass`)
