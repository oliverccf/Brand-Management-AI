package com.nocode.ai.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nocode.ai.domain.model.SocialMessage;
import com.nocode.ai.service.BrandAnalyzerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/analyzer")
@RequiredArgsConstructor
public class AnalyzerController {

    private final BrandAnalyzerService analyzerService;

    @PostMapping("/analyze")
    public ResponseEntity<SocialMessage> analyze(@RequestBody AnalysisRequest request) {
        SocialMessage processed = analyzerService.processNewMessage(
                request.brandId(),
                request.content(), 
                request.platform(), 
                request.user()
        );
        return ResponseEntity.ok(processed);
    }

    public record AnalysisRequest(java.util.UUID brandId, String content, String platform, String user) {}
}
