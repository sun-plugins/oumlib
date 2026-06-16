package dev.oum.oumlib.command.platform;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.oum.oumlib.command.Argument;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.FinePosition;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import org.bukkit.entity.Entity;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
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

    @Contract("_ -> new")
    public static @NonNull Argument<Location> createFinePositionArgument(String name) {
        return new Argument<>(name, ArgumentTypes.finePosition(), (raw, ctx) -> {
            FinePositionResolver resolver = (FinePositionResolver) raw;
            try {
                FinePosition pos = resolver.resolve((CommandSourceStack) ctx.getSource());
                return pos.toLocation(((CommandSourceStack) ctx.getSource()).getLocation().getWorld());
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Contract("_ -> new")
    public static @NonNull Argument<Location> createBlockPositionArgument(String name) {
        return new Argument<>(name, ArgumentTypes.blockPosition(), (raw, ctx) -> {
            BlockPositionResolver resolver = (BlockPositionResolver) raw;
            try {
                BlockPosition pos = resolver.resolve((CommandSourceStack) ctx.getSource());
                return pos.toLocation(((CommandSourceStack) ctx.getSource()).getLocation().getWorld());
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Contract("_ -> new")
    public static @NonNull Argument<List<Player>> createPlayersArgument(String name) {
        return new Argument<>(name, ArgumentTypes.players(), (raw, ctx) -> {
            PlayerSelectorArgumentResolver resolver = (PlayerSelectorArgumentResolver) raw;
            try {
                return resolver.resolve((CommandSourceStack) ctx.getSource());
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Contract("_ -> new")
    public static @NonNull Argument<World> createWorldArgument(String name) {
        return new Argument<>(name, ArgumentTypes.world(), (raw, ctx) -> (World) raw);
    }

    @Contract("_ -> new")
    public static @NonNull Argument<NamespacedKey> createKeyArgument(String name) {
        return new Argument<>(name, ArgumentTypes.key(), (raw, ctx) -> (NamespacedKey) raw);
    }

    @Contract("_ -> new")
    public static @NonNull Argument<Entity> createEntityArgument(String name) {
        return new Argument<>(name, ArgumentTypes.entity(), (raw, ctx) -> {
            EntitySelectorArgumentResolver resolver = (EntitySelectorArgumentResolver) raw;
            try {
                List<Entity> entities = resolver.resolve((CommandSourceStack) ctx.getSource());
                return entities.isEmpty() ? null : entities.getFirst();
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Contract("_ -> new")
    public static @NonNull Argument<List<Entity>> createEntitiesArgument(String name) {
        return new Argument<>(name, ArgumentTypes.entities(), (raw, ctx) -> {
            EntitySelectorArgumentResolver resolver = (EntitySelectorArgumentResolver) raw;
            try {
                return resolver.resolve((CommandSourceStack) ctx.getSource());
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
