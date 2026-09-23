package com.aiknow.ai;

import java.util.List;

public interface EmbeddingService {
    List<float[]> generateEmbeddings(List<String> texts);
    float[] generateEmbedding(String text);
}
