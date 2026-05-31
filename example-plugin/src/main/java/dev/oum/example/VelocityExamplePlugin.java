package dev.oum.example;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.event.Events;

@Plugin(
        id = "example-oum-plugin",
        name = "Example Oum Plugin",
        version = "1.0.0",
        description = "An example plugin using oumlib",
        authors = {"sun-dev"}
)
public final class VelocityExamplePlugin {

    private final ProxyServer server;

    @Inject
    public VelocityExamplePlugin(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        OumLib.init(server, this);
        ExampleAnnouncer.initialize();

        Events.listen(PostLoginEvent.class, e -> {
            ExampleAnnouncer.handlePlayerJoin(e.getPlayer().getUsername());
        });
    }
}
