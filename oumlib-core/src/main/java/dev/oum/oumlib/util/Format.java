package dev.oum.oumlib.util;

import org.jspecify.annotations.NonNull;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Format {

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###.##");
    private static final String[] SUFFIXES = {"", "k", "M", "B", "T"};

    private Format() {
    }

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

    public static String number(double number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String compactNumber(double number) {
        if (number < 1000) return String.valueOf((int) number);
        int exp = (int) (Math.log(number) / Math.log(1000));
        return String.format("%.1f%s", number / Math.pow(1000, exp), SUFFIXES[exp]);
    }

    public static @NonNull Duration parseDuration(@NonNull String input) {
        String cleaned = input.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        long totalSeconds = 0;
        Pattern pattern = Pattern.compile("(\\d+)([dhms])");
        Matcher matcher = pattern.matcher(cleaned);
        boolean matched = false;
        while (matcher.find()) {
            matched = true;
            long val = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            switch (unit) {
                case "d" -> totalSeconds += val * 24 * 60 * 60;
                case "h" -> totalSeconds += val * 60 * 60;
                case "m" -> totalSeconds += val * 60;
                case "s" -> totalSeconds += val;
            }
        }
        if (!matched) {
            try {
                return Duration.ofSeconds(Long.parseLong(cleaned));
            } catch (NumberFormatException e) {
                return Duration.ZERO;
            }
        }
        return Duration.ofSeconds(totalSeconds);
    }
}
