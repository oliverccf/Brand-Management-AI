package com.nocode.ai.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.web.server.LocalServerPort;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import java.util.HashMap;
import java.util.Map;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.client.RestTemplate;
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, 
    properties = "spring.autoconfigure.exclude=org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration")
public class CucumberSpringConfiguration {

    @LocalServerPort
    private int port;

    public int getPort() {
        return port;
    }



    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
        .withDatabaseName("brand_analyzer")
        .withUsername("postgres")
        .withPassword("postgres");

    static {
        kafka.start();
        postgres.start();
    }

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.enabled", () -> "true");
        registry.add("spring.ai.vectorstore.pgvector.initialize-schema", () -> "true");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest"); 
    }

    @TestConfiguration
    static class CucumberTestConfig {

        @Bean
        @Primary
        public ChatClient.Builder chatClientBuilder(ChatModel primaryChatModel) {
            return ChatClient.builder(primaryChatModel);
        }

        @Bean
        public ChatClient chatClient(ChatClient.Builder builder) {
            return builder.build();
        }

        @Bean
        @Primary
        public ChatModel primaryChatModel() {
            ChatModel chatModel = mock(ChatModel.class);
            
                
            when(chatModel.call(org.mockito.ArgumentMatchers.any(org.springframework.ai.chat.prompt.Prompt.class)))
                .thenAnswer(invocation -> {
                    org.springframework.ai.chat.prompt.Prompt prompt = invocation.getArgument(0);
                    boolean verificationRequired = prompt.getInstructions().stream()
                        .anyMatch(msg -> msg.toString().contains("verificationRequired=true") || 
                                       msg.toString().contains("TRUST CONTEXT"));
                    
                    String actions = verificationRequired ? 
                        "[\"Request account linkage for history access\"]" : "[\"Thank user\"]";
                    
                    String response = """
                        {
                            "sentiment": "POSITIVE",
                            "category": "PRAISE",
                            "summary": "User interaction.",
                            "confidenceScore": 0.95,
                            "keywords": ["test"],
                            "suggestedActions": %s,
                            "requiresUrgentAttention": false,
                            "verificationRequired": %b
                        }
                        """.formatted(actions, verificationRequired);
                        
                    org.springframework.ai.chat.model.Generation dynamicGen = new org.springframework.ai.chat.model.Generation(
                        new org.springframework.ai.chat.messages.AssistantMessage(response)
                    );
                    return new org.springframework.ai.chat.model.ChatResponse(List.of(dynamicGen));
                });
                
            return chatModel;
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public ProducerFactory<String, Object> producerFactory() {
            Map<String, Object> props = new HashMap<>();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            return new DefaultKafkaProducerFactory<>(props);
        }

        @Bean
        public KafkaTemplate<String, Object> kafkaTemplate() {
            return new KafkaTemplate<>(producerFactory());
        }

        @Bean
        public ConsumerFactory<String, Object> consumerFactory() {
            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
            return new DefaultKafkaConsumerFactory<>(props);
        }

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
            ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                    new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory());
            return factory;
        }

        @Bean
        @Primary
        public EmbeddingModel primaryEmbeddingModel() {
            return mock(EmbeddingModel.class);
        }

        @Bean
        @Primary
        public VectorStore vectorStore() {
            return mock(VectorStore.class);
        }

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }
}
