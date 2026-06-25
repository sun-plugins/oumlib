package dev.oum.oumlib.math;

import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record Quaternion(double x, double y, double z, double w) {

    public static final Quaternion IDENTITY = new Quaternion(0.0, 0.0, 0.0, 1.0);

    @Contract(value = "_, _ -> new", pure = true)
    public static @NonNull Quaternion fromAxisAngle(@NonNull Vector3D axis, double angle) {
        double halfAngle = angle * 0.5;
        double sin = Math.sin(halfAngle);
        Vector3D norm = axis.normalize();
        return new Quaternion(
                norm.x() * sin,
                norm.y() * sin,
                norm.z() * sin,
                Math.cos(halfAngle)
        );
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull Quaternion fromEulerAngle(@Nullable EulerAngle angle) {
        if (angle == null) return IDENTITY;
        double cx = Math.cos(angle.getX() * 0.5);
        double sx = Math.sin(angle.getX() * 0.5);
        double cy = Math.cos(angle.getY() * 0.5);
        double sy = Math.sin(angle.getY() * 0.5);
        double cz = Math.cos(angle.getZ() * 0.5);
        double sz = Math.sin(angle.getZ() * 0.5);

        return new Quaternion(
                sx * cy * cz - cx * sy * sz,
                cx * sy * cz + sx * cy * sz,
                cx * cy * sz - sx * sy * cz,
                cx * cy * cz + sx * sy * sz
        );
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull Quaternion read(@NonNull DataInputStream dis) throws IOException {
        return new Quaternion(dis.readDouble(), dis.readDouble(), dis.readDouble(), dis.readDouble());
    }

    @Contract(value = "_ -> new", pure = true)
    public @NonNull Quaternion multiply(@NonNull Quaternion other) {
        return new Quaternion(
                w * other.x + x * other.w + y * other.z - z * other.y,
                w * other.y - x * other.z + y * other.w + z * other.x,
                w * other.z + x * other.y - y * other.x + z * other.w,
                w * other.w - x * other.x - y * other.y - z * other.z
        );
    }

    @Contract(value = "_ -> new", pure = true)
    public @NonNull Vector3D multiply(@NonNull Vector3D vec) {
        double num1 = x * 2.0;
        double num2 = y * 2.0;
        double num3 = z * 2.0;
        double num4 = x * num1;
        double num5 = y * num2;
        double num6 = z * num3;
        double num7 = x * num2;
        double num8 = x * num3;
        double num9 = y * num3;
        double num10 = w * num1;
        double num11 = w * num2;
        double num12 = w * num3;
        return new Vector3D(
                (1.0 - (num5 + num6)) * vec.x() + (num7 - num12) * vec.y() + (num8 + num11) * vec.z(),
                (num7 + num12) * vec.x() + (1.0 - (num4 + num6)) * vec.y() + (num9 - num10) * vec.z(),
                (num8 - num11) * vec.x() + (num9 + num10) * vec.y() + (1.0 - (num4 + num5)) * vec.z()
        );
    }

    @Contract(value = " -> new", pure = true)
    public @NonNull Quaternion conjugate() {
        return new Quaternion(-x, -y, -z, w);
    }

    @Contract(pure = true)
    public double dot(@NonNull Quaternion other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
    }

    @Contract(value = " -> new", pure = true)
    public @NonNull Quaternion normalize() {
        double len = Math.sqrt(x * x + y * y + z * z + w * w);
        if (len == 0.0) return IDENTITY;
        return new Quaternion(x / len, y / len, z / len, w / len);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public @NonNull Quaternion slerp(@NonNull Quaternion other, double t) {
        double cosHalfTheta = dot(other);
        Quaternion correctedOther = other;
        if (cosHalfTheta < 0.0) {
            correctedOther = new Quaternion(-other.x, -other.y, -other.z, -other.w);
            cosHalfTheta = -cosHalfTheta;
        }
        if (Math.abs(cosHalfTheta) >= 1.0) {
            return this;
        }
        double halfTheta = Math.acos(cosHalfTheta);
        double sinHalfTheta = Math.sqrt(1.0 - cosHalfTheta * cosHalfTheta);
        if (Math.abs(sinHalfTheta) < 0.001) {
            return new Quaternion(
                    x * (1.0 - t) + correctedOther.x * t,
                    y * (1.0 - t) + correctedOther.y * t,
                    z * (1.0 - t) + correctedOther.z * t,
                    w * (1.0 - t) + correctedOther.w * t
            ).normalize();
        }
        double ratioA = Math.sin((1.0 - t) * halfTheta) / sinHalfTheta;
        double ratioB = Math.sin(t * halfTheta) / sinHalfTheta;
        return new Quaternion(
                x * ratioA + correctedOther.x * ratioB,
                y * ratioA + correctedOther.y * ratioB,
                z * ratioA + correctedOther.z * ratioB,
                w * ratioA + correctedOther.w * ratioB
        );
    }

    @Contract(value = " -> new", pure = true)
    public @NonNull EulerAngle toEulerAngle() {
        double sinr_cosp = 2.0 * (w * x + y * z);
        double cosr_cosp = 1.0 - 2.0 * (x * x + y * y);
        double roll = Math.atan2(sinr_cosp, cosr_cosp);

        double sinp = 2.0 * (w * y - z * x);
        double pitch;
        if (Math.abs(sinp) >= 1.0) {
            pitch = Math.copySign(Math.PI / 2.0, sinp);
        } else {
            pitch = Math.asin(sinp);
        }

        double siny_cosp = 2.0 * (w * z + x * y);
        double cosy_cosp = 1.0 - 2.0 * (y * y + z * z);
        double yaw = Math.atan2(siny_cosp, cosy_cosp);

        return new EulerAngle(roll, pitch, yaw);
    }

    public void write(@NonNull DataOutputStream dos) throws IOException {
        dos.writeDouble(x);
        dos.writeDouble(y);
        dos.writeDouble(z);
        dos.writeDouble(w);
    }
}
