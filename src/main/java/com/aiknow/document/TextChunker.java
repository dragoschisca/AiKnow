package com.aiknow.document;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TextChunker {

    private int chunkSize = 800;
    private int chunkOverlap = 100;

    public record ChunkResult(int chunkIndex, int pageNumber, String content, int tokenCount) {}

    public record PageText(int pageNumber, String text) {}

    public List<ChunkResult> chunkText(List<PageText> pages) {
        List<ChunkResult> results = new ArrayList<>();
        if (pages == null || pages.isEmpty()) {
            return results;
        }

        StringBuilder accumulatedText = new StringBuilder();
        int chunkIndex = 0;
        int currentPageStart = pages.get(0).pageNumber();

        for (int i = 0; i < pages.size(); i++) {
            PageText page = pages.get(i);
            String text = page.text();
            if (text == null || text.trim().isEmpty()) {
                continue;
            }
            
            if (accumulatedText.isEmpty()) {
                currentPageStart = page.pageNumber();
            } else {
                accumulatedText.append(" ");
            }
            
            accumulatedText.append(text);

            while (accumulatedText.length() >= chunkSize) {
                String chunkContent = accumulatedText.substring(0, chunkSize);
                int tokenCount = chunkContent.split("\\s+").length;
                results.add(new ChunkResult(chunkIndex++, currentPageStart, chunkContent, tokenCount));

                accumulatedText.delete(0, chunkSize - chunkOverlap);
            }
        }

        if (!accumulatedText.isEmpty()) {
            String chunkContent = accumulatedText.toString();
            int tokenCount = chunkContent.split("\\s+").length;
            results.add(new ChunkResult(chunkIndex, currentPageStart, chunkContent, tokenCount));
        }

        return results;
    }
}
