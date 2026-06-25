package dev.oum.oumlib.math;

import org.jetbrains.annotations.Contract;

public final class FastMath {

    private FastMath() {
    }

    @Contract(pure = true)
    public static double safeDivide(double numerator, double denominator, double fallback) {
        if (denominator == 0.0 || Double.isNaN(denominator) || Double.isInfinite(denominator)) {
            return fallback;
        }
        double result = numerator / denominator;
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            return fallback;
        }
        return result;
    }

    @Contract(pure = true)
    public static double clamp(double val, double min, double max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    @Contract(pure = true)
    public static int clamp(int val, int min, int max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    @Contract(pure = true)
    public static double map(double val, double fromMin, double fromMax, double toMin, double toMax) {
        if (fromMax - fromMin == 0.0) return toMin;
        return toMin + (val - fromMin) / (fromMax - fromMin) * (toMax - toMin);
    }

    @Contract(pure = true)
    public static double sin(double x) {
        x = x % (2.0 * Math.PI);
        if (x < -Math.PI) x += 2.0 * Math.PI;
        else if (x > Math.PI) x -= 2.0 * Math.PI;
        if (x < 0.0) {
            double nx = -x;
            return -16.0 * nx * (Math.PI - nx) / (5.0 * Math.PI * Math.PI - 4.0 * nx * (Math.PI - nx));
        } else {
            return 16.0 * x * (Math.PI - x) / (5.0 * Math.PI * Math.PI - 4.0 * x * (Math.PI - x));
        }
    }

    @Contract(pure = true)
    public static double cos(double x) {
        return sin(x + Math.PI * 0.5);
    }
}
