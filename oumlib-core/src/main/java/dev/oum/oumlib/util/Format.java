package dev.oum.oumlib.util;

import org.jspecify.annotations.NonNull;

import java.text.DecimalFormat;
import java.time.Duration;

public final class Format {

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###.##");
    private static final String[] SUFFIXES = {"", "k", "M", "B", "T"};

    private Format() {
    }

    /**
     * Formats a duration into a clean string (e.g., 125s -> "2m 5s", 45s -> "45s").
     */
    public static @NonNull String duration(@NonNull Duration duration) {
        long seconds = duration.getSeconds();
        if (seconds <= 0) return "0s";

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (secs > 0 || sb.isEmpty()) sb.append(secs).append("s");

        return sb.toString().trim();
    }

    /**
     * Formats a duration into standard digital time (e.g., 125s -> "02:05", 3665s -> "01:01:05").
     */
    public static @NonNull String digitalTime(@NonNull Duration duration) {
        long seconds = duration.getSeconds();
        if (seconds <= 0) return "00:00";

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }

    /**
     * Formats a number with comma separators (e.g., 1250000 -> "1,250,000").
     */
    public static String number(double number) {
        return NUMBER_FORMAT.format(number);
    }

    /**
     * Compacts a number into suffixes (e.g., 1500 -> "1.5k", 2500000 -> "2.5M").
     */
    public static String compactNumber(double number) {
        if (number < 1000) return String.valueOf((int) number);
        int exp = (int) (Math.log(number) / Math.log(1000));
        return String.format("%.1f%s", number / Math.pow(1000, exp), SUFFIXES[exp]);
    }
}
