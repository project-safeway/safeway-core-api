package com.safeway.tech.repository;

import com.safeway.tech.domain.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    @Query("SELECT s FROM Student s JOIN GuardianStudent gs ON gs.student = s " +
            "WHERE s.id IN :ids AND gs.guardian.user.id = :userId")
    List<Student> findByIdInAndUserId(@Param("ids") List<UUID> ids, @Param("userId") UUID userId);

    @Query("SELECT s FROM Student s JOIN GuardianStudent gs ON gs.student = s " +
            "WHERE s.active = true AND gs.guardian.user.id = :userId")
    List<Student> findAllByActiveTrueAndUserId(@Param("userId") UUID userId);

    @Query("SELECT s FROM Student s JOIN GuardianStudent gs ON gs.student = s " +
            "WHERE s.id = :studentId AND gs.guardian.user.id = :userId AND s.active = true")
    Optional<Student> findByIdAndUserId(@Param("studentId") UUID studentId, @Param("userId") UUID userId);
}
