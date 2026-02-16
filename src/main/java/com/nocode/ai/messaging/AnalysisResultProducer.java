package com.nocode.ai.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.nocode.ai.domain.model.AnalysisResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class AnalysisResultProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.analysis-results}")
    private String topic;

    public void publishResult(AnalysisResult result) {
        AnalysisResultDTO dto = new AnalysisResultDTO(
            result.getMessage().getId(),
            result.getSentiment().name(),
            result.getCategory(),
            result.getConfidenceScore(),
            result.getPublicClosingMessage()
        );
        
        kafkaTemplate.send(topic, result.getMessage().getId().toString(), dto);
        log.info("Published analysis result for message {}", result.getMessage().getId());
    }

    public record AnalysisResultDTO(Long messageId, String sentiment, String category, Double confidence, String publicClosingMessage) {}
}
