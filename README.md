# oumlib

[![](https://jitpack.io/v/sun-mc-dev/oumlib.svg)](https://jitpack.io/#sun-mc-dev/oumlib)

A lightweight, multi-platform library for Paper (Minecraft servers) and Velocity (proxies) built for Java 25. It helps developers write clean, boilerplate-free code.

## Requirements & Compatibility

- **Java**: Java 25 or higher (utilizes Virtual Threads).
- **Paper**: Version 1.21.4 or newer.
- **Velocity**: Version 3.5.x or newer.
- **Folia**: Fully compatible. Tasks run on safe async scheduler structures, and it supports Folia's multi-threaded tick environment.

---

## Installation

Add the JitPack repository and the library dependency to your `pom.xml`.

### 1. Add Repository
```xml
<repository>
   <id>jitpack.io</id>
   <url>https://jitpack.io</url>
</repository>
```

### 2. Add Dependency
If you are shading the library into your plugin, declare the compile-scope dependency below.
Look for "VERSION" at our github releases page: [Releases](https://github.com/sun-mc-dev/oumlib/releases)

```xml
<dependency>
    <groupId>com.github.sun-mc-dev.oumlib</groupId>
    <artifactId>oumlib-core</artifactId>
    <version>VERSION</version>
    <scope>compile</scope>
</dependency>
```

### 3. Shade and Relocate the Dependency
To prevent classpath conflicts with other plugins running different versions of OumLib on the same server, you must **relocate** the library packages into your own plugin's package space.

Also, exclude dependency security signatures to avoid runtime validation errors.

Add this `maven-shade-plugin` configuration to your plugin's `pom.xml`:

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
                    <!-- Relocate oumlib into your own plugin package -->
                    <relocation>
                        <pattern>dev.oum.oumlib</pattern>
                        <shadedPattern>your.plugin.package.libs.oumlib</shadedPattern>
                    </relocation>
                </relocations>
                <filters>
                    <!-- Exclude security signature files that break shaded JARs -->
                    <filter>
                        <artifact>*:*</artifact>
                        <excludes>
                            <exclude>META-INF/*.SF</exclude>
                            <exclude>META-INF/*.DSA</exclude>
                            <exclude>META-INF/*.RSA</exclude>
                            <exclude>META-INF/MANIFEST.MF</exclude>
                        </excludes>
                    </filter>
                </filters>
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

## Quick Start Example

Here is a short preview showing how clean it is to set up a configuration, a command, and a chest GUI on Paper:

```java
// 1. Declare config as a Java Record
public record MyConfig(
    @Comment("Default broadcast message") String broadcastMessage
) implements ConfigSection {}

// 2. Load the configuration
ConfigManager<MyConfig> config = ConfigManager.of(MyConfig.class, "config.yml", 
    () -> new MyConfig("<yellow>Default broadcast message!</yellow>")
).enableAutoReload();

// 3. Register a command to open a GUI
Commands.literal("admin")
    .permission("myplugin.admin")
    .subcommand(sub -> sub
        .label("menu")
        .executes(context -> {
            if (!context.isPlayer()) return;
            
            // Build and open a GUI
            ChestMenu.builder()
                .title("<gold>Dashboard</gold>")
                .rows(3)
                .pattern(
                    "#########",
                    "#   B   #",
                    "#########"
                )
                .bind('B', ItemBuilder.of(Material.BOOK).name("<yellow>Broadcast</yellow>").build())
                .onClick('B', click -> {
                    // Send message and close menu
                    Text.send(click.player(), config.get().broadcastMessage());
                    click.player().closeInventory();
                })
                .build()
                .open(context.playerOrThrow());
        })
    ).register();
```

---

## Local Compilation & Fallback

If you want to build the library yourself, or if you prefer to install it locally instead of using JitPack:

1. Clone this repository.
2. Open a terminal in the root directory.
3. Run the Maven installation command:
   ```bash
   mvn clean install
   ```
4. You can now reference the library in your local projects without declaring the JitPack repository:
   ```xml
   <dependency>
       <groupId>dev.oum</groupId>
       <artifactId>oumlib-core</artifactId>
       <version>VERSION</version>
       <scope>compile</scope>
   </dependency>
   ```

---

## Features & Documentation

Detailed guides for each package:

- **[Main Setup & Initialization](docs/setup.md)**: How to initialize OumLib on Paper and Velocity.
- **[Commands](docs/commands.md)**: A simple Brigadier-based command system with cooldowns.
- **[Configurations](docs/configuration.md)**: Auto-reloading record-based configurations.
- **[Database](docs/database.md)**: Non-blocking asynchronous SQLite and MySQL database helpers.
- **[Events](docs/events.md)**: Dynamic, fluent event listeners for Paper and Velocity.
- **[GUI & Chest Menus](docs/inventories.md)**: Easy chest menus with layouts and click actions.
- **[Scheduler](docs/scheduler.md)**: Platform-agnostic scheduler for delayed and repeating tasks.
- **[Text & Placeholders](docs/text.md)**: MiniMessage text presets and custom placeholders.
- **[Plugin Bridges](docs/bridges.md)**: Economy, Custom Items, Nexo, and LuckPerms Permission Bridges.
- **[General Utilities](docs/utilities.md)**: PDC wrappers, duration formatting, and location serialization.
