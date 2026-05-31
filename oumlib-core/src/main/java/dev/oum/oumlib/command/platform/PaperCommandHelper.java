package dev.oum.oumlib.command.platform;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.oum.oumlib.command.Argument;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.List;

public final class PaperCommandHelper {

    private PaperCommandHelper() {
    }

    @Contract("_ -> new")
    public static @NonNull Argument<Player> createPlayerArgument(String name) {
        return new Argument<>(name, ArgumentTypes.player(), (raw, ctx) -> {
            PlayerSelectorArgumentResolver resolver = (PlayerSelectorArgumentResolver) raw;
            List<Player> players;
            try {
                players = resolver.resolve((CommandSourceStack) ctx.getSource());
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
            return players.isEmpty() ? null : players.getFirst();
        });
    }
}
