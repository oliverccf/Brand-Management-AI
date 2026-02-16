package com.nocode.ai.domain.repository;

import com.nocode.ai.domain.model.SocialMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialMessageRepository extends JpaRepository<SocialMessage, Long> {
}
