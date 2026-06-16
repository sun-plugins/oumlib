package dev.oum.oumlib.util;

import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.event.Events;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.jspecify.annotations.NonNull;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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

    /**
     * Registers a new backend server dynamically on the proxy.
     */
    public static void registerServer(@NonNull String name, @NonNull String ipAddress, int port) {
        OumLib.proxy().registerServer(new ServerInfo(name, new InetSocketAddress(ipAddress, port)));
    }

    /**
     * Unregisters a backend server dynamically by name.
     */
    public static boolean unregisterServer(@NonNull String name) {
        return OumLib.proxy().getServer(name)
                .map(server -> {
                    OumLib.proxy().unregisterServer(server.getServerInfo());
                    return true;
                })
                .orElse(false);
    }

    /**
     * Pings a server asynchronously to determine if it is online.
     */
    public static @NonNull CompletableFuture<Boolean> isOnline(@NonNull String serverName) {
        return OumLib.proxy().getServer(serverName)
                .map(server -> server.ping()
                        .thenApply(ping -> true)
                        .exceptionally(err -> false))
                .orElseGet(() -> CompletableFuture.completedFuture(false));
    }

    /**
     * Sends a MiniMessage-formatted message to all players on a specific backend server.
     */
    public static void broadcastTo(@NonNull String serverName, @NonNull String miniMessage, @NonNull TagResolver... resolvers) {
        OumLib.proxy().getServer(serverName).ifPresent(server -> {
            var component = MiniMessage.miniMessage().deserialize(miniMessage, resolvers);
            for (Player player : server.getPlayersConnected()) {
                player.sendMessage(component);
            }
        });
    }

    /**
     * Sends a MiniMessage-formatted title to all players on a specific backend server.
     */
    public static void sendTitleTo(@NonNull String serverName, @NonNull String title, @NonNull String subtitle, @NonNull TagResolver... resolvers) {
        OumLib.proxy().getServer(serverName).ifPresent(server -> {
            var titleComp = MiniMessage.miniMessage().deserialize(title, resolvers);
            var subtitleComp = MiniMessage.miniMessage().deserialize(subtitle, resolvers);
            var titleObject = Title.title(titleComp, subtitleComp);
            for (Player player : server.getPlayersConnected()) {
                player.showTitle(titleObject);
            }
        });
    }

    /**
     * Finds the least populated online server from the provided list.
     */
    public static @NonNull CompletableFuture<Optional<RegisteredServer>> getBestServer(@NonNull List<String> serverNames) {
        var futures = serverNames.stream()
                .map(name -> OumLib.proxy().getServer(name))
                .flatMap(Optional::stream)
                .map(server -> server.ping()
                        .thenApply(ping -> Map.entry(server, server.getPlayersConnected().size()))
                        .exceptionally(err -> null))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    RegisteredServer best = null;
                    int minPlayers = Integer.MAX_VALUE;

                    for (var future : futures) {
                        try {
                            var entry = future.getNow(null);
                            if (entry != null && entry.getValue() < minPlayers) {
                                minPlayers = entry.getValue();
                                best = entry.getKey();
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    return Optional.ofNullable(best);
                });
    }
}
