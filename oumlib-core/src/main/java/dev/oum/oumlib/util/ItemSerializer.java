package dev.oum.oumlib.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.Base64;

public final class ItemSerializer {

    private static Method serializeAsBytesMethod;
    private static Method deserializeBytesMethod;
    private static boolean paperBytesSupported = false;

    static {
        try {
            serializeAsBytesMethod = ItemStack.class.getMethod("serializeAsBytes");
            deserializeBytesMethod = ItemStack.class.getMethod("deserializeBytes", byte[].class);
            paperBytesSupported = true;
        } catch (NoSuchMethodException ignored) {
        }
    }

    private ItemSerializer() {
    }

    public static @NonNull String serialize(@Nullable ItemStack item) {
        if (item == null) return "";
        try {
            byte[] bytes = serializeToBytes(item);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ItemStack", e);
        }
    }

    public static @Nullable ItemStack deserialize(@NonNull String base64) {
        if (base64.isEmpty()) return null;
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return deserializeFromBytes(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize ItemStack", e);
        }
    }

    public static @NonNull String serializeArray(ItemStack @NonNull [] items) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeInt(items.length);
            for (ItemStack item : items) {
                if (item == null) {
                    dos.writeInt(-1);
                } else {
                    byte[] bytes = serializeToBytes(item);
                    dos.writeInt(bytes.length);
                    dos.write(bytes);
                }
            }
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ItemStack array", e);
        }
    }

    public static ItemStack @NonNull [] deserializeArray(@NonNull String base64) {
        if (base64.isEmpty()) return new ItemStack[0];
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                 DataInputStream dis = new DataInputStream(bais)) {
                int length = dis.readInt();
                ItemStack[] items = new ItemStack[length];
                for (int i = 0; i < length; i++) {
                    int size = dis.readInt();
                    if (size == -1) {
                        items[i] = null;
                    } else {
                        byte[] itemBytes = new byte[size];
                        dis.readFully(itemBytes);
                        items[i] = deserializeFromBytes(itemBytes);
                    }
                }
                return items;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize ItemStack array", e);
        }
    }

    private static byte[] serializeToBytes(@NonNull ItemStack item) throws Exception {
        if (paperBytesSupported) {
            return (byte[]) serializeAsBytesMethod.invoke(item);
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)) {
            boos.writeObject(item);
            return baos.toByteArray();
        }
    }

    private static @NonNull ItemStack deserializeFromBytes(byte @NonNull [] bytes) throws Exception {
        if (paperBytesSupported) {
            return (ItemStack) deserializeBytesMethod.invoke(null, (Object) bytes);
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)) {
            return (ItemStack) bois.readObject();
        }
    }
}
