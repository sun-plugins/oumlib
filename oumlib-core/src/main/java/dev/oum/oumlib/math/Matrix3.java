package dev.oum.oumlib.math;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public record Matrix3(
        double m00, double m01, double m02,
        double m10, double m11, double m12,
        double m20, double m21, double m22
) {

    public static final Matrix3 IDENTITY = new Matrix3(
            1.0, 0.0, 0.0,
            0.0, 1.0, 0.0,
            0.0, 0.0, 1.0
    );

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull Matrix3 rotationX(double angle) {
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        return new Matrix3(
                1.0, 0.0, 0.0,
                0.0, c, -s,
                0.0, s, c
        );
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull Matrix3 rotationY(double angle) {
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        return new Matrix3(
                c, 0.0, s,
                0.0, 1.0, 0.0,
                -s, 0.0, c
        );
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull Matrix3 rotationZ(double angle) {
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        return new Matrix3(
                c, -s, 0.0,
                s, c, 0.0,
                0.0, 0.0, 1.0
        );
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NonNull Matrix3 rotationAroundAxis(@NonNull Vector3D axis, double angle) {
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        double t = 1.0 - c;
        Vector3D norm = axis.normalize();
        double x = norm.x();
        double y = norm.y();
        double z = norm.z();
        return new Matrix3(
                t * x * x + c, t * x * y - s * z, t * x * z + s * y,
                t * x * y + s * z, t * y * y + c, t * y * z - s * x,
                t * x * z - s * y, t * y * z + s * x, t * z * z + c
        );
    }

    @Contract(value = "_ -> new", pure = true)
    public @NonNull Vector3D transform(@NonNull Vector3D vec) {
        return new Vector3D(
                m00 * vec.x() + m01 * vec.y() + m02 * vec.z(),
                m10 * vec.x() + m11 * vec.y() + m12 * vec.z(),
                m20 * vec.x() + m21 * vec.y() + m22 * vec.z()
        );
    }
}
