package com.nocode.ai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName("vector_store") // Nome da tabela para os vetores
                .dimensions(384) // Dimensões para all-minilm(384) ou nomic-embed-text(768)
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .removeExistingVectorStoreTable(false)
                .build();
    }
}
