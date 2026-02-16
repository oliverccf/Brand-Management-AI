package com.nocode.ai.service.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BrandCrisisTools {

    @Tool(description = "Escalate a message to a human manager if it represents a brand crisis or severe legal threat.")
    public String escalateToManager(String reason, String urgencyLevel) {
        log.warn("CRITICAL: Escalating to manager. Reason: {}. Urgency: {}", reason, urgencyLevel);
        // Aqui simularíamos uma integração com Jira, Zendesk ou Slack
        return "Message successfully escalated to the crisis management team. Incident ID: " + System.currentTimeMillis();
    }

    @Tool(description = "Search for similar past incidents in the brand history to check context.")
    public String checkHistory(String topic) {
        log.info("Searching history for topic: {}", topic);
        // Simulação de busca
        return "Similar incidents found in October 2025 regarding 'Late Delivery'. Most were resolved with a refund.";
    }
}
