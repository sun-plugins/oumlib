package dev.oum.oumlib.math;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class WeightedSelector<T> {

    private final List<Entry<T>> entries;
    private final double totalWeight;

    private WeightedSelector(List<Entry<T>> entries, double totalWeight) {
        this.entries = List.copyOf(entries);
        this.totalWeight = totalWeight;
    }

    @Contract(value = " -> new", pure = true)
    public static <T> @NonNull Builder<T> builder() {
        return new Builder<>();
    }

    public @Nullable T select() {
        if (entries.isEmpty()) return null;
        if (totalWeight <= 0.0) return entries.get(0).value;
        double target = ThreadLocalRandom.current().nextDouble() * totalWeight;
        int low = 0;
        int high = entries.size() - 1;
        while (low < high) {
            int mid = (low + high) >>> 1;
            if (entries.get(mid).cumulativeWeight < target) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return entries.get(low).value;
    }

    public @NonNull List<T> values() {
        List<T> list = new ArrayList<>();
        for (Entry<T> entry : entries) {
            list.add(entry.value);
        }
        return list;
    }

    private record Entry<T>(T value, double cumulativeWeight) {
    }

    public static final class Builder<T> {
        private final List<RawEntry<T>> items = new ArrayList<>();

        @Contract(value = "_, _ -> this", mutates = "this")
        public @NonNull Builder<T> add(T value, double weight) {
            if (weight > 0.0) {
                items.add(new RawEntry<>(value, weight));
            }
            return this;
        }

        @Contract(value = " -> new", pure = true)
        public @NonNull WeightedSelector<T> build() {
            List<Entry<T>> compiled = new ArrayList<>();
            double cumulative = 0.0;
            for (RawEntry<T> item : items) {
                cumulative += item.weight;
                compiled.add(new Entry<>(item.value, cumulative));
            }
            return new WeightedSelector<>(compiled, cumulative);
        }

        private record RawEntry<T>(T value, double weight) {
        }
    }
}
