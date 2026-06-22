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

## 2. Menu State Management

`ChestMenu` features built-in reactive state tracking. You can register state providers, embed state placeholders in the title, and update state values dynamically. OumLib handles title rendering, player-specific state scopes, and automated layout refreshes.

```java
import dev.oum.oumlib.inventory.ChestMenu;

ChestMenu menu = ChestMenu.builder()
    // Define a title containing a state placeholder
    .title("<dark_gray>Level: {player_level}</dark_gray>")
    .rows(3)
    // Register initial state supplier
    .state("player_level", player -> player.getLevel())
    .pattern(
        "#########",
        "#   L   #",
        "#########"
    )
    .bind('#', new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
    // Bind 'L' to level up item
    .bind('L', () -> new ItemStack(Material.EXPERIENCE_BOTTLE))
    .onClick('L', context -> {
        // Update the state value dynamically
        int currentLevel = (int) context.menu().getState(context.player(), "player_level");
        context.player().setLevel(currentLevel + 1);
        
        // This updates the tracked state and automatically refreshes 
        // the GUI layout and window title.
        context.menu().updateState(context.player(), "player_level", currentLevel + 1);
    })
    .build();
```

---

## 3. The ClickContext Object

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

## 4. Inventory Click Protection Guards

To prevent standard menu bugs, OumLib implements two click protection safeguards internally:
1. **Auto-Cancellation**: All clicks inside the menu container are cancelled (`event.setCancelled(true)`) to prevent players from taking items.
2. **Player Inventory Isolation**: The click listener checks:
   ```java
   if (event.getClickedInventory() == null || !event.getClickedInventory().equals(inv)) return;
   ```
   This ensures that clicking items inside the player's own inventory hotbar does *not* trigger the GUI slot handlers, preventing unexpected actions or item glitches.

---

## 5. ItemBuilder Reference

To make inventory item and custom ItemStack creations clean and readable, OumLib features a chainable, component-aware `ItemBuilder` API. It supports Adventure components, MiniMessage templates, persistent data containers (PDC), and modern Paper 1.21+ data components.

### Building Items:
```java
import dev.oum.oumlib.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

// 1. Create a formatted item using MiniMessage and String lore:
ItemStack excalibur = ItemBuilder.of(Material.DIAMOND_SWORD)
    .name("<gold>Excalibur</gold>")
    .lore(
        "A legendary sword",
        "of ancient kings"
    )
    .enchant(Enchantment.SHARPNESS, 5)
    .glow() // Makes the item glow without showing enchantments flag
    .unbreakable(true)
    .build();

// 2. Use Adventure Components directly:
ItemStack item = ItemBuilder.of(Material.GOLDEN_APPLE)
    .name(Component.text("Special Apple").color(NamedTextColor.GOLD))
    .lore(List.of(Component.text("A custom lore line")))
    .build();

// 3. Quick-build formatted items in one statement:
ItemStack quickItem = ItemBuilder.quick(
    Material.NETHERITE_INGOT,
    "<red>Netherite Alloy</red>",
    "<gray>High-grade metal used</gray>",
    "<gray>for crafting gear.</gray>"
);
```

### Modifying and Appending:
You can pass an existing `ItemStack` into the builder to copy it and make incremental edits:
```java
ItemStack copied = ItemBuilder.of(excalibur)
    .type(Material.NETHERITE_SWORD) // Swaps material to netherite
    .addLore("Modified by system")  // Appends to existing lore lines
    .amount(5)                      // Sets quantity
    .build();
```

### Modern Paper Data Component Features (1.21 & 1.21.4+):
```java
ItemStack modernItem = ItemBuilder.of(Material.SHIELD)
    // Sets the client-side 3D model path (replaces legacy CustomModelData)
    .itemModel(NamespacedKey.fromString("myplugin:custom_shield"))
    
    // Override item glint shine override
    .glintOverride(true)
    
    // Set custom maximum stack size (e.g. stack shields/swords up to 16)
    .maxStackSize(16)
    
    // Set custom max durability
    .maxDamage(500)
    
    // Set fire/lava immunity (won't burn when dropped)
    .fireResistant(true)
    
    .build();
```

### Persistent Data Container (PDC) Integration:
Attach custom typed metadata directly to items without verbose serialization wrappers:
```java
ItemStack itemWithPdc = ItemBuilder.of(Material.GOLD_INGOT)
    .name("<gold>Treasure Ingot</gold>")
    .pdc("key_string", "some-metadata")
    .pdc("key_int", 42)
    .pdc("key_double", 3.14)
    .pdc("key_boolean", true)
    
    // You can even store sub-items or lists of items inside this item's PDC:
    .pdc("key_sub_item", new ItemStack(Material.APPLE))
    .pdc("key_item_array", new ItemStack[]{ new ItemStack(Material.COOKIE) })
    .build();
```

### General Utilities:
- `.clearLore()`: Removes all lore lines.
- `.amount(int)`: Sets stack quantity.
- `.flag(ItemFlag...)`: Adds specific item flags.

---

## 6. AnvilMenu Reference

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

---

## 7. PaginatedMenu Reference

`PaginatedMenu` simplifies displaying lists of items across multiple pages, handling page navigation arrows and content distribution automatically.

```java
import dev.oum.oumlib.inventory.ItemBuilder;
import dev.oum.oumlib.inventory.PaginatedMenu;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

List<ItemStack> items = new ArrayList<>();
for (int i = 1; i <= 50; i++) {
    items.add(ItemBuilder.of(Material.GOLD_INGOT)
        .name("<yellow>Reward #" + i + "</yellow>")
        .build());
}

PaginatedMenu menu = PaginatedMenu.builder()
    .title("<dark_gray>Rewards Selection (<page>/<total>)</dark_gray>")
    .rows(6)
    // The slots where content items are populated
    .contentSlots(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25)
    .items(items)
    // Optional click handler for when a content item is clicked
    .onClick((context, item, index) -> {
        context.player().sendMessage("You clicked index " + index);
    })
    .build();

// Open the menu for a player
menu.open(player);
```
