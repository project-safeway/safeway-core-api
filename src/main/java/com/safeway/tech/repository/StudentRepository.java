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

    @Query("SELECT a FROM Student a WHERE a.id IN :ids AND a.usuario.id = :userId")
    List<Student> findByIdInAndIdUsuario(@Param("ids") List<UUID> ids, @Param("userId") UUID userId);

    @Query("SELECT a FROM Student a WHERE a.ativo = true AND a.usuario.id = :userId")
    List<Student> findByAtivoTrueAndIdUsuario(@Param("userId") UUID userId);

    @Query("SELECT a FROM Student a WHERE a.id = :alunoId AND a.usuario.id = :userId AND a.ativo = true")
    Optional<Student> findByIdAndUsuarioId(@Param("studentId") UUID alunoId, @Param("userId") UUID userId);
}
