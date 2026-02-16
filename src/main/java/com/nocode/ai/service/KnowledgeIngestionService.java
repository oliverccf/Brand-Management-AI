package com.nocode.ai.service;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeIngestionService {

    private final VectorStore vectorStore;

    public void ingestDocument(Resource resource) {
        log.info("Ingesting document: {}", resource.getFilename());
        
        // 1. Read document
        TextReader reader = new TextReader(resource);
        List<Document> documents = reader.get();
        
        // 2. Split into chunks to fit context window
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.apply(documents);
        
        // 3. Store in Vector DB (will automatically generate embeddings)
        vectorStore.accept(chunks);
        
        log.info("Successfully ingested {} chunks into vector store", chunks.size());
    }

    /**
     * Ingest raw text with custom metadata (for conversation memory)
     * @param text The text content to ingest
     * @param metadata Custom metadata (e.g., customer_id, type, date)
     */
    public void ingestText(String text, Map<String, Object> metadata) {
        log.info("Ingesting text with metadata: {}", metadata);
        
        // Create document with metadata
        Document document = new Document(text, metadata);
        
        // Split if text is too long
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.apply(List.of(document));
        
        // Store in Vector DB
        vectorStore.accept(chunks);
        
        log.info("Successfully ingested {} chunks with metadata into vector store", chunks.size());
    }
}

