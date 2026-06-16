package dev.oum.oumlib.command;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.util.Cooldown;
import dev.oum.oumlib.util.Permission;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class CommandBuilder {

    private final String label;
    private final List<Argument<?>> arguments = new ArrayList<>();
    private final List<SubcommandBuilder> subcommands = new ArrayList<>();
    private final List<String> aliases = new ArrayList<>();
    private String description = "";
    private String permission;
    private Permission permissionObject;
    private String cooldownMessage = "<red>Wait <remaining>s before using this again.";
    private Consumer<CommandContext> executor;
    private Cooldown cooldown;
    private Predicate<CommandContext> cooldownBypass;
    private BiConsumer<CommandContext, Throwable> exceptionHandler;

    private CommandBuilder(String label) {
        this.label = label;
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull CommandBuilder create(@NonNull String label) {
        return new CommandBuilder(label);
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull CommandBuilder literal(@NonNull String label) {
        return new CommandBuilder(label);
    }

    @CheckReturnValue
    public @NonNull CommandBuilder description(@NonNull String description) {
        this.description = description;
        return this;
    }

    @CheckReturnValue
    @Deprecated(since = "1.0.5", forRemoval = false)
    public @NonNull CommandBuilder permission(@NonNull String permission) {
        this.permission = permission;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    @CheckReturnValue
    public @NonNull CommandBuilder permission(@NonNull Permission permission) {
        this.permissionObject = permission;
        this.permission = permission.name();
        return this;
    }

    @CheckReturnValue
    public @NonNull CommandBuilder aliases(String @NonNull ... a) {
        aliases.addAll(List.of(a));
        return this;
    }

    @CheckReturnValue
    public @NonNull CommandBuilder cooldown(@NonNull Duration duration) {
        this.cooldown = new Cooldown(duration);
        return this;
    }

    @CheckReturnValue
    public @NonNull CommandBuilder cooldownMessage(@NonNull String message) {
        this.cooldownMessage = message;
        return this;
    }

    @CheckReturnValue
    public @NonNull CommandBuilder argument(@NonNull Argument<?> argument) {
        arguments.add(argument);
        return this;
    }

    @Contract("_ -> this")
    @CheckReturnValue
    public @NonNull CommandBuilder subcommand(@NonNull Consumer<@NonNull SubcommandBuilder> configurer) {
        SubcommandBuilder sub = new SubcommandBuilder();
        configurer.accept(sub);
        subcommands.add(sub);
        return this;
    }

    @CheckReturnValue
    public @NonNull CommandBuilder executes(@NonNull Consumer<@NonNull CommandContext> executor) {
        this.executor = executor;
        return this;
    }

    public void register() {
        try {
            CommandRegistrar registrar;
            if (OumLib.plugin() != null) {
                registrar = (CommandRegistrar) Class.forName("dev.oum.oumlib.command.platform.PaperCommandRegistrar")
                        .getDeclaredConstructor().newInstance();
            } else {
                registrar = (CommandRegistrar) Class.forName("dev.oum.oumlib.command.platform.VelocityCommandRegistrar")
                        .getDeclaredConstructor().newInstance();
            }
            registrar.register(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register command: " + label, e);
        }
    }

    public @NonNull String label() {
        return label;
    }

    public @NonNull String description() {
        return description;
    }

    public @Nullable String permission() {
        return permission;
    }

    public @Nullable Permission permissionObject() {
        return permissionObject;
    }

    public @NonNull String cooldownMessage() {
        return cooldownMessage;
    }

    public @NonNull List<@NonNull Argument<?>> arguments() {
        return arguments;
    }

    public @NonNull List<@NonNull SubcommandBuilder> subcommands() {
        return subcommands;
    }

    public @Nullable Consumer<@NonNull CommandContext> executor() {
        return executor;
    }

    public @NonNull List<@NonNull String> aliases() {
        return aliases;
    }

    @CheckReturnValue
    public @NonNull CommandBuilder cooldownBypass(@NonNull Predicate<@NonNull CommandContext> bypassPredicate) {
        this.cooldownBypass = bypassPredicate;
        return this;
    }

    public @Nullable Predicate<@NonNull CommandContext> cooldownBypass() {
        return cooldownBypass;
    }

    public @Nullable Cooldown cooldown() {
        return cooldown;
    }

    @CheckReturnValue
    public @NonNull CommandBuilder onException(@NonNull BiConsumer<CommandContext, Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    public @Nullable BiConsumer<CommandContext, Throwable> exceptionHandler() {
        return exceptionHandler;
    }
}