package dev.oum.oumlib.command;

import dev.oum.oumlib.util.Permission;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class SubcommandBuilder {

    private final List<Argument<?>> arguments = new ArrayList<>();
    private String label;
    private String permission;
    private Permission permissionObject;
    private Consumer<CommandContext> executor;

    public SubcommandBuilder label(String label) {
        this.label = label;
        return this;
    }

    public SubcommandBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    public SubcommandBuilder permission(Permission permission) {
        this.permissionObject = permission;
        this.permission = permission.name();
        return this;
    }

    public SubcommandBuilder argument(Argument<?> argument) {
        arguments.add(argument);
        return this;
    }

    public SubcommandBuilder executes(Consumer<CommandContext> executor) {
        this.executor = executor;
        return this;
    }

    public String label() {
        return label;
    }

    public String permission() {
        return permission;
    }

    public Permission permissionObject() {
        return permissionObject;
    }

    @Contract(pure = true)
    @NonNull
    @Unmodifiable
    public List<Argument<?>> arguments() {
        return List.copyOf(arguments);
    }

    public Consumer<CommandContext> executor() {
        return executor;
    }
}