# Text & Placeholders

OumLib handles chat and message rendering using Kyori Adventure's **MiniMessage** format. It includes default message styling presets and a custom placeholder registration system that bridges dynamically into PlaceholderAPI (PAPI) and MiniPlaceholders.

---

## 1. MiniMessage & Formatting

All text functions in OumLib parse MiniMessage format by default. You can use standard MiniMessage tags like `<color>`, `<hover>`, and `<click>`:

```java
import dev.oum.oumlib.text.Text;

Text.send(player, "<gold>Welcome!</gold> Click <red><hover:show_text:'Go!'>[here]</hover></red>.");
```

---

## 2. Text Presets

OumLib defines standard text presets to ensure clean, consistent messaging styles across your plugin:
- **`Text.Preset.info(player, message)`**: Displays a standard informational message (typically gray/blue).
- **`Text.Preset.success(player, message)`**: Displays a success message (typically green).
- **`Text.Preset.error(player, message)`**: Displays an error message (typically red).

You can configure the prefix for these presets during OumLib initialization:
```java
OumLib.init(this)
    .preset(Preset.INFO, "<gray>[Info]</gray> ")
    .preset(Preset.SUCCESS, "<green>[Success]</green> ")
    .preset(Preset.ERROR, "<red>[Error]</red> ");
```

---

## 3. Placeholder System

You can register custom placeholders under your plugin's identifier. 

### Dynamic Placeholder Mappings
When you register a placeholder (e.g. `coins` under the namespace `myplugin`):
1. **OumLib Internal Resolver**: Resolves `<myplugin_coins>` inside OumLib text calls.
2. **PlaceholderAPI Bridge**: Automatically registers `%myplugin_coins%` in PlaceholderAPI.
3. **MiniPlaceholders Bridge**: Automatically registers `<myplugin_coins>` in MiniPlaceholders.

```java
import dev.oum.oumlib.OumLib;

// Register player-specific coins placeholder
OumLib.placeholders("myplugin")
    .register("coins", player -> {
        // Fetch values dynamically
        int count = Database.getCoins(player.getUniqueId());
        return String.valueOf(count);
    });
```

---

## 4. Manual Placeholder Resolution

If you need to parse placeholders inside a raw string manually (handling both MiniMessage format and legacy PlaceholderAPI formats), use `Placeholders.resolve()`:

```java
import dev.oum.oumlib.text.Placeholders;

String parsed = Placeholders.resolve(
    player, 
    "You have %myplugin_coins% coins remaining."
);
```
OumLib will first evaluate standard placeholders internally, and if PlaceholderAPI is enabled on the server, it will pass the text to PAPI for a secondary evaluation, resolving all server-wide placeholders.

---

## 5. Global Broadcasts & Platform-Agnostic Audiences

Instead of manually fetching players from Paper/Velocity APIs to send messages, OumLib provides platform-agnostic methods to access all connected players or the console.

### Platform-Agnostic Audiences
- **`OumLib.players()`**: Returns a unified Kyori `Audience` containing all online players on the server (Paper) or proxy (Velocity).
- **`OumLib.console()`**: Returns the console command sender/source `Audience`.

### Global Broadcast Methods
You can broadcast MiniMessage formatted text to all online players dynamically:

```java
import dev.oum.oumlib.text.Text;

// 1. Simple text broadcast
Text.broadcast("<yellow>The server is reloading in 5 minutes!</yellow>");

// 2. Broadcast with placeholders
Text.broadcast("<yellow>Winner is <winner>!</yellow>", "winner", playerName);

// 3. Broadcast with a Record data injector
Text.broadcast("<gold>Stats update: Joins: <joins> | Sent: <sent></gold>", myStatsRecord);
```

### Broadcast Presets
You can also trigger styled broadcasts matching your registered preset styles:

```java
// Sends success message with success prefix to all players
Text.Preset.successBroadcast("A global event has begun!");

// Sends error message with error prefix to all players
Text.Preset.errorBroadcast("The database connection was lost!");
```

