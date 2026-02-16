package com.nocode.ai.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer_identities", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"brand_id", "channel_type", "platform_user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId; // The unified Customer ID

    @Column(name = "brand_id", nullable = false)
    private UUID brandId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false, length = 50)
    private SocialMessage.ChannelType channelType;

    @Column(name = "platform_user_id", nullable = false)
    private String platformUserId; // The identifier on the platform (e.g., phone, @username)

    @Column(name = "trust_level", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TrustLevel trustLevel = TrustLevel.UNVERIFIED;

    @Column(name = "created_at")
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "last_seen_at")
    @Builder.Default
    private OffsetDateTime lastSeenAt = OffsetDateTime.now();

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    public enum TrustLevel {
        TRUSTED,     // Verified phone/email/login
        UNVERIFIED,  // Public social media handle (could be impersonated)
        BLOCKED      // Known spammer/bad actor
    }
}
