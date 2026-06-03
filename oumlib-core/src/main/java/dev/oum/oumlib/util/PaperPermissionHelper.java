package dev.oum.oumlib.util;

import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jspecify.annotations.NonNull;

public final class PaperPermissionHelper {

    private PaperPermissionHelper() {
    }

    public static void register(String name, String description, Permission.@NonNull Default defaultValue) {
        PermissionDefault defaultVal = switch (defaultValue) {
            case TRUE -> PermissionDefault.TRUE;
            case FALSE -> PermissionDefault.FALSE;
            case OP -> PermissionDefault.OP;
            case NOT_OP -> PermissionDefault.NOT_OP;
        };

        org.bukkit.permissions.Permission bukkitPerm = new org.bukkit.permissions.Permission(
                name,
                description,
                defaultVal
        );

        PluginManager pm = Bukkit.getPluginManager();
        if (pm.getPermission(name) == null) {
            pm.addPermission(bukkitPerm);
        }
    }
}
