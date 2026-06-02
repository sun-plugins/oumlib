package dev.oum.oumlib.util;

import com.velocitypowered.api.permission.PermissionSubject;
import dev.oum.oumlib.OumLib;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class Permission {

    private final String name;
    private final String description;
    private final Default defaultValue;
    private Permission(@NonNull Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.defaultValue = builder.defaultValue;
        registerOnPaper();
    }

    public static @NonNull Builder builder(@NonNull String name) {
        return new Builder(name);
    }

    public @NonNull String name() {
        return name;
    }

    public @Nullable String description() {
        return description;
    }

    public @NonNull Default defaultValue() {
        return defaultValue;
    }

    /**
     * Checks if the given audience has this permission.
     */
    public boolean has(@NonNull Audience audience) {
        if (audience instanceof Permissible permissible) {
            return permissible.hasPermission(name);
        }

        if (audience instanceof PermissionSubject subject) {
            return subject.hasPermission(name);
        }

        return false;
    }

    private void registerOnPaper() {
        try {
            Class.forName("org.bukkit.permissions.Permission");
            org.bukkit.permissions.Permission bukkitPerm = new org.bukkit.permissions.Permission(
                    name,
                    description,
                    toBukkitDefault(defaultValue)
            );

            PluginManager pm = Bukkit.getPluginManager();
            if (pm.getPermission(name) == null) {
                pm.addPermission(bukkitPerm);
            }
        } catch (ClassNotFoundException ignored) {

        } catch (Exception e) {
            OumLib.logError("Failed to register Bukkit permission: " + name, e);
        }
    }

    @Contract(pure = true)
    private PermissionDefault toBukkitDefault(@NonNull Default def) {
        return switch (def) {
            case TRUE -> PermissionDefault.TRUE;
            case FALSE -> PermissionDefault.FALSE;
            case OP -> PermissionDefault.OP;
            case NOT_OP -> PermissionDefault.NOT_OP;
        };
    }

    public enum Default {
        TRUE, FALSE, OP, NOT_OP
    }

    public static final class Builder {
        private final String name;
        private String description = "";
        private Default defaultValue = Default.OP;

        private Builder(String name) {
            this.name = name;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(Default defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        @Contract(" -> new")
        public @NonNull Permission build() {
            return new Permission(this);
        }
    }
}
