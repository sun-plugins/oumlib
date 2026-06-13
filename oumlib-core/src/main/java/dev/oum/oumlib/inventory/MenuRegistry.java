package dev.oum.oumlib.inventory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class MenuRegistry {
    private static final Set<Menu> activeMenus = ConcurrentHashMap.newKeySet();

    private MenuRegistry() {
    }

    public static void register(Menu menu) {
        activeMenus.add(menu);
    }

    public static void unregister(Menu menu) {
        activeMenus.remove(menu);
    }

    public static void shutdown() {
        for (Menu menu : activeMenus) {
            menu.closeAll();
        }
        activeMenus.clear();
    }
}
