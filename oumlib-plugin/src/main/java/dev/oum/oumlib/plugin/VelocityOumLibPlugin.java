package dev.oum.oumlib.plugin;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.oum.oumlib.OumLib;

@Plugin(id = "oumlib", name = "OumLib", version = "1.0.7", description = "Provided plugin library for OumLib.")
public final class VelocityOumLibPlugin {

    private final ProxyServer server;

    @Inject
    public VelocityOumLibPlugin(ProxyServer server) {
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