package dev.oum.oumlib.util;

import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.event.Events;
import org.jspecify.annotations.NonNull;

import java.util.List;

public final class Proxy {

    private Proxy() {
    }

    public static boolean connect(@NonNull Player player, @NonNull String serverName) {
        return OumLib.proxy().getServer(serverName)
                .map(server -> {
                    player.createConnectionRequest(server).fireAndForget();
                    return true;
                })
                .orElse(false);
    }

    public static void registerFallbackRouter(@NonNull Object plugin, @NonNull List<String> fallbackServerNames) {
        Events.listen(KickedFromServerEvent.class, event -> {
            String kickedFrom = event.getServer().getServerInfo().getName();

            for (String lobbyName : fallbackServerNames) {
                if (lobbyName.equalsIgnoreCase(kickedFrom)) {
                    continue;
                }

                var optionalServer = OumLib.proxy().getServer(lobbyName);
                if (optionalServer.isPresent()) {
                    event.setResult(KickedFromServerEvent.RedirectPlayer.create(
                            optionalServer.get(),
                            event.getServerKickReason().orElse(null)
                    ));
                    return;
                }
            }
        });
    }

    public static int getPlayerCount(@NonNull String serverName) {
        return OumLib.proxy().getServer(serverName)
                .map(server -> server.getPlayersConnected().size())
                .orElse(0);
    }

    public static int getPlayerCount(@NonNull List<String> serverNames) {
        int count = 0;
        for (String name : serverNames) {
            count += getPlayerCount(name);
        }
        return count;
    }

    public static boolean sendPluginMessage(@NonNull Player player, @NonNull String channelName, byte[] data) {
        return player.getCurrentServer()
                .map(serverConn -> {
                    MinecraftChannelIdentifier identifier = MinecraftChannelIdentifier.from(channelName);
                    OumLib.proxy().getChannelRegistrar().register(identifier);
                    return serverConn.sendPluginMessage(identifier, data);
                })
                .orElse(false);
    }
}
