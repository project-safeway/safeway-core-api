package com.safeway.tech.repository;

import com.safeway.tech.domain.models.Student;
import com.safeway.tech.domain.models.Attendance;
import com.safeway.tech.domain.models.ChamadaAluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ChamadaAlunoRepository extends JpaRepository<ChamadaAluno, UUID> {

    @Query("SELECT ca FROM ChamadaAluno ca WHERE ca.chamada = :chamada AND ca.aluno = :aluno")
    Optional<ChamadaAluno> findByChamadaAndAluno(
            @Param("chamada") Attendance attendance,
            @Param("student") Student student);
}
