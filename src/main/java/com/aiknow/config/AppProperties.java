package com.aiknow.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Gemini gemini = new Gemini();
    private final Storage storage = new Storage();
    private final Rag rag = new Rag();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long expirationMs;
        private long refreshExpirationMs;
    }

    @Getter
    @Setter
    public static class Gemini {
        private String apiKey;
        private String chatModel;
        private String embeddingModel;
        private int maxRetries = 3;
        private long initialBackoffMs = 500;
    }

    @Getter
    @Setter
    public static class Storage {
        private String uploadDir;
    }

    @Getter
    @Setter
    public static class Rag {
        private int topK = 5;
        private double minSimilarity = 0.65;
        private int maxHistoryMessages = 10;
    }
}
