package com.nocode.ai.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "social_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "brand_id", nullable = false)
    private UUID brandId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", length = 50)
    private ChannelType channelType;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private String platform; // TWITTER, INSTAGRAM, etc.

    @Column(name = "platform_user")
    private String platformUser;

    @Column(name = "message_id_source")
    private String messageIdSource;

    @Column(name = "created_at_source")
    private OffsetDateTime createdAtSource;

    @Column(name = "captured_at")
    @Builder.Default
    private OffsetDateTime capturedAt = OffsetDateTime.now();

    @OneToOne(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private AnalysisResult analysisResult;

    public enum ChannelType {
        WHATSAPP,           // Trusted: Phone verified by Meta
        APP_LOGIN,          // Trusted: Authenticated user session
        INSTAGRAM,          // Open: Requires verification
        TWITTER,            // Open: Requires verification
        FACEBOOK,           // Open: Requires verification
        LINKEDIN,           // Open: Requires verification
        RECLAME_AQUI,       // Open: High-impact complaint platform
        CONSUMIDOR_GOV,     // Open: Government complaint platform
        TIKTOK,             // Open: Requires verification
        YOUTUBE,            // Open: Requires verification
        REDDIT,             // Open: Requires verification
        EMAIL,              // Semi-trusted: Can be verified
        UNKNOWN             // Fallback
    }
}

