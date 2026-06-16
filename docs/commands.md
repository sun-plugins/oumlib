# Command System

OumLib features a builder-based wrapper for Brigadier, providing modern command registration with platform-agnostic structures, typed arguments, dynamic completions, and integrated cooldowns.

## Registering a Command

A command consists of literals (names), arguments (parameters), subcommands, execution blocks, and optional parameters like permissions and cooldowns.

```java
import dev.oum.oumlib.command.Arguments;
import dev.oum.oumlib.command.Commands;
import dev.oum.oumlib.text.Text;
import dev.oum.oumlib.util.Permission;
import java.time.Duration;
import java.util.List;

public class MyCommands {

    public static void register() {
        Permission gamemodePermission = Permission.builder("myplugin.gamemode")
            .description("Allows player to change gamemodes")
            .defaultValue(Permission.Default.OP)
            .build();

        // Define an argument with custom suggestions
        var modeArg = Arguments.string("mode")
            .suggests(context -> List.of("survival", "creative", "adventure", "spectator"));

        Commands.literal("gamemode")
            .permission(gamemodePermission)
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

## Subcommands

You can attach subcommands to your command builder using `.subcommand(Consumer<SubcommandBuilder>)`. Subcommands support their own permissions, arguments, and execution logic.

```java
Permission warpPermission = Permission.builder("myplugin.warp").build();
Permission warpAdminPermission = Permission.builder("myplugin.warp.admin").build();

Commands.literal("warp")
    .permission(warpPermission)
    .subcommand(sub -> sub
        .label("create")
        .aliases("set", "add") // Registers aliases for subcommand
        .permission(warpAdminPermission)
        .argument(Arguments.string("name"))
        .executes(context -> {
            String name = context.args().getString("name");
            Text.Preset.success(context.sender(), "Warp created: " + name);
        })
    )
    .subcommand(sub -> sub
        .label("tp")
        .argument(Arguments.string("name"))
        .executes(context -> {
            String name = context.args().getString("name");
            Text.Preset.info(context.sender(), "Teleporting to: " + name);
        })
    )
    .register();
```

---

## Command Permissions

You can secure commands and subcommands using raw permission `String` nodes, or by using OumLib's cross-platform `Permission` utility:

```java
import dev.oum.oumlib.command.Commands;
import dev.oum.oumlib.util.Permission;

public class AdminCommand {

    private static final Permission ADMIN_PERM = Permission.builder("myplugin.admin")
        .description("Allows admin command execution")
        .defaultValue(Permission.Default.OP)
        .build();

    public static void register() {
        Commands.literal("admin")
            .permission(ADMIN_PERM) // Natively supports OumLib's Permission objects
            .executes(context -> {
                Text.Preset.success(context.sender(), "Admin menu opened.");
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
- `context.args()`: Accessor for parsed command arguments. You can fetch arguments by their `Argument<T>` definition, or by their parameter name directly:
  - `args.get(Argument<T>)`: Returns the type-safe parsed value.
  - `args.getString("name")`: Returns the parsed String, or `""` if not found.
  - `args.getInt("name")`: Returns the parsed integer, or `0` if not found.
  - `args.getDouble("name")`: Returns the parsed double, or `0.0` if not found.
  - `args.getBoolean("name")`: Returns the parsed boolean, or `false` if not found.
  - `args.get("name", Class<T>)`: Returns the parsed object of the specified class directly from Brigadier.
- `context.reply(Component)`: Sends a pre-built Adventure `Component` to the sender.
- `context.reply(String, TagResolver...)`: Parses a MiniMessage template with optional resolvers and sends it.
- `context.sendTranslated(String, TagResolver...)`: Automatically detects the sender's language locale, resolves the translation key from `Localization` files, parses MiniMessage placeholders, and sends the localized message.
- `context.sendActionBar(String / Component, TagResolver...)`: Sends a MiniMessage-parsed or raw component action bar message to the sender.
- `context.sendTitle(String title, String subtitle, TagResolver...)`: Shows a title/subtitle parsed via MiniMessage.
- `context.sendTitle(String title, String subtitle, Duration fadeIn, Duration stay, Duration fadeOut, TagResolver...)`: Shows a title with specific fade-in, stay, and fade-out timings.
- `context.clearTitle()`: Clears any currently displayed titles.

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

## Rich Suggestions with Tooltips

On modern Brigadier platforms, tab completion suggestions can show hoverable descriptive tooltips. OumLib supports this via `RichSuggestion` and `.suggestsRich(...)`:

```java
import dev.oum.oumlib.command.RichSuggestion;
import dev.oum.oumlib.command.Arguments;

var warpArg = Arguments.string("warp")
    .suggestsRich(context -> List.of(
        RichSuggestion.of("spawn", "Teleport to the main lobby spawn area"),
        RichSuggestion.of("pvp", "Teleport to the PvP combat arena"),
        RichSuggestion.of("shop", "Teleport to the server marketplace")
    ));
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

---

## Command Exception Handling

To prevent raw stack traces from leaking to players and to log command errors gracefully, OumLib features a centralized exception handling pipeline.

### 1. Global Command Error Handler
Configure a fallback handler during OumLib initialization to log command execution failures globally (e.g. sending alerts to Discord or external log services):

```java
OumLib.init(this)
    .commandErrorHandler((context, exception) -> {
        // Send a custom error message to the player
        context.sender().sendMessage(MiniMessage.miniMessage()
            .deserialize("<red>An unexpected error occurred: " + exception.getMessage() + "</red>"));
            
        // Log to console/SLF4J
        OumLib.logError("Unhandled error in /" + context.label(), exception);
    });
```

### 2. Builder-Specific Exception Handler
Define a custom handler for a single command to clean up local resources, reset user state, or display custom transaction error screens:

```java
Commands.literal("buy")
    .onException((context, exception) -> {
        context.sender().sendMessage("<red>Transaction failed: Your balance was not charged.</red>");
    })
    .executes(context -> {
        // Business logic that may throw payment exceptions
    });
```

