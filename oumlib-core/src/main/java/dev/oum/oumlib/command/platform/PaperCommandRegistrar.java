package dev.oum.oumlib.command.platform;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.command.*;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

public final class PaperCommandRegistrar implements CommandRegistrar {

    @Override
    public void register(CommandBuilder builder) {
        OumLib.plugin().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            var registrar = event.registrar();
            registrar.register(buildNode(builder).build(), builder.description(), List.copyOf(builder.aliases()));
        });
    }

    private @NonNull LiteralArgumentBuilder<CommandSourceStack> buildNode(@NonNull CommandBuilder builder) {
        var root = Commands.literal(builder.label());

        if (builder.permission() != null) {
            String perm = builder.permission();
            root.requires(source -> source.getSender().hasPermission(perm));
        }

        for (SubcommandBuilder sub : builder.subcommands()) {
            var subLiteral = Commands.literal(sub.label());
            if (sub.permission() != null) {
                String subPerm = sub.permission();
                subLiteral.requires(source -> source.getSender().hasPermission(subPerm));
            }
            attachArguments(subLiteral, sub.arguments(), sub.executor(), builder);
            root.then(subLiteral);
        }

        if (builder.executor() != null) {
            attachArguments(root, builder.arguments(), builder.executor(), builder);
        }

        return root;
    }

    private void attachArguments(
            LiteralArgumentBuilder<CommandSourceStack> node,
            @NonNull List<Argument<?>> args,
            Consumer<CommandContext> exec,
            CommandBuilder builder
    ) {
        if (args.isEmpty()) {
            node.executes(ctx -> {
                handleExecution(ctx.getSource(), new ArgumentMap(ctx), exec, builder);
                return 1;
            });
            return;
        }

        RequiredArgumentBuilder<CommandSourceStack, ?> first = buildArgChain(args, exec, builder);
        node.then(first);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private RequiredArgumentBuilder<CommandSourceStack, ?> buildArgChain(
            @NonNull List<Argument<?>> args,
            Consumer<CommandContext> exec,
            CommandBuilder builder
    ) {
        RequiredArgumentBuilder head = null;
        RequiredArgumentBuilder prev = null;

        for (int i = 0; i < args.size(); i++) {
            Argument<?> arg = args.get(i);
            RequiredArgumentBuilder current = Commands.argument(
                    arg.name(), arg.brigadierType()
            );
            if (arg.suggestionProvider() != null) {
                current.suggests(arg.suggestionProvider());
            }
            if (i == args.size() - 1) {
                current.executes(ctx -> {
                    handleExecution((CommandSourceStack) ctx.getSource(), new ArgumentMap(ctx), exec, builder);
                    return 1;
                });
            }
            if (head == null) {
                head = current;
            } else {
                prev.then(current);
            }
            prev = current;
        }

        return head;
    }

    private void handleExecution(
            @NonNull CommandSourceStack source,
            ArgumentMap map,
            Consumer<CommandContext> exec,
            @NonNull CommandBuilder builder
    ) {
        var sender = source.getSender();
        if (builder.cooldown() != null && sender instanceof Player player) {
            String bypassPerm = (builder.permission() != null ? builder.permission() : builder.label()) + ".bypass";
            if (!player.hasPermission(bypassPerm) && builder.cooldown().isOnCooldown(player.getUniqueId())) {
                long remaining = builder.cooldown().remainingSeconds(player.getUniqueId());
                player.sendMessage(MiniMessage.miniMessage()
                        .deserialize(builder.cooldownMessage().replace("<remaining>", String.valueOf(remaining))));
                return;
            }
            builder.cooldown().set(player.getUniqueId());
        }
        exec.accept(new CommandContext(source, sender, builder.label(), map));
    }
}
