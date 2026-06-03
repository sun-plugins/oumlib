package dev.oum.oumlib.bridge.permission;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public final class PermissionBridge {

    private static Object luckPerms;

    static {
        try {
            Class<?> provider = Class.forName("net.luckperms.api.LuckPermsProvider");
            luckPerms = provider.getMethod("get").invoke(null);
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
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, uuid);
            if (user == null) return null;
            return (String) user.getClass().getMethod("getPrimaryGroup").invoke(user);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static @Nullable String getPrefix(@NonNull UUID uuid) {
        if (luckPerms == null) return null;
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, uuid);
            if (user == null) return null;
            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
            return (String) metaData.getClass().getMethod("getPrefix").invoke(metaData);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static @Nullable String getSuffix(@NonNull UUID uuid) {
        if (luckPerms == null) return null;
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, uuid);
            if (user == null) return null;
            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
            return (String) metaData.getClass().getMethod("getSuffix").invoke(metaData);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static @Nullable String getMetaValue(@NonNull UUID uuid, @NonNull String key) {
        if (luckPerms == null) return null;
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, uuid);
            if (user == null) return null;
            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
            return (String) metaData.getClass().getMethod("getMetaValue", String.class).invoke(metaData, key);
        } catch (Exception ignored) {
        }
        return null;
    }
}
