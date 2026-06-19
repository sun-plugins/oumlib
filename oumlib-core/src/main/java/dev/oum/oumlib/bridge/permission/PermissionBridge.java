package dev.oum.oumlib.bridge.permission;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class PermissionBridge {

    private static Object luckPerms;
    private static Class<?> luckPermsClass;
    private static Class<?> userManagerClass;
    private static Class<?> userClass;
    private static Class<?> cachedDataClass;
    private static Class<?> cachedMetaDataClass;
    private static Class<?> nodeMapClass;
    private static Class<?> nodeTypeClass;
    private static Class<?> nodeClass;
    private static Class<?> nodeBuilderClass;
    private static Class<?> inheritanceNodeClass;

    static {
        try {
            Class<?> provider = Class.forName("net.luckperms.api.LuckPermsProvider");
            luckPerms = provider.getMethod("get").invoke(null);
            if (luckPerms != null) {
                luckPermsClass = Class.forName("net.luckperms.api.LuckPerms");
                userManagerClass = Class.forName("net.luckperms.api.model.user.UserManager");
                userClass = Class.forName("net.luckperms.api.model.user.User");
                cachedDataClass = Class.forName("net.luckperms.api.cacheddata.CachedData");
                cachedMetaDataClass = Class.forName("net.luckperms.api.cacheddata.CachedMetaData");
                nodeMapClass = Class.forName("net.luckperms.api.node.NodeMap");
                nodeTypeClass = Class.forName("net.luckperms.api.node.NodeType");
                nodeClass = Class.forName("net.luckperms.api.node.Node");
                nodeBuilderClass = Class.forName("net.luckperms.api.node.NodeBuilder");
                inheritanceNodeClass = Class.forName("net.luckperms.api.node.types.InheritanceNode");
            }
        } catch (Throwable ignored) {
        }
    }

    private PermissionBridge() {
    }

    public static boolean isAvailable() {
        return luckPerms != null;
    }

    public static @Nullable String getPrimaryGroup(@NonNull UUID uuid) {
        if (luckPerms == null) return null;
        try {
            Object userManager = luckPermsClass.getMethod("getUserManager").invoke(luckPerms);
            Object user = userManagerClass.getMethod("getUser", UUID.class).invoke(userManager, uuid);
            if (user == null) return null;
            return (String) userClass.getMethod("getPrimaryGroup").invoke(user);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static @Nullable String getPrefix(@NonNull UUID uuid) {
        if (luckPerms == null) return null;
        try {
            Object userManager = luckPermsClass.getMethod("getUserManager").invoke(luckPerms);
            Object user = userManagerClass.getMethod("getUser", UUID.class).invoke(userManager, uuid);
            if (user == null) return null;
            Object cachedData = userClass.getMethod("getCachedData").invoke(user);
            Object metaData = cachedDataClass.getMethod("getMetaData").invoke(cachedData);
            return (String) cachedMetaDataClass.getMethod("getPrefix").invoke(metaData);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static @Nullable String getSuffix(@NonNull UUID uuid) {
        if (luckPerms == null) return null;
        try {
            Object userManager = luckPermsClass.getMethod("getUserManager").invoke(luckPerms);
            Object user = userManagerClass.getMethod("getUser", UUID.class).invoke(userManager, uuid);
            if (user == null) return null;
            Object cachedData = userClass.getMethod("getCachedData").invoke(user);
            Object metaData = cachedDataClass.getMethod("getMetaData").invoke(cachedData);
            return (String) cachedMetaDataClass.getMethod("getSuffix").invoke(metaData);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static @Nullable String getMetaValue(@NonNull UUID uuid, @NonNull String key) {
        if (luckPerms == null) return null;
        try {
            Object userManager = luckPermsClass.getMethod("getUserManager").invoke(luckPerms);
            Object user = userManagerClass.getMethod("getUser", UUID.class).invoke(userManager, uuid);
            if (user == null) return null;
            Object cachedData = userClass.getMethod("getCachedData").invoke(user);
            Object metaData = cachedDataClass.getMethod("getMetaData").invoke(cachedData);
            return (String) cachedMetaDataClass.getMethod("getMetaValue", String.class).invoke(metaData, key);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static @Nullable List<String> getGroups(@NonNull UUID uuid) {
        if (luckPerms == null) return null;
        try {
            Object userManager = luckPermsClass.getMethod("getUserManager").invoke(luckPerms);
            Object user = userManagerClass.getMethod("getUser", UUID.class).invoke(userManager, uuid);
            if (user == null) return null;
            List<String> groups = new ArrayList<>();
            Collection<?> nodes = (Collection<?>) userClass.getMethod("getNodes").invoke(user);
            Method getGroupNameMethod = inheritanceNodeClass.getMethod("getGroupName");
            for (Object node : nodes) {
                if (inheritanceNodeClass.isInstance(node)) {
                    groups.add((String) getGroupNameMethod.invoke(node));
                }
            }
            return groups;
        } catch (Exception ignored) {
        }
        return null;
    }

    public static void setGroups(@NonNull UUID uuid, @NonNull List<String> groups, @Nullable String primaryGroup) {
        if (luckPerms == null) return;
        try {
            Object userManager = luckPermsClass.getMethod("getUserManager").invoke(luckPerms);
            CompletableFuture<?> future = (CompletableFuture<?>) userManagerClass
                    .getMethod("loadUser", UUID.class)
                    .invoke(userManager, uuid);

            future.thenAcceptAsync(user -> {
                try {
                    Object data = userClass.getMethod("data").invoke(user);
                    Object inheritanceType = nodeTypeClass.getField("INHERITANCE").get(null);

                    nodeMapClass.getMethod("clear", nodeTypeClass).invoke(data, inheritanceType);

                    Method builderMethod = inheritanceNodeClass.getMethod("builder", String.class);
                    Method addMethod = nodeMapClass.getMethod("add", nodeClass);
                    Method buildMethod = nodeBuilderClass.getMethod("build");

                    for (String group : groups) {
                        Object builder = builderMethod.invoke(null, group);
                        Object node = buildMethod.invoke(builder);
                        addMethod.invoke(data, node);
                    }

                    if (primaryGroup != null) {
                        userClass.getMethod("setPrimaryGroup", String.class).invoke(user, primaryGroup);
                    }

                    userManagerClass.getMethod("saveUser", userClass).invoke(userManager, user);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception ignored) {
        }
    }
}
