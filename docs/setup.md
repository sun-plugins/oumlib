# Main Setup & Initialization

OumLib can be integrated either as a shared library plugin (recommended) or shaded directly into your plugin JAR. Depending on the setup method you choose, initialization is either handled automatically by the server/proxy or manually by your plugin code.

## Integration Auto-Detection
During initialization, the library automatically scans the server's plugin manager for supported integrations. You do not need to write any integration code. It currently hooks into:
- **PlaceholderAPI (PAPI)**: Auto-registers registered OumLib placeholders into PAPI so other plugins can use them.
- **MiniPlaceholders**: Bridges OumLib placeholders into MiniPlaceholders' global parsing context.

---

## 1. Shared Plugin Mode (Recommended)

By placing the built `oumlib-plugin` JAR in your server's `plugins` folder, the library operates as a central dependency. Downstream plugins can share a single classloader and reference the library without needing to shade or relocate it.

### Maven Dependency
Declare `oumlib-core` with `<scope>provided</scope>` in your downstream plugin's `pom.xml`:

```xml
<dependency>
    <groupId>com.github.sun-mc-dev.oumlib</groupId>
    <artifactId>oumlib-core</artifactId>
    <version>VERSION</version>
    <scope>provided</scope>
</dependency>
```

### Declaring Dependencies in Metadata

#### Paper/Spigot (`plugin.yml` or `paper-plugin.yml`)
Add `OumLib` as a dependency so your plugin loads after the library:

```yaml
# plugin.yml
depend: [OumLib]

# OR paper-plugin.yml
dependencies:
  - name: OumLib
    required: true
    bootstrap: false
```

#### Velocity (`@Plugin` Annotation)
Declare the dependency on `oumlib` inside your plugin definition:

```java
@Plugin(
    id = "myplugin",
    name = "MyPlugin",
    version = "1.0.0",
    dependencies = {
        @Dependency(id = "oumlib")
    }
)
```

No initialization or shutdown calls (`OumLib.init(...)` / `OumLib.shutdown()`) are required in your plugin code; the central library plugin handles startup and cleanup automatically.

---

## 2. Shaded Mode (Fallback)

### Paper/Spigot Setup

Initialize OumLib in your main class's `onEnable()` method, and call `shutdown()` in `onDisable()` to clean up background threads and scheduler tasks.

```java
package dev.oum.example;

import dev.oum.oumlib.OumLib;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperExamplePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Initialize OumLib for Paper.
        // OumLib.init returns an InitBuilder for method chaining configuration.
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

### The InitBuilder
When calling `OumLib.init()`, it returns an `InitBuilder` which allows fluent configuration of library-wide features:
- `.preset(Preset type, String prefix)`: Customizes formatting prefixes for global text presets.

Example:
```java
OumLib.init(this)
    .preset(Preset.INFO, "<gray>[Info]</gray> ")
    .preset(Preset.SUCCESS, "<green>[Success]</green> ")
    .preset(Preset.ERROR, "<red>[Error]</red> ");
```

---

### Velocity Setup

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

## Platform-Independent Detection Utilities

To facilitate building modules or shared libraries that operate seamlessly across both Paper (server-side) and Velocity (proxy-side), OumLib provides static checks to safely detect the host platform at runtime:

```java
import dev.oum.oumlib.OumLib;

if (OumLib.isPaper()) {
    // Run Paper/Folia-specific code
} else if (OumLib.isVelocity()) {
    // Run Velocity-specific code
}
```

These checks are completely safe to call on either platform. They avoid raising `IllegalStateException` or class loader errors, making it simple to write shared plugins or multi-platform systems.

---

## Shading & Relocation Implications
If you shade OumLib into multiple plugins running on the same server, you **must** relocate `dev.oum.oumlib`. If you do not relocate it:
- The Java Virtual Machine will load only one instance of the `OumLib` class files (usually from the plugin that loaded first).
- Callbacks, scheduler mappings, and internal variables of the other plugins will overwrite each other, causing errors or silent failures.
- Shading relocation keeps your plugin's dependency sandbox isolated.

> [!NOTE]
> All compile-scope dependencies utilized internally by OumLib (such as HikariCP) are automatically relocated to `dev.oum.oumlib.internal.hikari` when the OumLib jar is built. This ensures that downstream plugin developers only need to relocate the main `dev.oum.oumlib` package prefix (e.g. to `your.plugin.oumlib`) and all nested internal dependencies will automatically follow without needing manual relocation rules.
