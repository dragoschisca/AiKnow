package com.aiknow.ai;

import com.aiknow.config.AppProperties;
import com.google.genai.Client;
import com.google.genai.types.EmbedContentResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiEmbeddingService implements EmbeddingService {

    private final AppProperties appProperties;
    private Client client;
    private String model;

    @PostConstruct
    public void init() {
        this.client = Client.builder().apiKey(appProperties.getGemini().getApiKey()).build();
        this.model = appProperties.getGemini().getEmbeddingModel();
    }

    @Override
    public List<float[]> generateEmbeddings(List<String> texts) {
        List<float[]> embeddings = new ArrayList<>();
        for (String text : texts) {
            embeddings.add(embedWithRetry(text));
        }
        return embeddings;
    }

    @Override
    public float[] generateEmbedding(String text) {
        return embedWithRetry(text);
    }

    private float[] embedWithRetry(String text) {
        int maxRetries = appProperties.getGemini().getMaxRetries();
        long backoffMs = appProperties.getGemini().getInitialBackoffMs();

        RuntimeException lastError = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                EmbedContentResponse response = client.models.embedContent(model, text, null);
                List<Float> values = response.embeddings().get().get(0).values().get();
                float[] arr = new float[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    arr[i] = values.get(i);
                }
                return arr;
            } catch (RuntimeException e) {
                lastError = e;
                if (attempt == maxRetries) {
                    break;
                }
                long delay = backoffMs * (1L << attempt);
                log.warn("Gemini embedding call failed (attempt {}/{}), retrying in {}ms: {}",
                        attempt + 1, maxRetries + 1, delay, e.getMessage());
                sleep(delay);
            }
        }
        log.error("Failed to generate embedding after {} attempts", maxRetries + 1, lastError);
        throw new RuntimeException("Failed to generate embedding after " + (maxRetries + 1) + " attempts", lastError);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while retrying Gemini embedding call", ie);
        }
    }
}
