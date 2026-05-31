package dev.oum.oumlib.text;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jspecify.annotations.NonNull;

public final class TextBuilder {

    private final String raw;
    private ClickEvent clickEvent;
    private Component hoverText;

    TextBuilder(String raw) {
        this.raw = raw;
    }

    public TextBuilder clickRunCommand(String command) {
        this.clickEvent = ClickEvent.runCommand(command);
        return this;
    }

    public TextBuilder clickSuggestCommand(String command) {
        this.clickEvent = ClickEvent.suggestCommand(command);
        return this;
    }

    public TextBuilder hoverText(String text) {
        this.hoverText = MiniMessage.miniMessage().deserialize(text);
        return this;
    }

    public Component build() {
        Component c = MiniMessage.miniMessage().deserialize(raw);
        if (clickEvent != null) c = c.clickEvent(clickEvent);
        if (hoverText != null) c = c.hoverEvent(HoverEvent.showText(hoverText));
        return c;
    }

    public void send(@NonNull Audience audience) {
        audience.sendMessage(build());
    }
}