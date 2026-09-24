package com.safeway.tech.domain.models;

import com.safeway.tech.domain.enums.StatusPresencaEnum;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chamada_alunos")
@Getter @Setter
@NoArgsConstructor
public class ChamadaAluno extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private StatusPresencaEnum presenca;

    private LocalDateTime data;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Attendance attendance;

    @ManyToOne(fetch = FetchType.LAZY)
    private Student student;
}
