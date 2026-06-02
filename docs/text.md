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
    .add("coins", player -> {
        // Since OumLib is cross-platform, player is passed as Object.
        // Cast it to org.bukkit.entity.Player (Paper) or com.velocitypowered.api.proxy.Player (Velocity)
        if (player instanceof org.bukkit.entity.Player p) {
            return String.valueOf(p.getLevel() * 10);
        }
        return "0";
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

// 4. Broadcast to action bars
Text.broadcastActionBar("<red>Warning: System overload!</red>");

// 5. Broadcast global titles
Text.broadcastTitle("<gold>VICTORY!</gold>", "<gray>Blue team won the match</gray>");
```

### Broadcast Presets
You can also trigger styled broadcasts matching your registered preset styles:

```java
// Sends success message with success prefix to all players
Text.Preset.successBroadcast("A global event has begun!");

// Sends error message with error prefix to all players
Text.Preset.errorBroadcast("The database connection was lost!");
```

---

## 6. Localization & Multi-Language (I18n)

OumLib features a built-in localization (I18n) system that automatically loads, parses, and translates keys into localized `Component`s using MiniMessage format. It supports player client locale translation out-of-the-box on both Paper and Velocity.

### Initializing the System

To initialize, specify the default language code (e.g. `en`). OumLib will look inside the `lang/` subfolder in your plugin's data folder (and automatically extract the default lang file if it doesn't exist):

```java
import dev.oum.oumlib.text.Localization;

// Load translation files (e.g. lang/en.yml, lang/es.yml)
Localization.load("en");
```

### Structuring Lang Files

Language files use standard nested YAML, which OumLib flattens to dot-notation paths internally:

```yaml
# lang/en.yml
general:
  welcome: "<green>Welcome back, <player>!</green>"
  no-permission: "<red>You do not have permission to do this.</red>"
```

### Translating Messages

You can translate using the default language, or fetch translations dynamically targeting a player's client language:

```java
import dev.oum.oumlib.text.Localization;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

Player player = ...;

// 1. Send localized message matching player's Minecraft client locale settings (e.g. Spanish)
player.sendMessage(Localization.translateFor(player, "general.welcome", 
    Placeholder.parsed("player", player.getName())
));

// 2. Fallback to default server translation
Component msg = Localization.translate("general.no-permission");
```

