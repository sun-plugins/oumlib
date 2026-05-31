package dev.oum.oumlib.text.placeholder;

import dev.oum.oumlib.OumLib;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlaceholderResolver {

    // Matches <namespace_key> and <namespace_key:param> — namespace and key are alphanumeric/underscore only.
    private static final Pattern PATTERN = Pattern.compile("<([a-z0-9]+)_([a-z0-9_]+)(?::([a-zA-Z0-9_.-]+))?>");

    private PlaceholderResolver() {
    }

    public static @NonNull String resolveInternal(String input, Object player) {
        PlaceholderRegistry registry = OumLib.globalRegistry();
        Matcher m = PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String namespace = m.group(1);
            String key = m.group(2);
            String paramStr = m.group(3);
            Map<String, String> params = paramStr != null ? Map.of("value", paramStr) : Map.of();
            String resolved = registry.resolve(namespace, key, player, params);
            m.appendReplacement(sb, resolved != null ? Matcher.quoteReplacement(resolved) : m.group());
        }
        m.appendTail(sb);
        return sb.toString();
    }
}