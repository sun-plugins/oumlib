# Web & Discord Webhooks

OumLib includes a built-in, lightweight, asynchronous Discord Webhook client that relies on Java's standard `HttpClient` and a custom, zero-reflection JSON builder.

---

## Real-world Example: Anti-Cheat Alert Dispatcher

Here is an anti-cheat logging manager that queues and batches player alerts in the background. If 20 players flag checks at the same time, OumLib batches them into combined embeds sent every second, preventing Discord API rate limit blocks:

```java
import dev.oum.oumlib.web.Webhook;
import dev.oum.oumlib.web.WebhookEmbed;
import org.bukkit.entity.Player;

public final class AntiCheatDiscordLogger {
    private final String webhookUrl;

    public AntiCheatDiscordLogger(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void logDetection(Player player, String hackType, double violationLevel) {
        WebhookEmbed alertEmbed = WebhookEmbed.builder()
            .title("Security Alert")
            .description("Player **" + player.getName() + "** failed security verification checks.")
            .color(0xFF3333)
            .field("Violator", player.getName(), true)
            .field("Detection", hackType, true)
            .field("VL Score", String.valueOf(violationLevel), true)
            .footer("AntiCheat Security Daemon", null)
            .build();

        Webhook.queueEmbed(webhookUrl, alertEmbed);
    }
}
```

---

## Simple Discord Webhook Sending

Send standard Discord webhooks asynchronously:

```java
import dev.oum.oumlib.web.Webhook;

public final class StatusNotifier {
    public void notifyStartup(String url) {
        Webhook.url(url)
            .username("Status Monitor")
            .content("Plugin successfully initialized on server startup.")
            .sendAsync();
    }
}
```
