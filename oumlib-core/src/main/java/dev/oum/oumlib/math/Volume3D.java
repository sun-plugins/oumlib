package dev.oum.oumlib.math;

import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public interface Volume3D {

    @Contract(pure = true)
    boolean contains(@NonNull Vector3D point);

    @Contract(pure = true)
    boolean intersects(@NonNull Volume3D other);

    record AABB3D(@NonNull Vector3D min, @NonNull Vector3D max) implements Volume3D {
        @Contract(value = "_ -> new", pure = true)
        public static @NonNull AABB3D fromBoundingBox(BoundingBox box) {
            if (box == null) {
                return new AABB3D(Vector3D.ZERO, Vector3D.ZERO);
            }
            return new AABB3D(
                    new Vector3D(box.getMinX(), box.getMinY(), box.getMinZ()),
                    new Vector3D(box.getMaxX(), box.getMaxY(), box.getMaxZ())
            );
        }

        @Override
        @Contract(pure = true)
        public boolean contains(@NonNull Vector3D p) {
            return p.x() >= min.x() && p.x() <= max.x() &&
                    p.y() >= min.y() && p.y() <= max.y() &&
                    p.z() >= min.z() && p.z() <= max.z();
        }

        @Override
        @Contract(pure = true)
        public boolean intersects(@NonNull Volume3D other) {
            if (other instanceof AABB3D(Vector3D min1, Vector3D max1)) {
                return min.x() <= max1.x() && max.x() >= min1.x() &&
                        min.y() <= max1.y() && max.y() >= min1.y() &&
                        min.z() <= max1.z() && max.z() >= min1.z();
            }
            if (other instanceof Sphere3D(Vector3D center, double radius)) {
                double x = Math.max(min.x(), Math.min(center.x(), max.x()));
                double y = Math.max(min.y(), Math.min(center.y(), max.y()));
                double z = Math.max(min.z(), Math.min(center.z(), max.z()));
                return center.distanceSquared(new Vector3D(x, y, z)) <= radius * radius;
            }
            return other.intersects(this);
        }

        @Contract(value = " -> new", pure = true)
        public @NonNull BoundingBox toBoundingBox() {
            return new BoundingBox(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
        }
    }

    record Sphere3D(@NonNull Vector3D center, double radius) implements Volume3D {
        @Override
        @Contract(pure = true)
        public boolean contains(@NonNull Vector3D p) {
            return center.distanceSquared(p) <= radius * radius;
        }

        @Override
        @Contract(pure = true)
        public boolean intersects(@NonNull Volume3D other) {
            if (other instanceof Sphere3D(Vector3D center1, double radius2)) {
                double rSum = radius + radius2;
                return center.distanceSquared(center1) <= rSum * rSum;
            }
            if (other instanceof AABB3D box) {
                return box.intersects(this);
            }
            if (other instanceof Cylinder3D(Vector3D base, double radius1, double height)) {
                double rSum = radius + radius1;
                double dx = center.x() - base.x();
                double dz = center.z() - base.z();
                if (dx * dx + dz * dz > rSum * rSum) return false;
                double clampedY = Math.max(base.y(), Math.min(center.y(), base.y() + height));
                double dy = center.y() - clampedY;
                return dy * dy + dx * dx + dz * dz <= rSum * rSum;
            }
            return other.intersects(this);
        }
    }

    record Cylinder3D(@NonNull Vector3D base, double radius, double height) implements Volume3D {
        @Override
        @Contract(pure = true)
        public boolean contains(@NonNull Vector3D p) {
            if (p.y() < base.y() || p.y() > base.y() + height) return false;
            double dx = p.x() - base.x();
            double dz = p.z() - base.z();
            return dx * dx + dz * dz <= radius * radius;
        }

        @Override
        @Contract(pure = true)
        public boolean intersects(@NonNull Volume3D other) {
            if (other instanceof Cylinder3D(Vector3D base1, double radius1, double height1)) {
                double rSum = radius + radius1;
                double dx = base.x() - base1.x();
                double dz = base.z() - base1.z();
                if (dx * dx + dz * dz > rSum * rSum) return false;
                return base.y() <= base1.y() + height1 && base.y() + height >= base1.y();
            }
            if (other instanceof Sphere3D sphere) {
                return sphere.intersects(this);
            }
            if (other instanceof AABB3D(Vector3D min, Vector3D max)) {
                double x = Math.max(min.x(), Math.min(base.x(), max.x()));
                double z = Math.max(min.z(), Math.min(base.z(), max.z()));
                double dx = base.x() - x;
                double dz = base.z() - z;
                if (dx * dx + dz * dz > radius * radius) return false;
                return base.y() <= max.y() && base.y() + height >= min.y();
            }
            return other.intersects(this);
        }
    }
}
