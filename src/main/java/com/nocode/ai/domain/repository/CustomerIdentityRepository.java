package com.nocode.ai.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nocode.ai.domain.model.CustomerIdentity;
import com.nocode.ai.domain.model.SocialMessage.ChannelType;

@Repository
public interface CustomerIdentityRepository extends JpaRepository<CustomerIdentity, UUID> {
    Optional<CustomerIdentity> findByBrandIdAndChannelTypeAndPlatformUserId(UUID brandId, ChannelType channelType, String platformUserId);
}
