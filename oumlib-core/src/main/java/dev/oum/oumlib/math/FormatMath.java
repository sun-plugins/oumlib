package dev.oum.oumlib.math;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.Locale;
import java.util.TreeMap;

public final class FormatMath {

    private static final TreeMap<Integer, String> ROMAN_NUMERALS = new TreeMap<>();
    private static final String[] SUFFIXES = {"", "k", "M", "B", "T", "Q"};

    static {
        ROMAN_NUMERALS.put(1000, "M");
        ROMAN_NUMERALS.put(900, "CM");
        ROMAN_NUMERALS.put(500, "D");
        ROMAN_NUMERALS.put(400, "CD");
        ROMAN_NUMERALS.put(100, "C");
        ROMAN_NUMERALS.put(90, "XC");
        ROMAN_NUMERALS.put(50, "L");
        ROMAN_NUMERALS.put(40, "XL");
        ROMAN_NUMERALS.put(10, "X");
        ROMAN_NUMERALS.put(9, "IX");
        ROMAN_NUMERALS.put(5, "V");
        ROMAN_NUMERALS.put(4, "IV");
        ROMAN_NUMERALS.put(1, "I");
    }

    private FormatMath() {
    }

    @Contract(pure = true)
    public static @NonNull String compact(double number) {
        if (Double.isNaN(number) || Double.isInfinite(number)) return "0";
        boolean negative = number < 0;
        double absValue = Math.abs(number);
        if (absValue < 1000.0) {
            return (negative ? "-" : "") + formatDecimal(absValue);
        }
        int index = 0;
        while (absValue >= 1000.0 && index < SUFFIXES.length - 1) {
            absValue /= 1000.0;
            index++;
        }
        return (negative ? "-" : "") + formatDecimal(absValue) + SUFFIXES[index];
    }

    private static String formatDecimal(double val) {
        if (val == (long) val) {
            return String.format("%d", (long) val);
        }
        return String.format(Locale.US, "%.2f", val)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
    }

    @Contract(pure = true)
    public static @NonNull String toRoman(int number) {
        if (number <= 0 || number > 3999) return String.valueOf(number);
        int l = ROMAN_NUMERALS.floorKey(number);
        if (number == l) {
            return ROMAN_NUMERALS.get(number);
        }
        return ROMAN_NUMERALS.get(l) + toRoman(number - l);
    }
}
