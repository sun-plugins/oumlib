package dev.oum.oumlib.math;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record Vector3D(double x, double y, double z) {

    public static final Vector3D ZERO = new Vector3D(0.0, 0.0, 0.0);
    public static final Vector3D ONE = new Vector3D(1.0, 1.0, 1.0);

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull Vector3D fromBukkit(@Nullable Vector vector) {
        if (vector == null) return ZERO;
        return new Vector3D(vector.getX(), vector.getY(), vector.getZ());
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull Vector3D fromLocation(@Nullable Location loc) {
        if (loc == null) return ZERO;
        return new Vector3D(loc.getX(), loc.getY(), loc.getZ());
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull Vector3D fromBlock(@Nullable Block block) {
        if (block == null) return ZERO;
        return new Vector3D(block.getX(), block.getY(), block.getZ());
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull Vector3D fromEntity(@Nullable Entity entity) {
        if (entity == null) return ZERO;
        return fromLocation(entity.getLocation());
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull Vector3D fromBlockVector(@Nullable BlockVector bv) {
        if (bv == null) return ZERO;
        return new Vector3D(bv.getX(), bv.getY(), bv.getZ());
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull Vector3D read(@NonNull DataInputStream dis) throws IOException {
        return new Vector3D(dis.readDouble(), dis.readDouble(), dis.readDouble());
    }

    @Contract(value = "_ -> new", pure = true)
    public @NonNull Vector3D add(@NonNull Vector3D other) {
        return new Vector3D(x + other.x, y + other.y, z + other.z);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public @NonNull Vector3D add(double dx, double dy, double dz) {
        return new Vector3D(x + dx, y + dy, z + dz);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NonNull Vector3D subtract(@NonNull Vector3D other) {
        return new Vector3D(x - other.x, y - other.y, z - other.z);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public @NonNull Vector3D subtract(double dx, double dy, double dz) {
        return new Vector3D(x - dx, y - dy, z - dz);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NonNull Vector3D multiply(double factor) {
        return new Vector3D(x * factor, y * factor, z * factor);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NonNull Vector3D multiply(@NonNull Vector3D other) {
        return new Vector3D(x * other.x, y * other.y, z * other.z);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NonNull Vector3D divide(double divisor) {
        if (divisor == 0.0) return ZERO;
        return new Vector3D(x / divisor, y / divisor, z / divisor);
    }

    @Contract(pure = true)
    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    @Contract(pure = true)
    public double lengthSquared() {
        return x * x + y * y + z * z;
    }

    @Contract(value = " -> new", pure = true)
    public @NonNull Vector3D normalize() {
        double len = length();
        if (len == 0.0) return ZERO;
        return new Vector3D(x / len, y / len, z / len);
    }

    @Contract(pure = true)
    public double dot(@NonNull Vector3D other) {
        return x * other.x + y * other.y + z * other.z;
    }

    @Contract(value = "_ -> new", pure = true)
    public @NonNull Vector3D cross(@NonNull Vector3D other) {
        return new Vector3D(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
        );
    }

    @Contract(pure = true)
    public double distance(@NonNull Vector3D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Contract(pure = true)
    public double distanceSquared(@NonNull Vector3D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    @Contract(value = "_, _ -> new", pure = true)
    public @NonNull Vector3D lerp(@NonNull Vector3D other, double t) {
        double newX = x + (other.x - x) * t;
        double newY = y + (other.y - y) * t;
        double newZ = z + (other.z - z) * t;
        return new Vector3D(newX, newY, newZ);
    }

    @Contract(value = " -> new", pure = true)
    public @NonNull Vector toBukkit() {
        return new Vector(x, y, z);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NonNull Location toLocation(@Nullable World world) {
        return new Location(world, x, y, z);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public @NonNull Location toLocation(@Nullable World world, float yaw, float pitch) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public void applyVelocity(@Nullable Entity entity) {
        if (entity != null) {
            entity.setVelocity(toBukkit());
        }
    }

    public void teleport(@Nullable Entity entity) {
        if (entity != null) {
            entity.teleport(toLocation(entity.getWorld()));
        }
    }

    @Contract(value = " -> new", pure = true)
    public @NonNull BlockVector toBlockVector() {
        return new BlockVector(x, y, z);
    }

    public void write(@NonNull DataOutputStream dos) throws IOException {
        dos.writeDouble(x);
        dos.writeDouble(y);
        dos.writeDouble(z);
    }
}
