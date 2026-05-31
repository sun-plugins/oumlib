package dev.oum.oumlib.inventory;

import org.bukkit.entity.Player;

public sealed interface Menu permits ChestMenu, PaginatedMenu, AnvilMenu {

    void open(Player player);

    void close(Player player);
}