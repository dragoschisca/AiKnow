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
        try {
            for (String text : texts) {
                // Using the basic approach assuming EmbedContentResponse structure
                EmbedContentResponse response = client.models.embedContent(
                        model,
                        text,
                        null
                );
                List<Float> values = response.embeddings().get().get(0).values().get();
                float[] arr = new float[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    arr[i] = values.get(i);
                }
                embeddings.add(arr);
            }
            return embeddings;
        } catch (Exception e) {
            log.error("Failed to generate embeddings", e);
            throw new RuntimeException("Failed to generate embeddings", e);
        }
    }

    @Override
    public float[] generateEmbedding(String text) {
        return generateEmbeddings(List.of(text)).get(0);
    }
}
