package dev.oum.oumlib.command;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record RichSuggestion(
        @NonNull String value,
        @Nullable String tooltip
) {
    public static @NonNull RichSuggestion of(@NonNull String value, @Nullable String tooltip) {
        return new RichSuggestion(value, tooltip);
    }
}
