package com.nocode.ai.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.Answers;
import org.springframework.context.annotation.Primary;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import java.util.HashMap;

import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.nocode.ai.api.dto.AiAnalysisResponse;
import com.nocode.ai.domain.model.AnalysisResult;
import com.nocode.ai.domain.model.AnalysisResult.Sentiment;
import com.nocode.ai.domain.repository.AnalysisResultRepository;
import com.nocode.ai.messaging.SocialMediaConsumer.SocialMessageDTO;
import com.nocode.ai.service.KnowledgeIngestionService;
import com.nocode.ai.service.IdentityService;
import static org.mockito.ArgumentMatchers.any;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SocialMediaKafkaSimulationTest {

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
        registry.add("spring.kafka.enabled", () -> "true"); // Importante: Habilita o consumidor
        registry.add("spring.ai.vectorstore.pgvector.initialize-schema", () -> "true");
        registry.add("spring.flyway.enabled", () -> "false"); // Disable flyway if not used or to rely on JPA ddl-auto
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @TestConfiguration
    static class KafkaConfig {

        @Bean
        public ProducerFactory<String, Object> producerFactory() {
            Map<String, Object> props = new HashMap<>();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            return new DefaultKafkaProducerFactory<>(props);
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
        public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(ConsumerFactory<String, Object> consumerFactory) {
            ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            return factory;
        }

        @Bean
        public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
            return new KafkaTemplate<>(producerFactory);
        }

        @Bean
        @Primary
        public ChatClient.Builder chatClientBuilder(ChatClient chatClient) {
            // Use RETURNS_SELF so chained calls in constructor don't return null
            ChatClient.Builder builder = mock(ChatClient.Builder.class, Answers.RETURNS_SELF);
            when(builder.build()).thenReturn(chatClient);
            return builder;
        }

        @Bean
        public ChatClient chatClient() {
            return mock(ChatClient.class);
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private AnalysisResultRepository resultRepository;


    // These mocks are returned by the builder chain, we need to stub the builder to return them in setup()
    // or rely on deep stubs. Since we used RETURNS_SELF for builder, it returns itself.
    // But build() returns ChatClient.
    
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec callResponseSpec;

    @MockitoBean
    private OllamaChatModel ollamaChatModel;

    @MockitoBean
    private OllamaEmbeddingModel ollamaEmbeddingModel;

    @MockitoBean
    private VectorStore vectorStore;

    @MockitoBean
    private KnowledgeIngestionService knowledgeIngestionService;

    @MockitoBean
    private IdentityService identityService;

    @Autowired
    private ChatClient chatClient;

    @BeforeEach
    void setup() {
        // Mock Identity Service
        when(identityService.resolveIdentity(any(), any(), any())).thenReturn(java.util.UUID.randomUUID());
        when(identityService.getTrustLevel(any(), any(), any())).thenReturn(com.nocode.ai.domain.model.CustomerIdentity.TrustLevel.TRUSTED);

        // Configuração do Mock da AI (para focar no teste do Kafka)
        // chatClient is already autowired and injected into the Service via the Builder bean
        
        // With RETURNS_SELF, the builder methods return the builder instance.
        // build() is already stubbed in the configuration.
        
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class, Answers.RETURNS_SELF);
        callResponseSpec = mock(ChatClient.CallResponseSpec.class, Answers.RETURNS_SELF);

        // Mock the ChatClient flow
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);

        // Mock AI response - JSON string that will be parsed
        String mockJsonResponse = """
            {
                "sentiment": "POSITIVE",
                "category": "PRAISE",
                "summary": "User is happy with the product.",
                "confidenceScore": 0.95,
                "keywords": ["happy", "product"],
                "suggestedActions": ["Thank user"],
                "requiresUrgentAttention": false,
                "verificationRequired": false
            }
            """;
        
        // CRITICAL: Mock .content() to return the JSON string
        when(callResponseSpec.content()).thenReturn(mockJsonResponse);

        AiAnalysisResponse mockResponse = new AiAnalysisResponse(
            Sentiment.POSITIVE, 
            "PRAISE", 
            "User is happy with the product.", 
            0.95,
            List.of("happy", "product"),
            List.of("Thank user"),
            false,
            false
        );
        when(callResponseSpec.entity(AiAnalysisResponse.class)).thenReturn(mockResponse);
    }

    @Test
    void simulateRealSocialMediaTraffic() {
        java.util.UUID testBrandId = java.util.UUID.randomUUID();
        // Create messages simulating different networks
        List<SocialMessageDTO> messages = List.of(
            new SocialMessageDTO(testBrandId, "I love the new features! #awesome", "TWITTER", "@happy_user"),
            new SocialMessageDTO(testBrandId, "Wait time for support is too long.", "FACEBOOK", "angry_customer"),
            new SocialMessageDTO(testBrandId, "Check out this unboxing video!", "TIKTOK", "influencer_123"),
            new SocialMessageDTO(testBrandId, "Is this compatible with Mac?", "REDDIT", "tech_question")
        );

        // Send messages to Kafka
        messages.forEach(msg -> {
            kafkaTemplate.send("social-media-messages", msg);
            try {
                // Simulate real-time arrival
                Thread.sleep(500); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Verify consumption and processing
        // We expect 4 results in the database eventually
        Awaitility.await()
            .atMost(30, TimeUnit.SECONDS)
            .pollInterval(Duration.ofMillis(500))
            .until(() -> resultRepository.count() >= 4);

        System.out.println("Processing completed! Found " + resultRepository.count() + " analysis results.");
        
        List<AnalysisResult> results = resultRepository.findAll();
        results.forEach(r -> {
            System.out.println("Result: " + r);
        });
    }
}
