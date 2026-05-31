package dev.oum.oumlib;

import com.velocitypowered.api.proxy.ProxyServer;
import dev.oum.oumlib.event.EventBus;
import dev.oum.oumlib.event.platform.PaperEventBus;
import dev.oum.oumlib.event.platform.VelocityEventBus;
import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.oumlib.scheduler.platform.BukkitSchedulerAdapter;
import dev.oum.oumlib.scheduler.platform.VelocitySchedulerAdapter;
import dev.oum.oumlib.text.Preset;
import dev.oum.oumlib.text.PresetRegistry;
import dev.oum.oumlib.text.placeholder.PlaceholderRegistry;
import dev.oum.oumlib.text.placeholder.bridge.MiniPlaceholdersHelper;
import dev.oum.oumlib.text.placeholder.bridge.PapiHelper;
import net.kyori.adventure.audience.Audience;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class OumLib {

    private static Plugin plugin;
    private static ProxyServer proxyServer;
    private static Object velocityPlugin;
    
    private static PresetRegistry presetRegistry;
    private static PlaceholderRegistry placeholderRegistry;
    private static boolean initialized;

    private OumLib() {
    }

    @Contract("_ -> new")
    public static @NonNull InitBuilder init(Plugin p) {
        if (initialized) throw new IllegalStateException("OumLib already initialized.");
        plugin = p;
        presetRegistry = new PresetRegistry();
        placeholderRegistry = new PlaceholderRegistry();
        
        PaperEventBus.initialize(p);
        EventBus.initialize(PaperEventBus.get());
        
        BukkitSchedulerAdapter.initialize(p);
        Scheduler.initialize(BukkitSchedulerAdapter.get());
        
        detectIntegrations(p);
        initialized = true;
        return new InitBuilder();
    }

    @Contract("_, _ -> new")
    public static @NonNull InitBuilder init(ProxyServer server, Object pluginInstance) {
        if (initialized) throw new IllegalStateException("OumLib already initialized.");
        proxyServer = server;
        velocityPlugin = pluginInstance;
        presetRegistry = new PresetRegistry();
        placeholderRegistry = new PlaceholderRegistry();
        
        VelocityEventBus.initialize(server, pluginInstance);
        EventBus.initialize(VelocityEventBus.get());
        
        VelocitySchedulerAdapter.initialize(server, pluginInstance);
        Scheduler.initialize(VelocitySchedulerAdapter.get());
        
        detectVelocityIntegrations(server);
        initialized = true;
        return new InitBuilder();
    }

    private static void detectIntegrations(@NonNull Plugin p) {
        if (p.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            PapiHelper.register(p, placeholderRegistry);
        }
        if (p.getServer().getPluginManager().isPluginEnabled("MiniPlaceholders")) {
            MiniPlaceholdersHelper.register(placeholderRegistry);
        }
    }

    private static void detectVelocityIntegrations(@NonNull ProxyServer server) {
        if (server.getPluginManager().isLoaded("miniplaceholders")) {
            MiniPlaceholdersHelper.register(placeholderRegistry);
        }
    }

    public static void shutdown() {
        Scheduler.shutdownAll();
    }

    public static Plugin plugin() {
        assertInit();
        if (plugin == null) throw new IllegalStateException("Plugin is only available on Paper platform.");
        return plugin;
    }

    public static ProxyServer proxy() {
        assertInit();
        if (proxyServer == null) throw new IllegalStateException("ProxyServer is only available on Velocity platform.");
        return proxyServer;
    }

    public static Object velocityPlugin() {
        assertInit();
        if (velocityPlugin == null) throw new IllegalStateException("Velocity plugin instance is only available on Velocity platform.");
        return velocityPlugin;
    }

    public static @NonNull File getDataFolder() {
        assertInit();
        if (plugin != null) {
            return plugin.getDataFolder();
        } else {
            String id = proxyServer.getPluginManager().fromInstance(velocityPlugin)
                    .map(c -> c.getDescription().getId())
                    .orElse("oumlib-plugin");
            return new File("plugins", id);
        }
    }

    public static void logError(String message) {
        if (plugin != null) {
            plugin.getSLF4JLogger().error(message);
        } else {
            Logger.getLogger("OumLib").log(Level.SEVERE, message);
        }
    }

    public static void logError(String message, Throwable t) {
        if (plugin != null) {
            plugin.getSLF4JLogger().error(message, t);
        } else {
            Logger.getLogger("OumLib").log(Level.SEVERE, message, t);
        }
    }

    public static PresetRegistry presets() {
        assertInit();
        return presetRegistry;
    }

    public static PlaceholderRegistry placeholders(String namespace) {
        assertInit();
        return placeholderRegistry.forNamespace(namespace);
    }

    public static PlaceholderRegistry globalRegistry() {
        return placeholderRegistry;
    }

    public static @NonNull Audience players() {
        assertInit();
        if (plugin != null) {
            return Audience.audience(plugin.getServer().getOnlinePlayers());
        } else {
            return Audience.audience(proxyServer.getAllPlayers());
        }
    }

    public static @NonNull Audience console() {
        assertInit();
        if (plugin != null) {
            return plugin.getServer().getConsoleSender();
        } else {
            return proxyServer.getConsoleCommandSource();
        }
    }

    private static void assertInit() {
        if (!initialized) throw new IllegalStateException("Call OumLib.init(plugin) first.");
    }

    public static final class InitBuilder {
        public InitBuilder preset(Preset type, String prefix) {
            presetRegistry.register(type, prefix);
            return this;
        }
    }
}