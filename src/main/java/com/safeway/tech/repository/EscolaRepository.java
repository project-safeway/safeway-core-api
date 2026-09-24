package com.safeway.tech.repository;

import com.safeway.tech.domain.models.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EscolaRepository extends JpaRepository<School, UUID> {
    @Query("SELECT DISTINCT e FROM School e LEFT JOIN FETCH e.alunos WHERE e.usuario.id = :usuarioId AND e.ativo = true")
    List<School> findByUsuarioIdUsuario(@Param("usuarioId") UUID usuarioId);

    @Query("SELECT e FROM School e LEFT JOIN FETCH e.alunos WHERE e.id = :idEscola AND e.usuario.id = :userId AND e.ativo = true")
    Optional<School> findByIdEscolaAndIdUsuario(@Param("idEscola") UUID idEscola, @Param("userId") UUID userId);
}
