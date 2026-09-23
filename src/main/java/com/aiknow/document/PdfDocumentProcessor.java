package com.aiknow.document;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PdfDocumentProcessor {

    public List<TextChunker.PageText> extractPages(InputStream pdfInputStream) {
        List<TextChunker.PageText> pages = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(pdfInputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            int numberOfPages = document.getNumberOfPages();
            
            for (int n = 1; n <= numberOfPages; n++) {
                stripper.setStartPage(n);
                stripper.setEndPage(n);
                String text = stripper.getText(document);
                if (text != null && !text.trim().isEmpty()) {
                    pages.add(new TextChunker.PageText(n, text.trim()));
                }
            }
        } catch (IOException e) {
            log.error("Failed to extract pages from PDF", e);
            throw new RuntimeException("Failed to extract pages from PDF", e);
        }
        return pages;
    }
}
