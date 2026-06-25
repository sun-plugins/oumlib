# Setup & Initialization

OumLib is designed to be shaded and relocated directly into your plugin JAR.

---

## Auto-Detection of Integrations

During initialization, OumLib automatically scans the server's plugin environment and hooks into the following platforms if detected:
- **PlaceholderAPI (PAPI)**: Auto-registers OumLib placeholders into PAPI.
- **MiniPlaceholders**: Bridges OumLib placeholders into the MiniPlaceholders parsing context.

---

## 1. Maven Dependency Configuration

Add the JitPack repository and declare `oumlib-core` with `compile` scope in your plugin's `pom.xml`:

```xml
<dependency>
    <groupId>com.github.sun-mc-dev.oumlib</groupId>
    <artifactId>oumlib-core</artifactId>
    <version>VERSION</version>
    <scope>compile</scope>
</dependency>
```

You must relocate OumLib inside your package space to prevent conflicts with other plugins using different versions of the library:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.6.2</version>
            <configuration>
                <createDependencyReducedPom>false</createDependencyReducedPom>
                <relocations>
                    <relocation>
                        <pattern>dev.oum.oumlib</pattern>
                        <shadedPattern>your.plugin.package.libs.oumlib</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## 2. Bootstrapping OumLib (Paper / Spigot)

Initialize OumLib in your main class's `onEnable()` method, and call `shutdown()` in `onDisable()` to clean up scheduled tasks and databases:

```java
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.text.Preset;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        OumLib.init(this)
            .preset(Preset.INFO, "<gray>[MyPlugin]</gray> ")
            .preset(Preset.SUCCESS, "<green>[MyPlugin - Success]</green> ")
            .preset(Preset.ERROR, "<red>[MyPlugin - Error]</red> ");
    }

    @Override
    public void onDisable() {
        OumLib.shutdown();
    }
}
```

---

## 3. Bootstrapping OumLib (Velocity Proxy)

Initialize OumLib inside the proxy plugin constructor or initialization event:

```java
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.oum.oumlib.OumLib;
import com.google.inject.Inject;

@Plugin(id = "my-proxy", name = "MyProxy", version = "1.0.0")
public final class VelocityProxyPlugin {
    private final ProxyServer server;

    @Inject
    public VelocityProxyPlugin(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        OumLib.init(server, this);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        OumLib.shutdown();
    }
}
```

---

## Platform Detection Utilities

To build multi-platform modules running across both Paper and Velocity, check the host platform at runtime:

```java
import dev.oum.oumlib.OumLib;

public class PlatformDetector {
    public void logPlatformInfo() {
        if (OumLib.isPaper()) {
            System.out.println("Executing on Paper/Folia Server platform");
        } else if (OumLib.isVelocity()) {
            System.out.println("Executing on Velocity Proxy platform");
        }
    }
}
```

These checks are safe to call on either platform and do not raise class loading errors.
