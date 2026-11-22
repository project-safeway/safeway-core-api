package com.safeway.tech.repository;

import com.safeway.tech.domain.models.UserPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserPhotoRepository extends JpaRepository<UserPhoto, UUID> {

    Optional<UserPhoto> findByUserIdAndActiveTrue(UUID userId);

    boolean existsByUserIdAndActiveTrue(UUID userId);
}
