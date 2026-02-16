package com.nocode.ai.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nocode.ai.domain.model.Brand;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID> {
    java.util.Optional<Brand> findByName(String name);
}
