package dev.oum.oumlib.inventory;

import org.bukkit.event.inventory.ClickType;
import org.jspecify.annotations.NonNull;

public sealed interface ClickAction {

    record LeftClick() implements ClickAction {
    }

    record RightClick() implements ClickAction {
    }

    record ShiftLeftClick() implements ClickAction {
    }

    record ShiftRightClick() implements ClickAction {
    }

    record MiddleClick() implements ClickAction {
    }

    record Other(ClickType type) implements ClickAction {
    }

    static @NonNull ClickAction from(@NonNull ClickType type) {
        return switch (type) {
            case LEFT -> new LeftClick();
            case RIGHT -> new RightClick();
            case SHIFT_LEFT -> new ShiftLeftClick();
            case SHIFT_RIGHT -> new ShiftRightClick();
            case MIDDLE -> new MiddleClick();
            default -> new Other(type);
        };
    }
}