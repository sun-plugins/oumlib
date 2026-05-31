package dev.oum.oumlib.util;

import com.velocitypowered.api.proxy.Player;
import dev.oum.oumlib.OumLib;
import org.jspecify.annotations.NonNull;

public final class Proxy {

    private Proxy() {}

    /**
     * Safely connects a Velocity player to a server by its registered name.
     * Returns true if the connection attempt was successfully initiated.
     */
    public static boolean connect(@NonNull Player player, @NonNull String serverName) {
        return OumLib.proxy().getServer(serverName)
                .map(server -> {
                    player.createConnectionRequest(server).fireAndForget();
                    return true;
                })
                .orElse(false);
    }
}
