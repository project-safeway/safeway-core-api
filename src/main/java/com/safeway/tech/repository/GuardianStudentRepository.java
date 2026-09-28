package com.safeway.tech.repository;

import com.safeway.tech.domain.models.GuardianStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GuardianStudentRepository extends JpaRepository<GuardianStudent, UUID> {
}
