package com.nocode.ai.domain.model;

import java.time.OffsetDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "analysis_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "message_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private SocialMessage message;

    @Enumerated(EnumType.STRING)
    private Sentiment sentiment;

    @Column(name = "brand_id")
    private java.util.UUID brandId;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(length = 100)
    private String category; // COMPLAINT, PRAISE, QUESTION, etc.

    private Double confidenceScore;

    @Column(name = "analyzed_at")
    @Builder.Default
    private OffsetDateTime analyzedAt = OffsetDateTime.now();


    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String rawAiResponse;

    @Column(columnDefinition = "TEXT")
    private String publicClosingMessage;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.OPEN;

    public enum Sentiment {
        POSITIVE, NEUTRAL, NEGATIVE, URGENT
    }

    public enum Status {
        OPEN, IN_PROGRESS, RESOLVED
    }
}
