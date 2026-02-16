package com.nocode.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nocode.ai.domain.model.AnalysisResult;
import com.nocode.ai.domain.repository.AnalysisResultRepository;
import com.nocode.ai.messaging.AnalysisResultProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResolutionService {

    private final AnalysisResultRepository resultRepository;
    private final ChatClient.Builder chatClientBuilder;
    private final AnalysisResultProducer resultProducer;

    @Transactional
    public void resolveCase(Long analysisResultId, String resolutionNotes) {
        AnalysisResult result = resultRepository.findById(analysisResultId)
                .orElseThrow(() -> new IllegalArgumentException("Analysis result not found"));

        // Trigger AI to generate a polite public response
        String publicClosingMessage = generatePublicClosingMessage(result, resolutionNotes);
        result.setPublicClosingMessage(publicClosingMessage);
        result.setStatus(AnalysisResult.Status.RESOLVED);
        resultRepository.save(result);

        // Publish to Kafka so the outbound connector can post it back to the original platform
        // Using the existing producer as an example - in a real app, this might go to a 'public-replies' topic
        resultProducer.publishResult(result); 
        
        log.info("Public closing message generated: {}", publicClosingMessage);
    }

    private String generatePublicClosingMessage(AnalysisResult result, String notes) {
        ChatClient client = chatClientBuilder.build();
        
        return client.prompt()
                .system("You are a Brand Reputation Assistant. Generate a SHORT, polite, and professional public reply " +
                        "for a social media thread (Instagram/Twitter) informing that the customer's issue has been resolved " +
                        "privately. Mention that we are happy to help and ask for feedback if they wish.")
                .user(String.format("Original complaint: %s. Resolution summary: %s. Channel: %s", 
                        result.getMessage().getContent(), notes, result.getMessage().getPlatform()))
                .call()
                .content();
    }
}
