# Text & Placeholders System

OumLib handles chat formatting and text rendering using Kyori Adventure's MiniMessage format. It includes default message styling presets and a custom placeholder registration system that bridges dynamically into PlaceholderAPI (PAPI) and MiniPlaceholders.

---

## Real-world Example: Chat Trivia Challenge

Here is a chat trivia challenge manager. It broadcasts localized question messages and opens a secure chat-capture session for the player using `TextInput`, validating answers and playing audio cues:

```java
import dev.oum.oumlib.effect.Effects;
import dev.oum.oumlib.text.Localization;
import dev.oum.oumlib.text.Text;
import dev.oum.oumlib.text.TextInput;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import java.time.Duration;

public final class TriviaChallengeManager {
    public void startTrivia(Player player) {
        player.sendMessage(Localization.translateFor(player, "trivia.start"));

        TextInput.builder()
            .prompt(Localization.translateFor(player, "trivia.question"))
            .timeout(Duration.ofSeconds(15))
            .cancelWord("quit")
            .onTimeout(p -> {
                Text.send(p, "<red>Time's up! You failed the challenge.</red>");
                Effects.sound(Sound.BLOCK_ANVIL_LAND).volume(1.0F).pitch(1.0F).play(p);
            })
            .onCancel(p -> {
                Text.send(p, "<gray>Trivia session closed.</gray>");
            })
            .onInput((p, answer) -> {
                if (answer.equalsIgnoreCase("Minecraft")) {
                    Text.send(p, "<green>Correct answer!</green>");
                    Effects.sound(Sound.ENTITY_PLAYER_LEVELUP).volume(1.0F).pitch(1.0F).play(p);
                    
                    p.sendMessage(Localization.translateFor(p, "trivia.completed", 
                        Placeholder.parsed("score", "100")
                    ));
                    return true;
                }
                
                Text.send(p, "<red>Incorrect! Try again (or type 'quit'):</red>");
                return false;
            })
            .start(player);
    }
}
```

YAML translation resource file (`lang/en.yml`):
```yaml
trivia:
  start: "<gold>[Trivia]</gold> A new challenge has started!"
  question: "<yellow>[Trivia]</yellow> What game is this plugin written for?"
  completed: "<green>Challenge completed! Score added: <score></green>"
```

---

## Text Presets

OumLib defines standard text presets for consistent messaging styles:

```java
import dev.oum.oumlib.text.Text;
import org.bukkit.entity.Player;

public class FeedbackSender {
    public void sendFeedback(Player player) {
        Text.Preset.info(player, "Your profile is loading.");
        Text.Preset.success(player, "Coins added to balance.");
        Text.Preset.error(player, "Payment rejected!");
    }
}
```

---

## Placeholder Registration

Register custom placeholders under your plugin's identifier namespace. Registered placeholders are automatically bridged into PAPI and MiniPlaceholders:

```java
import dev.oum.oumlib.OumLib;
import org.bukkit.entity.Player;

public class LevelPlaceholderRegistry {
    public void register() {
        OumLib.placeholders("myplugin")
            .add("level", player -> {
                if (player instanceof Player p) {
                    return String.valueOf(p.getLevel());
                }
                return "0";
            });
    }
}
```

---

## Global Broadcasts

Broadcast MiniMessage-formatted text, action bars, or titles to all connected players or console:

```java
import dev.oum.oumlib.text.Text;

public class AlertBroadcaster {
    public void announceMaintenance() {
        Text.broadcast("<red>[Alert]</red> Maintenance starts in 10 minutes!");
        Text.broadcastActionBar("<red>Warning: Save progress now!</red>");
        Text.broadcastTitle("<red>MAINTENANCE</red>", "<gray>Please finish transactions</gray>");
    }
}
```
