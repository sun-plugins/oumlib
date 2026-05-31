package dev.oum.oumlib.inventory;

import dev.oum.oumlib.OumLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ItemBuilder {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final ItemStack stack;
    private final ItemMeta meta;

    private ItemBuilder(Material material) {
        this.stack = new ItemStack(material);
        this.meta = stack.getItemMeta();
    }

    private ItemBuilder(@NonNull ItemStack item) {
        this.stack = item.clone();
        this.meta = stack.getItemMeta();
    }

    @Contract("_ -> new")
    public static @NonNull ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    @Contract("_ -> new")
    public static @NonNull ItemBuilder of(ItemStack item) {
        return new ItemBuilder(item);
    }

    public ItemBuilder name(String miniMessage) {
        meta.displayName(MM.deserialize("<!italic>" + miniMessage));
        return this;
    }

    public ItemBuilder lore(String... lines) {
        meta.lore(Arrays.stream(lines)
                .map(l -> MM.deserialize("<!italic><gray>" + l))
                .collect(Collectors.toList()));
        return this;
    }

    public ItemBuilder clearLore() {
        meta.lore(null);
        return this;
    }

    public ItemBuilder addLore(String... lines) {
        List<Component> currentLore = meta.lore();
        if (currentLore == null) currentLore = new ArrayList<>();
        List<Component> newLines = Arrays.stream(lines)
                .map(l -> MM.deserialize("<!italic><gray>" + l))
                .toList();
        currentLore.addAll(newLines);
        meta.lore(currentLore);
        return this;
    }

    public ItemBuilder amount(int amount) {
        stack.setAmount(amount);
        return this;
    }

    public ItemBuilder type(Material material) {
        stack.setType(material);
        return this;
    }

    public ItemBuilder enchant(Enchantment e, int level) {
        meta.addEnchant(e, level, true);
        return this;
    }

    public ItemBuilder glow() {
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder flag(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder customModelData(Integer data) {
        meta.setCustomModelData(data);
        return this;
    }

    public ItemBuilder unbreakable(boolean value) {
        meta.setUnbreakable(value);
        return this;
    }

    public ItemBuilder pdc(String key, String value) {
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        meta.getPersistentDataContainer().set(nsk, PersistentDataType.STRING, value);
        return this;
    }

    public ItemBuilder pdc(String key, int value) {
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        meta.getPersistentDataContainer().set(nsk, PersistentDataType.INTEGER, value);
        return this;
    }

    public ItemBuilder pdc(String key, double value) {
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        meta.getPersistentDataContainer().set(nsk, PersistentDataType.DOUBLE, value);
        return this;
    }

    public ItemStack build() {
        stack.setItemMeta(meta);
        return stack;
    }
}