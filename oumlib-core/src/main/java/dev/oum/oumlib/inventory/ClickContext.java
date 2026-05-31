package dev.oum.oumlib.inventory;

import org.bukkit.entity.Player;

public record ClickContext(Player player, ClickAction action, int slot) {
}