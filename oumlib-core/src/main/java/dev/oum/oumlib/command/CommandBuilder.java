package dev.oum.oumlib.command;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.util.Cooldown;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class CommandBuilder {

    private final String label;
    private final List<Argument<?>> arguments = new ArrayList<>();
    private final List<SubcommandBuilder> subcommands = new ArrayList<>();
    private final List<String> aliases = new ArrayList<>();
    private String description = "";
    private String permission;
    private String cooldownMessage = "<red>Wait <remaining>s before using this again.";
    private Consumer<CommandContext> executor;
    private Cooldown cooldown;

    private CommandBuilder(String label) {
        this.label = label;
    }

    @Contract("_ -> new")
    static @NonNull CommandBuilder create(String label) {
        return new CommandBuilder(label);
    }

    public CommandBuilder description(String description) {
        this.description = description;
        return this;
    }

    public CommandBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    public CommandBuilder aliases(String... a) {
        aliases.addAll(List.of(a));
        return this;
    }

    public CommandBuilder cooldown(Duration duration) {
        this.cooldown = new Cooldown(duration);
        return this;
    }

    public CommandBuilder cooldownMessage(String message) {
        this.cooldownMessage = message;
        return this;
    }

    public CommandBuilder argument(Argument<?> argument) {
        arguments.add(argument);
        return this;
    }

    @Contract("_ -> this")
    public CommandBuilder subcommand(@NonNull Consumer<SubcommandBuilder> configurer) {
        SubcommandBuilder sub = new SubcommandBuilder();
        configurer.accept(sub);
        subcommands.add(sub);
        return this;
    }

    public CommandBuilder executes(Consumer<CommandContext> executor) {
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

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public String permission() {
        return permission;
    }

    public String cooldownMessage() {
        return cooldownMessage;
    }

    public List<Argument<?>> arguments() {
        return arguments;
    }

    public List<SubcommandBuilder> subcommands() {
        return subcommands;
    }

    public Consumer<CommandContext> executor() {
        return executor;
    }

    public List<String> aliases() {
        return aliases;
    }

    public Cooldown cooldown() {
        return cooldown;
    }
}