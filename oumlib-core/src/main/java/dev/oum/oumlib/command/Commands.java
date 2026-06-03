package dev.oum.oumlib.command;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public final class Commands {

    private Commands() {
    }

    @Contract("_ -> new")
    public static @NonNull CommandBuilder create(String label) {
        return CommandBuilder.create(label);
    }

    @Contract("_ -> new")
    public static @NonNull CommandBuilder literal(String label) {
        return CommandBuilder.literal(label);
    }
}