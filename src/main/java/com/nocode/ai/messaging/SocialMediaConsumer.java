package com.nocode.ai.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.nocode.ai.service.BrandAnalyzerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class SocialMediaConsumer {

    private final BrandAnalyzerService analyzerService;

    @KafkaListener(topics = "${spring.kafka.topics.social-messages}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(SocialMessageDTO messageDTO) {
        log.info("Received message from Kafka: {}", messageDTO);
        
        try {
            analyzerService.processNewMessage(
                messageDTO.brandId(),
                messageDTO.content(),
                messageDTO.platform(),
                messageDTO.user()
            );
        } catch (Exception e) {
            log.error("Error processing message from Kafka", e);
        }
    }

    public record SocialMessageDTO(java.util.UUID brandId, String content, String platform, String user) {}
}
