package com.safeway.tech.repository;

import com.safeway.tech.domain.models.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GuardianRepository extends JpaRepository<Guardian, UUID> {

    @Query("SELECT r FROM Guardian r WHERE r.usuario.id = :userId")
    List<Guardian> findAllByIdUsuario(@Param("userId") UUID userId);

    @Query("SELECT r FROM Guardian r JOIN r.alunos a WHERE a.id = :alunoId AND r.usuario.id = :usuarioId")
    List<Guardian> findByAlunosIdAndUsuarioIdUsuario(@Param("studentId") UUID alunoId, @Param("usuarioId") UUID usuarioId);

    @Query("SELECT r FROM Guardian r WHERE r.id = :idResponsavel AND r.usuario.id = :usuarioId")
    Optional<Guardian> findByIdResponsavelAndIdUsuario(@Param("idResponsavel") UUID idResponsavel, @Param("usuarioId") UUID usuarioId);

    @Query("SELECT r FROM Guardian r WHERE r.cpf = :cpf AND r.usuario.id = :usuarioId")
    Optional<Guardian> findByCpfAndIdUsuario(@Param("cpf") String cpf, @Param("usuarioId") UUID usuarioId);
}
