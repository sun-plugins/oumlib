package dev.oum.oumlib.plugin;

import dev.oum.oumlib.OumLib;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperOumLibPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        OumLib.init(this);
    }

    @Override
    public void onDisable() {
        OumLib.shutdown();
    }
}
