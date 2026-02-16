package com.nocode.ai.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nocode.ai.domain.model.CustomerIdentity;
import com.nocode.ai.domain.model.CustomerIdentity.TrustLevel;
import com.nocode.ai.domain.model.SocialMessage.ChannelType;
import com.nocode.ai.domain.repository.CustomerIdentityRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityService {

    private final CustomerIdentityRepository identityRepository;

    /**
     * Resolves the identity of a customer based on the brand, platform and user ID.
     * If the identity does not exist, a new one is created.
     * 
     * @param brandId The ID of the brand
     * @param platformUserId The user ID on the platform (e.g., phone number, handle)
     * @param channelType The type of channel (e.g., WHATSAPP, INSTAGRAM)
     * @return The unified Customer ID (UUID)
     */
    @Transactional
    public UUID resolveIdentity(UUID brandId, String platformUserId, ChannelType channelType) {
        return identityRepository.findByBrandIdAndChannelTypeAndPlatformUserId(brandId, channelType, platformUserId)
                .map(identity -> {
                    log.info("Found existing identity for user: {} on channel: {} for brand: {}", platformUserId, channelType, brandId);
                    // Update last seen
                    identity.setLastSeenAt(java.time.OffsetDateTime.now());
                    identityRepository.save(identity);
                    return identity.getCustomerId();
                })
                .orElseGet(() -> {
                    log.info("Creating new identity for user: {} on channel: {} for brand: {}", platformUserId, channelType, brandId);
                    UUID newCustomerId = UUID.randomUUID();
                    CustomerIdentity newIdentity = CustomerIdentity.builder()
                            .customerId(newCustomerId)
                            .brandId(brandId)
                            .channelType(channelType)
                            .platformUserId(platformUserId)
                            .trustLevel(TrustLevel.UNVERIFIED)
                            .build();
                    identityRepository.save(newIdentity);
                    return newCustomerId;
                });
    }

    /**
     * Links a new identity to an existing customer ID (Identity Merging).
     */
    @Transactional
    public void linkIdentity(UUID brandId, UUID existingCustomerId, String platformUserId, ChannelType channelType, TrustLevel level) {
        log.info("Linking user: {} on channel: {} to existing Customer: {} for brand: {}", platformUserId, channelType, existingCustomerId, brandId);
        
        CustomerIdentity identity = identityRepository.findByBrandIdAndChannelTypeAndPlatformUserId(brandId, channelType, platformUserId)
                .orElseGet(() -> CustomerIdentity.builder()
                        .brandId(brandId)
                        .channelType(channelType)
                        .platformUserId(platformUserId)
                        .build());
        
        identity.setCustomerId(existingCustomerId);
        identity.setTrustLevel(level);
        identity.setLastSeenAt(java.time.OffsetDateTime.now());
        identity.setVerifiedAt(java.time.OffsetDateTime.now());
        
        identityRepository.save(identity);
    }

    public TrustLevel getTrustLevel(UUID brandId, String platformUserId, ChannelType channelType) {
        return identityRepository.findByBrandIdAndChannelTypeAndPlatformUserId(brandId, channelType, platformUserId)
                .map(CustomerIdentity::getTrustLevel)
                .orElse(TrustLevel.UNVERIFIED);
    }
}
