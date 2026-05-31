package dev.oum.example;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.event.Events;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperExamplePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        OumLib.init(this);
        ExampleAnnouncer.initialize();

        Events.listen(PlayerJoinEvent.class, event -> {
            event.joinMessage(null);
            ExampleAnnouncer.handlePlayerJoin(event.getPlayer().getName());
        });
    }

    @Override
    public void onDisable() {
        OumLib.shutdown();
    }
}
