# GUI & Chest Menus

OumLib includes a simple, lightweight inventory menu system for Paper/Bukkit. It uses layout patterns, item bindings, and event handlers to build custom menus without listener boilerplate.

---

## 1. Defining Layouts with Dynamic Bindings

The `Layout` system maps text patterns to inventory slots. You can bind specific characters to static items or **dynamic item suppliers** (which execute every time the menu is opened or refreshed).

```java
import dev.oum.oumlib.inventory.ChestMenu;
import dev.oum.oumlib.inventory.Layout;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class SettingsMenu {

    private static boolean settingEnabled = true;

    public static ChestMenu build() {
        return ChestMenu.builder()
            .title("<dark_gray>System Options</dark_gray>")
            .rows(3)
            .pattern(
                "#########",
                "#   T   #",
                "#########"
            )
            // Bind a static border item
            .bind('#', new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
            // Bind a dynamic supplier for 'T' (Toggle)
            .bind('T', () -> {
                Material mat = settingEnabled ? Material.LIME_WOOL : Material.RED_WOOL;
                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.displayName(MiniMessage.miniMessage().deserialize(
                        settingEnabled ? "<green>Setting Enabled</green>" : "<red>Setting Disabled</red>"
                    ));
                    item.setItemMeta(meta);
                }
                return item;
            })
            // Handle clicks on key 'T'
            .onClick('T', context -> {
                // Toggle state
                settingEnabled = !settingEnabled;
                context.player().sendMessage("Toggled setting!");
                
                // Refresh the menu for the player to update the wool color
                context.menu().refresh(context.player());
            })
            .build();
    }
}
```

---

## 2. The ClickContext Object

When a slot is clicked, the register handler receives a `ClickContext` containing:
- **`context.player()`**: The player who clicked.
- **`context.slot()`**: The slot index clicked.
- **`context.action()`**: The click action type (translates Bukkit's click actions into basic types: `LEFT`, `RIGHT`, `SHIFT_LEFT`, `SHIFT_RIGHT`, `MIDDLE`, or `UNKNOWN`).

```java
.onClick(13, context -> {
    if (context.action() == ClickAction.RIGHT) {
        context.player().sendMessage("You right-clicked!");
    } else {
        context.player().sendMessage("You clicked!");
    }
});
```

---

## 3. Inventory Click Protection Guards

To prevent standard menu bugs, OumLib implements two click protection safeguards internally:
1. **Auto-Cancellation**: All clicks inside the menu container are cancelled (`event.setCancelled(true)`) to prevent players from taking items.
2. **Player Inventory Isolation**: The click listener checks:
   ```java
   if (event.getClickedInventory() == null || !event.getClickedInventory().equals(inv)) return;
   ```
   This ensures that clicking items inside the player's own inventory hotbar does *not* trigger the GUI slot handlers, preventing unexpected actions or item glitches.

---

## 4. ItemBuilder Reference

To make inventory item creations clean, OumLib includes a chainable `ItemBuilder` helper:

```java
import dev.oum.oumlib.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

// Create a new item from scratch
ItemStack item1 = ItemBuilder.of(Material.DIAMOND_SWORD)
    .name("<gold>Excalibur</gold>")
    .lore("A legendary sword", "of kings")
    .enchant(Enchantment.SHARPNESS, 5)
    .glow() // Makes the item glow without showing enchantments flag
    .unbreakable(true)
    .customModelData(101) // Resource pack CustomModelData
    .build();

// Modify an existing item stack (Copy and Edit)
ItemStack copied = ItemBuilder.of(item1)
    .type(Material.NETHERITE_SWORD) // Swaps material
    .addLore("Modified by system")  // Appends to existing lore
    .build();

// Other utility methods:
// .clearLore() - Removes all lore lines
// .amount(int) - Sets stack quantity
// .flag(ItemFlag...) - Adds specific item flags
```

---

## 5. AnvilMenu Reference

`AnvilMenu` provides a simple way to prompt players for text input using Minecraft's anvil interface.

```java
import dev.oum.oumlib.inventory.AnvilMenu;

AnvilMenu menu = AnvilMenu.builder()
    .title("<blue>Rename Item</blue>")
    .placeholder("Enter new name...")
    .onConfirm((player, text) -> {
        player.sendMessage("You entered: " + text);
    })
    .onClose(player -> {
        player.sendMessage("You cancelled the input.");
    })
    .build();

// Open the menu for a player
menu.open(player);
```
