package dev.oum.oumlib.inventory;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.Gson;
import dev.oum.oumlib.OumLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ItemBuilder {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Gson GSON = new Gson();
    private final ItemMeta meta;
    private ItemStack stack;

    private ItemBuilder(Material material) {
        this.stack = new ItemStack(material);
        this.meta = stack.getItemMeta();
    }

    private ItemBuilder(@NonNull ItemStack item) {
        this.stack = item.clone();
        this.meta = stack.getItemMeta();
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull ItemBuilder of(ItemStack item) {
        return new ItemBuilder(item);
    }

    @CheckReturnValue
    public @NonNull ItemBuilder name(@NonNull String miniMessage) {
        meta.displayName(MM.deserialize("<!italic>" + miniMessage));
        return this;
    }

    @CheckReturnValue
    public @NonNull ItemBuilder lore(String @NonNull ... lines) {
        meta.lore(Arrays.stream(lines)
                .map(l -> MM.deserialize("<!italic><gray>" + l))
                .collect(Collectors.toList()));
        return this;
    }

    @CheckReturnValue
    public @NonNull ItemBuilder clearLore() {
        meta.lore(null);
        return this;
    }

    @CheckReturnValue
    public @NonNull ItemBuilder addLore(String @NonNull ... lines) {
        List<Component> currentLore = meta.lore();
        if (currentLore == null) currentLore = new ArrayList<>();
        List<Component> newLines = Arrays.stream(lines)
                .map(l -> MM.deserialize("<!italic><gray>" + l))
                .toList();
        currentLore.addAll(newLines);
        meta.lore(currentLore);
        return this;
    }

    @CheckReturnValue
    public @NonNull ItemBuilder amount(int amount) {
        stack.setAmount(amount);
        return this;
    }

    @CheckReturnValue
    public @NonNull ItemBuilder type(@NonNull Material material) {
        stack = stack.withType(material);
        return this;
    }

    @CheckReturnValue
    public @NonNull ItemBuilder enchant(@NonNull Enchantment e, int level) {
        meta.addEnchant(e, level, true);
        return this;
    }

    @CheckReturnValue
    public @NonNull ItemBuilder glow() {
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    @CheckReturnValue
    public @NonNull ItemBuilder flag(ItemFlag @NonNull ... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    @CheckReturnValue
    @SuppressWarnings("deprecation")
    public @NonNull ItemBuilder customModelData(@Nullable Integer data) {
        meta.setCustomModelData(data);
        return this;
    }

    @CheckReturnValue
    public @NonNull ItemBuilder unbreakable(boolean value) {
        meta.setUnbreakable(value);
        return this;
    }

    @CheckReturnValue
    public @NonNull ItemBuilder pdc(@NonNull String key, @NonNull String value) {
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        meta.getPersistentDataContainer().set(nsk, PersistentDataType.STRING, value);
        return this;
    }

    @CheckReturnValue
    public @NonNull ItemBuilder pdc(@NonNull String key, int value) {
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        meta.getPersistentDataContainer().set(nsk, PersistentDataType.INTEGER, value);
        return this;
    }

    @CheckReturnValue
    public @NonNull ItemBuilder pdc(@NonNull String key, double value) {
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        meta.getPersistentDataContainer().set(nsk, PersistentDataType.DOUBLE, value);
        return this;
    }

    @CheckReturnValue
    public @NonNull ItemBuilder pdc(@NonNull String key, boolean value) {
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        meta.getPersistentDataContainer().set(nsk, PersistentDataType.BYTE, (byte) (value ? 1 : 0));
        return this;
    }

    @CheckReturnValue
    public @NonNull ItemBuilder pdc(@NonNull String key, long value) {
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        meta.getPersistentDataContainer().set(nsk, PersistentDataType.LONG, value);
        return this;
    }

    @CheckReturnValue
    public @NonNull ItemBuilder pdc(@NonNull String key, @Nullable List<String> value) {
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        if (value == null) {
            meta.getPersistentDataContainer().remove(nsk);
        } else {
            meta.getPersistentDataContainer().set(nsk, PersistentDataType.STRING, GSON.toJson(value));
        }
        return this;
    }

    @CheckReturnValue
    @SuppressWarnings("unused")
    public @NonNull ItemBuilder skull(@NonNull OfflinePlayer player) {
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
        }
        return this;
    }

    @CheckReturnValue
    @SuppressWarnings("unused")
    public @NonNull ItemBuilder skull(@NonNull String textureValue) {
        if (meta instanceof SkullMeta skullMeta) {
            try {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
                PlayerTextures textures = profile.getTextures();
                URL url = URI.create("https://textures.minecraft.net/texture/" + textureValue).toURL();
                textures.setSkin(url);
                profile.setTextures(textures);
                skullMeta.setPlayerProfile(profile);
            } catch (Throwable t) {
                try {
                    Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
                    Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
                    Object profile = gameProfileClass.getConstructor(UUID.class, String.class)
                            .newInstance(UUID.randomUUID(), null);
                    Object property = propertyClass.getConstructor(String.class, String.class)
                            .newInstance("textures", textureValue);
                    Object properties = gameProfileClass.getMethod("getProperties").invoke(profile);
                    properties.getClass().getMethod("put", Object.class, Object.class).invoke(properties, "textures", property);

                    Field profileField = skullMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(skullMeta, profile);
                } catch (Throwable ignored) {
                }
            }
        }
        return this;
    }

    @Contract(" -> new")
    public @NonNull ItemStack build() {
        stack.setItemMeta(meta);
        return stack;
    }
}