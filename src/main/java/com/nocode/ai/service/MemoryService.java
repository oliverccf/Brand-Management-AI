package com.nocode.ai.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nocode.ai.domain.model.AnalysisResult;
import com.nocode.ai.domain.model.SocialMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Memory Service identified in Phase 2 of the roadmap.
 * Handles summarizing and storing interactions for RAG retrieval.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MemoryService {

    private final ChatClient.Builder chatClientBuilder;
    private final KnowledgeIngestionService knowledgeIngestionService;

    /**
     * Summarizes the interaction asynchronously and saves it to the vector store.
     */
    @Async
    public void saveInteractionMemory(SocialMessage message, AnalysisResult result) {
        log.info("Starting asynchronous memory summarization for message ID: {}", message.getId());
        
        try {
            // Build a dedicated ChatClient for summarization to avoid interfering with the main analysis context
            ChatClient summarizer = chatClientBuilder.build();

            String memoryPrompt = String.format("""
                Summarize this customer interaction in ONE concise sentence focusing on facts and sentiment for future RAG retrieval.
                Platform: %s
                User: %s
                Message: %s
                Sentiment: %s
                Category: %s
                """, 
                message.getPlatform(),
                message.getPlatformUser(),
                message.getContent(),
                result.getSentiment(),
                result.getCategory()
            );

            String summary = summarizer.prompt()
                    .user(memoryPrompt)
                    .call()
                    .content();

            if (summary == null || summary.isEmpty()) {
                log.warn("Empty summary generated for memory of message ID: {}", message.getId());
                return;
            }

            // Prepare metadata for vector store
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("message_id", message.getId().toString());
            if (message.getCustomerId() != null) {
                metadata.put("customer_id", message.getCustomerId().toString());
            }
            if (message.getBrandId() != null) {
                metadata.put("brand_id", message.getBrandId().toString());
            }
            metadata.put("channel_type", message.getChannelType() != null ? message.getChannelType().name() : "UNKNOWN");
            metadata.put("platform", message.getPlatform());
            metadata.put("sentiment", result.getSentiment().name());
            metadata.put("category", result.getCategory());
            metadata.put("type", "conversation_history");
            metadata.put("date", result.getAnalyzedAt().toString());

            // Save to vector store via KnowledgeIngestionService
            knowledgeIngestionService.ingestText(summary, metadata);
            
            log.info("Successfully saved asynchronous conversation memory for message ID: {}", message.getId());
        } catch (Exception e) {
            log.error("Failed to save conversation memory for message ID: {}", message.getId(), e);
        }
    }
}
