package org.springframework.boot.autoconfigure.jdbc;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Bridge for Spring AI 1.0.0-M6 which expects this class in the old package.
 * In Spring Boot 4.0.2, it was moved to org.springframework.boot.jdbc.autoconfigure.
 */
@AutoConfiguration
@Import(org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration.class)
public class JdbcTemplateAutoConfiguration {
}
