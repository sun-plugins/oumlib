# Main Setup & Initialization

OumLib must be initialized during your plugin's enable phase and shut down during the disable phase. This configures the internal scheduler adapters, registers the event bus, and automatically hooks into server-wide systems.

## Integration Auto-Detection
When you call `OumLib.init()`, the library automatically scans the server's plugin manager for supported integrations. You do not need to write any integration code. It currently hooks into:
- **PlaceholderAPI (PAPI)**: Auto-registers registered OumLib placeholders into PAPI so other plugins can use them.
- **MiniPlaceholders**: Bridges OumLib placeholders into MiniPlaceholders' global parsing context.

---

## Paper/Spigot Setup

Initialize OumLib in your main class's `onEnable()` method, and call `shutdown()` in `onDisable()` to clean up background threads and scheduler tasks.

```java
package dev.oum.example;

import dev.oum.oumlib.OumLib;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperExamplePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Initialize OumLib for Paper.
        // This registers command builders, listeners, and integration bridges.
        OumLib.init(this);
    }

    @Override
    public void onDisable() {
        // Shutdown all active background tasks, repeating loops,
        // and threads created by the OumLib scheduler.
        OumLib.shutdown();
    }
}
```

---

## Velocity Setup

Initialize OumLib in your proxy plugin constructor or proxy initialization subscriber. 

```java
package dev.oum.example;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.oum.oumlib.OumLib;

@Plugin(id = "example-proxy", name = "ExampleProxy", version = "1.0.0")
public final class VelocityExamplePlugin {

    private final ProxyServer server;

    @Inject
    public VelocityExamplePlugin(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Initialize OumLib for Velocity.
        OumLib.init(server, this);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        // Stop all active background scheduler tasks.
        OumLib.shutdown();
    }
}
```

---

## Shading & Relocation Implications
If you shade OumLib into multiple plugins running on the same server, you **must** relocate `dev.oum.oumlib`. If you do not relocate it:
- The Java Virtual Machine will load only one instance of the `OumLib` class files (usually from the plugin that loaded first).
- Callbacks, scheduler mappings, and internal variables of the other plugins will overwrite each other, causing errors or silent failures.
- Shading relocation keeps your plugin's dependency sandbox isolated.
