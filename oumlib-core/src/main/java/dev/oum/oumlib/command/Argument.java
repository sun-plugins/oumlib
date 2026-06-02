package dev.oum.oumlib.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import java.util.function.BiFunction;

public final class Argument<T> {

    private final String name;
    private final ArgumentType<?> brigadierType;
    private final BiFunction<Object, CommandContext<?>, T> extractor;
    private SuggestionProvider<?> suggestionProvider;

    public Argument(String name, ArgumentType<?> brigadierType,
                    BiFunction<Object, CommandContext<?>, T> extractor) {
        this.name = name;
        this.brigadierType = brigadierType;
        this.extractor = extractor;
    }

    public <S> Argument<T> suggests(SuggestionProvider<S> provider) {
        this.suggestionProvider = provider;
        return this;
    }

    public String name() {
        return name;
    }

    public ArgumentType<?> brigadierType() {
        return brigadierType;
    }

    public BiFunction<Object, CommandContext<?>, T> extractor() {
        return extractor;
    }

    @SuppressWarnings("unchecked")
    public <S> SuggestionProvider<S> suggestionProvider() {
        return (SuggestionProvider<S>) suggestionProvider;
    }
}