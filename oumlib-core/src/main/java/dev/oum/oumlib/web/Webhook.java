package dev.oum.oumlib.web;

import dev.oum.oumlib.scheduler.Promise;
import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.oumlib.scheduler.TaskHandle;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class Webhook {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private static final Map<String, Queue<String>> messageBuffers = new ConcurrentHashMap<>();
    private static final Map<String, Queue<WebhookEmbed>> embedBuffers = new ConcurrentHashMap<>();
    private static final Map<String, TaskHandle> flushTasks = new ConcurrentHashMap<>();

    private final String url;
    private final String username;
    private final String avatarUrl;
    private final String content;
    private final List<WebhookEmbed> embeds;

    @Contract(pure = true)
    private Webhook(@NonNull Builder builder) {
        this.url = builder.url;
        this.username = builder.username;
        this.avatarUrl = builder.avatarUrl;
        this.content = builder.content;
        this.embeds = List.copyOf(builder.embeds);
    }

    @CheckReturnValue
    public static @NonNull Builder url(@NonNull String url) {
        return new Builder(url);
    }

    public static void queueMessage(@NonNull String url, @NonNull String content) {
        messageBuffers.computeIfAbsent(url, k -> new ConcurrentLinkedQueue<>()).add(content);
        flushTasks.compute(url + "_message", (k, existing) -> {
            if (existing != null) {
                existing.cancel();
            }
            return Scheduler.runLater(Duration.ofMillis(1000), () -> flushQueue(url));
        });
    }

    public static void queueEmbed(@NonNull String url, @NonNull WebhookEmbed embed) {
        embedBuffers.computeIfAbsent(url, k -> new ConcurrentLinkedQueue<>()).add(embed);
        flushTasks.compute(url + "_embed", (k, existing) -> {
            if (existing != null) {
                existing.cancel();
            }
            return Scheduler.runLater(Duration.ofMillis(1000), () -> flushEmbedsQueue(url));
        });
    }

    private static void flushQueue(@NonNull String url) {
        Queue<String> queue = messageBuffers.get(url);
        if (queue == null || queue.isEmpty()) return;

        StringBuilder combinedContent = new StringBuilder();
        String msg;
        while ((msg = queue.poll()) != null) {
            if (!combinedContent.isEmpty()) {
                combinedContent.append("\n");
            }
            combinedContent.append(msg);
        }

        if (!combinedContent.isEmpty()) {
            Webhook.url(url)
                    .content(combinedContent.toString())
                    .sendAsync();
        }
    }

    private static void flushEmbedsQueue(@NonNull String url) {
        Queue<WebhookEmbed> queue = embedBuffers.get(url);
        if (queue == null || queue.isEmpty()) return;

        List<WebhookEmbed> batched = new ArrayList<>();
        WebhookEmbed embed;
        while ((embed = queue.poll()) != null) {
            batched.add(embed);
            if (batched.size() >= 10) {
                sendEmbedBatch(url, batched);
                batched = new ArrayList<>();
            }
        }

        if (!batched.isEmpty()) {
            sendEmbedBatch(url, batched);
        }
    }

    private static void sendEmbedBatch(String url, List<WebhookEmbed> embeds) {
        Builder builder = Webhook.url(url);
        for (WebhookEmbed em : embeds) {
            builder.embed(em);
        }
        builder.sendAsync();
    }

    private static @NonNull String escapeJson(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (ch < ' ') {
                        String t = "000" + Integer.toHexString(ch);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(ch);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static void sendWithRetry(String url, String json, int attempt) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 429 && attempt < 5) {
                long retryAfterMs = 1000;
                String retryAfterHeader = response.headers().firstValue("Retry-After").orElse(null);
                if (retryAfterHeader != null) {
                    try {
                        double val = Double.parseDouble(retryAfterHeader);
                        retryAfterMs = (long) (val * 1000.0);
                    } catch (NumberFormatException ignored) {
                    }
                } else {
                    retryAfterMs = (long) (1000 * Math.pow(2, attempt));
                }
                Thread.sleep(retryAfterMs);
                sendWithRetry(url, json, attempt + 1);
            } else if ((response.statusCode() < 200 || response.statusCode() >= 300) && attempt < 5) {
                Thread.sleep((long) (1000 * Math.pow(2, attempt)));
                sendWithRetry(url, json, attempt + 1);
            } else if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Webhook request failed after 5 retries with status: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            if (attempt < 5) {
                try {
                    Thread.sleep((long) (1000 * Math.pow(2, attempt)));
                } catch (InterruptedException ignored) {
                }
                sendWithRetry(url, json, attempt + 1);
            } else {
                throw new RuntimeException("Failed to send webhook request after 5 retries", e);
            }
        }
    }

    public @NonNull Promise<Void> sendAsync() {
        return Promise.supplyAsync(() -> {
            sendWithRetry(url, toJson(), 0);
            return null;
        });
    }

    private @NonNull String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        boolean first = true;
        if (username != null) {
            sb.append("\"username\":\"").append(escapeJson(username)).append("\"");
            first = false;
        }
        if (avatarUrl != null) {
            if (!first) sb.append(",");
            sb.append("\"avatar_url\":\"").append(escapeJson(avatarUrl)).append("\"");
            first = false;
        }
        if (content != null) {
            if (!first) sb.append(",");
            sb.append("\"content\":\"").append(escapeJson(content)).append("\"");
            first = false;
        }

        if (!embeds.isEmpty()) {
            if (!first) sb.append(",");
            sb.append("\"embeds\":[");
            for (int i = 0; i < embeds.size(); i++) {
                if (i > 0) sb.append(",");
                appendEmbedJson(sb, embeds.get(i));
            }
            sb.append("]");
        }

        sb.append("}");
        return sb.toString();
    }

    private void appendEmbedJson(@NonNull StringBuilder sb, @NonNull WebhookEmbed embed) {
        sb.append("{");
        boolean first = true;

        if (embed.title() != null) {
            sb.append("\"title\":\"").append(escapeJson(embed.title())).append("\"");
            first = false;
        }
        if (embed.description() != null) {
            if (!first) sb.append(",");
            sb.append("\"description\":\"").append(escapeJson(embed.description())).append("\"");
            first = false;
        }
        if (embed.color() != null) {
            if (!first) sb.append(",");
            sb.append("\"color\":").append(embed.color());
            first = false;
        }

        if (embed.thumbnailUrl() != null) {
            if (!first) sb.append(",");
            sb.append("\"thumbnail\":{\"url\":\"").append(escapeJson(embed.thumbnailUrl())).append("\"}");
            first = false;
        }

        if (embed.footerText() != null) {
            if (!first) sb.append(",");
            sb.append("\"footer\":{");
            sb.append("\"text\":\"").append(escapeJson(embed.footerText())).append("\"");
            if (embed.footerIcon() != null) {
                sb.append(",\"icon_url\":\"").append(escapeJson(embed.footerIcon())).append("\"");
            }
            sb.append("}");
            first = false;
        }

        if (embed.authorName() != null) {
            if (!first) sb.append(",");
            sb.append("\"author\":{");
            sb.append("\"name\":\"").append(escapeJson(embed.authorName())).append("\"");
            if (embed.authorUrl() != null) {
                sb.append(",\"url\":\"").append(escapeJson(embed.authorUrl())).append("\"");
            }
            if (embed.authorIcon() != null) {
                sb.append(",\"icon_url\":\"").append(escapeJson(embed.authorIcon())).append("\"");
            }
            sb.append("}");
            first = false;
        }

        List<WebhookEmbedField> fields = embed.fields();
        if (!fields.isEmpty()) {
            if (!first) sb.append(",");
            sb.append("\"fields\":[");
            for (int i = 0; i < fields.size(); i++) {
                if (i > 0) sb.append(",");
                WebhookEmbedField f = fields.get(i);
                sb.append("{");
                sb.append("\"name\":\"").append(escapeJson(f.name())).append("\",");
                sb.append("\"value\":\"").append(escapeJson(f.value())).append("\",");
                sb.append("\"inline\":").append(f.inline());
                sb.append("}");
            }
            sb.append("]");
        }

        sb.append("}");
    }

    public static final class Builder {
        private final String url;
        private final List<WebhookEmbed> embeds = new ArrayList<>();
        private String username;
        private String avatarUrl;
        private String content;

        private Builder(String url) {
            this.url = url;
        }

        @CheckReturnValue
        public @NonNull Builder username(@Nullable String username) {
            this.username = username;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder avatarUrl(@Nullable String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder content(@Nullable String content) {
            this.content = content;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder embed(@NonNull WebhookEmbed embed) {
            this.embeds.add(embed);
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder embed(@NonNull Consumer<WebhookEmbed.@NonNull Builder> builderConsumer) {
            WebhookEmbed.Builder builder = WebhookEmbed.builder();
            builderConsumer.accept(builder);
            this.embeds.add(builder.build());
            return this;
        }

        @CheckReturnValue
        public @NonNull Promise<Void> sendAsync() {
            return new Webhook(this).sendAsync();
        }
    }
}
