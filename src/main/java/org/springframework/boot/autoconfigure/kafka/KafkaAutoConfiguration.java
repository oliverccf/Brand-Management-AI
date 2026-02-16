package org.springframework.boot.autoconfigure.kafka;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Bridge for Spring AI 1.0.0-M6 which expects this class in the old package.
 * In Spring Boot 4.0.2, it was moved to org.springframework.boot.kafka.autoconfigure.
 */
@AutoConfiguration
@Import(org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration.class)
public class KafkaAutoConfiguration {
}
