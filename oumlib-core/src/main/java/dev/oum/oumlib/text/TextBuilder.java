package dev.oum.oumlib.text;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public final class TextBuilder {

    private final String raw;
    private ClickEvent clickEvent;
    private Component hoverText;

    TextBuilder(String raw) {
        this.raw = raw;
    }

    @CheckReturnValue
    public @NonNull TextBuilder clickRunCommand(@NonNull String command) {
        this.clickEvent = ClickEvent.runCommand(command);
        return this;
    }

    @CheckReturnValue
    public @NonNull TextBuilder clickSuggestCommand(@NonNull String command) {
        this.clickEvent = ClickEvent.suggestCommand(command);
        return this;
    }

    @CheckReturnValue
    public @NonNull TextBuilder hoverText(@NonNull String text) {
        this.hoverText = MiniMessage.miniMessage().deserialize(text);
        return this;
    }

    @Contract(" -> new")
    public @NonNull Component build() {
        Component c = MiniMessage.miniMessage().deserialize(raw);
        if (clickEvent != null) c = c.clickEvent(clickEvent);
        if (hoverText != null) c = c.hoverEvent(HoverEvent.showText(hoverText));
        return c;
    }

    public void send(@NonNull Audience audience) {
        audience.sendMessage(build());
    }
}