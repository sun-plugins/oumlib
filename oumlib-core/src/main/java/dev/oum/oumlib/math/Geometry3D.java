package dev.oum.oumlib.math;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class Geometry3D {

    private Geometry3D() {
    }

    @Contract(value = "_, _, _, _, _ -> new", pure = true)
    public static @NonNull List<Vector3D> bezier(@NonNull Vector3D start, @NonNull Vector3D control1,
                                                 @NonNull Vector3D control2, @NonNull Vector3D end, int segments) {
        List<Vector3D> points = new ArrayList<>();
        if (segments <= 0) {
            points.add(start);
            points.add(end);
            return points;
        }
        for (int i = 0; i <= segments; i++) {
            double t = (double) i / segments;
            double u = 1.0 - t;
            double tt = t * t;
            double uu = u * u;
            double uuu = uu * u;
            double ttt = tt * t;
            double x = uuu * start.x() + 3.0 * uu * t * control1.x() + 3.0 * u * tt * control2.x() + ttt * end.x();
            double y = uuu * start.y() + 3.0 * uu * t * control1.y() + 3.0 * u * tt * control2.y() + ttt * end.y();
            double z = uuu * start.z() + 3.0 * uu * t * control1.z() + 3.0 * u * tt * control2.z() + ttt * end.z();
            points.add(new Vector3D(x, y, z));
        }
        return points;
    }

    @Contract(value = "_, _, _, _, _ -> new", pure = true)
    public static @NonNull List<Vector3D> helix(@NonNull Vector3D center, double radius,
                                                double pitch, double height, int steps) {
        List<Vector3D> points = new ArrayList<>();
        if (steps <= 0) return points;
        for (int i = 0; i < steps; i++) {
            double fraction = (double) i / steps;
            double angle = fraction * height / pitch * 2.0 * Math.PI;
            double yOffset = fraction * height;
            double x = center.x() + radius * Math.cos(angle);
            double z = center.z() + radius * Math.sin(angle);
            double y = center.y() + yOffset;
            points.add(new Vector3D(x, y, z));
        }
        return points;
    }

    @Contract(pure = true)
    public static boolean intersects(@NonNull Vector3D rayOrigin, @NonNull Vector3D rayDirection,
                                     @NonNull Vector3D minBound, @NonNull Vector3D maxBound) {
        Vector3D dirFraction = new Vector3D(
                rayDirection.x() == 0.0 ? Double.MAX_VALUE : 1.0 / rayDirection.x(),
                rayDirection.y() == 0.0 ? Double.MAX_VALUE : 1.0 / rayDirection.y(),
                rayDirection.z() == 0.0 ? Double.MAX_VALUE : 1.0 / rayDirection.z()
        );
        double t1 = (minBound.x() - rayOrigin.x()) * dirFraction.x();
        double t2 = (maxBound.x() - rayOrigin.x()) * dirFraction.x();
        double t3 = (minBound.y() - rayOrigin.y()) * dirFraction.y();
        double t4 = (maxBound.y() - rayOrigin.y()) * dirFraction.y();
        double t5 = (minBound.z() - rayOrigin.z()) * dirFraction.z();
        double t6 = (maxBound.z() - rayOrigin.z()) * dirFraction.z();
        double tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        double tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));
        if (tmax < 0.0) return false;
        return tmin <= tmax;
    }
}
