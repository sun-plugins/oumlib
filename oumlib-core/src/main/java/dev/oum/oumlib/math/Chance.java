package dev.oum.oumlib.math;

import org.jetbrains.annotations.Contract;

import java.util.concurrent.ThreadLocalRandom;

public final class Chance {

    private Chance() {
    }

    @Contract
    public static boolean test(double probability) {
        if (probability <= 0.0) return false;
        if (probability >= 1.0) return true;
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    @Contract
    public static boolean testPercent(double percentage) {
        return test(percentage / 100.0);
    }

    @Contract
    public static double randomIn(double min, double max) {
        if (min >= max) return min;
        return min + (max - min) * ThreadLocalRandom.current().nextDouble();
    }

    @Contract
    public static int randomIn(int min, int max) {
        if (min >= max) return min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
