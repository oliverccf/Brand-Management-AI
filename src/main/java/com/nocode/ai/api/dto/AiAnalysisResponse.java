package com.nocode.ai.api.dto;

import java.util.List;

import com.nocode.ai.domain.model.AnalysisResult.Sentiment;

/**
 * Structured response from the AI Agent using Java 25 Records.
 */
public record AiAnalysisResponse(
    Sentiment sentiment,
    String category,
    String summary,
    Double confidenceScore,
    List<String> keywords,
    List<String> suggestedActions,
    boolean requiresUrgentAttention,
    boolean verificationRequired
) {}
