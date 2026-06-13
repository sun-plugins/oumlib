package dev.oum.oumlib.web;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class WebhookEmbed {

    private final String title;
    private final String description;
    private final Integer color;
    private final List<WebhookEmbedField> fields;
    private final String footerText;
    private final String footerIcon;
    private final String thumbnailUrl;
    private final String authorName;
    private final String authorUrl;
    private final String authorIcon;

    @Contract(pure = true)
    private WebhookEmbed(@NonNull Builder builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.color = builder.color;
        this.fields = List.copyOf(builder.fields);
        this.footerText = builder.footerText;
        this.footerIcon = builder.footerIcon;
        this.thumbnailUrl = builder.thumbnailUrl;
        this.authorName = builder.authorName;
        this.authorUrl = builder.authorUrl;
        this.authorIcon = builder.authorIcon;
    }

    @CheckReturnValue
    public static @NonNull Builder builder() {
        return new Builder();
    }

    public @Nullable String title() {
        return title;
    }

    public @Nullable String description() {
        return description;
    }

    public @Nullable Integer color() {
        return color;
    }

    public @NonNull List<WebhookEmbedField> fields() {
        return fields;
    }

    public @Nullable String footerText() {
        return footerText;
    }

    public @Nullable String footerIcon() {
        return footerIcon;
    }

    public @Nullable String thumbnailUrl() {
        return thumbnailUrl;
    }

    public @Nullable String authorName() {
        return authorName;
    }

    public @Nullable String authorUrl() {
        return authorUrl;
    }

    public @Nullable String authorIcon() {
        return authorIcon;
    }

    public static final class Builder {
        private final List<WebhookEmbedField> fields = new ArrayList<>();
        private String title;
        private String description;
        private Integer color;
        private String footerText;
        private String footerIcon;
        private String thumbnailUrl;
        private String authorName;
        private String authorUrl;
        private String authorIcon;

        private Builder() {
        }

        @CheckReturnValue
        public @NonNull Builder title(@Nullable String title) {
            this.title = title;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder description(@Nullable String description) {
            this.description = description;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder color(@Nullable Integer color) {
            this.color = color;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder field(@NonNull String name, @NonNull String value, boolean inline) {
            this.fields.add(new WebhookEmbedField(name, value, inline));
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder footer(@Nullable String text, @Nullable String iconUrl) {
            this.footerText = text;
            this.footerIcon = iconUrl;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder thumbnail(@Nullable String url) {
            this.thumbnailUrl = url;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder author(@Nullable String name, @Nullable String url, @Nullable String iconUrl) {
            this.authorName = name;
            this.authorUrl = url;
            this.authorIcon = iconUrl;
            return this;
        }

        @Contract(" -> new")
        public @NonNull WebhookEmbed build() {
            return new WebhookEmbed(this);
        }
    }
}
