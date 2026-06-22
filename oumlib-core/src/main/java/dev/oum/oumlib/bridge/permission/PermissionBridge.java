package dev.oum.oumlib.bridge.permission;

import dev.oum.oumlib.OumLib;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

interface PermissionHandler {
    String getPrimaryGroup(UUID uuid);

    String getPrefix(UUID uuid);

    String getSuffix(UUID uuid);

    String getMetaValue(UUID uuid, String key);

    List<String> getGroups(UUID uuid);

    void setGroups(UUID uuid, List<String> groups, String primaryGroup);
}

public final class PermissionBridge {

    private static PermissionHandler handler;
    private static boolean initialized = false;
    private static boolean hasProviderClass = false;

    static {
        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");
            hasProviderClass = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    private PermissionBridge() {
    }

    private static synchronized @Nullable PermissionHandler getHandler() {
        if (initialized) return handler;
        if (!hasProviderClass) {
            initialized = true;
            return null;
        }
        try {
            Class.forName("org.bukkit.Bukkit");
            if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
                handler = new LuckPermsHandler();
                initialized = true;
                return handler;
            }
        } catch (Throwable ignored) {
        }
        try {
            Class.forName("com.velocitypowered.api.proxy.ProxyServer");
            if (OumLib.proxy().getPluginManager().isLoaded("luckperms")) {
                handler = new LuckPermsHandler();
                initialized = true;
                return handler;
            }
        } catch (Throwable ignored) {
        }
        initialized = true;
        return handler;
    }

    public static boolean isAvailable() {
        return getHandler() != null;
    }

    public static @Nullable String getPrimaryGroup(@NonNull UUID uuid) {
        PermissionHandler h = getHandler();
        return h != null ? h.getPrimaryGroup(uuid) : null;
    }

    public static @Nullable String getPrefix(@NonNull UUID uuid) {
        PermissionHandler h = getHandler();
        return h != null ? h.getPrefix(uuid) : null;
    }

    public static @Nullable String getSuffix(@NonNull UUID uuid) {
        PermissionHandler h = getHandler();
        return h != null ? h.getSuffix(uuid) : null;
    }

    public static @Nullable String getMetaValue(@NonNull UUID uuid, @NonNull String key) {
        PermissionHandler h = getHandler();
        return h != null ? h.getMetaValue(uuid, key) : null;
    }

    public static @Nullable List<String> getGroups(@NonNull UUID uuid) {
        PermissionHandler h = getHandler();
        return h != null ? h.getGroups(uuid) : null;
    }

    public static void setGroups(@NonNull UUID uuid, @NonNull List<String> groups, @Nullable String primaryGroup) {
        PermissionHandler h = getHandler();
        if (h != null) {
            h.setGroups(uuid, groups, primaryGroup);
        }
    }
}

class LuckPermsHandler implements PermissionHandler {
    private final LuckPerms api = LuckPermsProvider.get();

    @Override
    public String getPrimaryGroup(UUID uuid) {
        User user = api.getUserManager().getUser(uuid);
        return user != null ? user.getPrimaryGroup() : null;
    }

    @Override
    public String getPrefix(UUID uuid) {
        User user = api.getUserManager().getUser(uuid);
        if (user == null) return null;
        return user.getCachedData().getMetaData().getPrefix();
    }

    @Override
    public String getSuffix(UUID uuid) {
        User user = api.getUserManager().getUser(uuid);
        if (user == null) return null;
        return user.getCachedData().getMetaData().getSuffix();
    }

    @Override
    public String getMetaValue(UUID uuid, String key) {
        User user = api.getUserManager().getUser(uuid);
        if (user == null) return null;
        return user.getCachedData().getMetaData().getMetaValue(key);
    }

    @Override
    public List<String> getGroups(UUID uuid) {
        User user = api.getUserManager().getUser(uuid);
        if (user == null) return null;
        List<String> groups = new ArrayList<>();
        for (Node node : user.getNodes()) {
            if (node instanceof InheritanceNode inheritanceNode) {
                groups.add(inheritanceNode.getGroupName());
            }
        }
        return groups;
    }

    @Override
    public void setGroups(UUID uuid, List<String> groups, String primaryGroup) {
        api.getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
            if (user == null) return;
            user.data().clear(NodeType.INHERITANCE::matches);
            for (String group : groups) {
                user.data().add(InheritanceNode.builder(group).build());
            }
            if (primaryGroup != null) {
                user.setPrimaryGroup(primaryGroup);
            }
            api.getUserManager().saveUser(user);
        });
    }
}
