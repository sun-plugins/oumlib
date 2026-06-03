package dev.oum.oumlib.config;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class YamlParser {

    private YamlParser() {
    }

    public static @NonNull Map<String, Object> parse(@NonNull File file) {
        if (!file.exists()) return new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return parseReader(reader);
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    public static @NonNull Map<String, Object> parse(@NonNull InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return parseReader(reader);
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    private static @NonNull Map<String, Object> parseReader(BufferedReader reader) throws Exception {
        List<YamlLine> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            int indent = 0;
            while (indent < line.length() && line.charAt(indent) == ' ') {
                indent++;
            }
            String trim = line.trim();
            if (trim.isEmpty() || trim.startsWith("#")) continue;
            int colon = trim.indexOf(':');
            if (colon == -1) continue;
            String key = trim.substring(0, colon).trim();
            String valStr = trim.substring(colon + 1).trim();
            if (valStr.isEmpty()) valStr = null;

            if (valStr != null) {
                int hashIdx = valStr.indexOf('#');
                if (hashIdx != -1) {
                    valStr = valStr.substring(0, hashIdx).trim();
                    if (valStr.isEmpty()) valStr = null;
                }
            }
            lines.add(new YamlLine(indent, key, valStr));
        }

        if (lines.isEmpty()) return new LinkedHashMap<>();
        int[] nextIdx = new int[1];
        return parseYamlLines(lines, 0, lines.get(0).indent, nextIdx);
    }

    private static @NonNull Map<String, Object> parseYamlLines(@NonNull List<YamlLine> lines, int startIndex,
                                                               int currentIndent, int[] nextIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        int i = startIndex;
        while (i < lines.size()) {
            YamlLine line = lines.get(i);
            if (line.indent < currentIndent) {
                break;
            }
            if (line.indent > currentIndent) {
                break;
            }

            if (line.valStr == null) {
                int childIndent = -1;
                for (int j = i + 1; j < lines.size(); j++) {
                    if (lines.get(j).indent > currentIndent) {
                        childIndent = lines.get(j).indent;
                    }
                    break;
                }

                if (childIndent != -1) {
                    int[] childNext = new int[1];
                    Map<String, Object> childMap = parseYamlLines(lines, i + 1, childIndent, childNext);
                    map.put(line.key, childMap);
                    i = childNext[0];
                } else {
                    map.put(line.key, new LinkedHashMap<>());
                    i++;
                }
            } else {
                map.put(line.key, getObject(line.valStr));
                i++;
            }
        }
        nextIndex[0] = i;
        return map;
    }

    private static @Nullable Object getObject(@NonNull String valStr) {
        Object val;
        if (valStr.startsWith("\"") && valStr.endsWith("\"")) {
            val = valStr.substring(1, valStr.length() - 1).replace("\\\"", "\"");
        } else if (valStr.startsWith("'") && valStr.endsWith("'")) {
            val = valStr.substring(1, valStr.length() - 1);
        } else if (valStr.equalsIgnoreCase("true")) {
            val = true;
        } else if (valStr.equalsIgnoreCase("false")) {
            val = false;
        } else if (valStr.equalsIgnoreCase("null")) {
            val = null;
        } else {
            try {
                if (valStr.contains(".")) {
                    val = Double.parseDouble(valStr);
                } else {
                    val = Integer.parseInt(valStr);
                }
            } catch (NumberFormatException e) {
                val = valStr;
            }
        }
        return val;
    }

    private record YamlLine(int indent, String key, String valStr) {
    }
}
