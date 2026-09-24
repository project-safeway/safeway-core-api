package com.safeway.tech.domain.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "responsavel_alunos")
@Getter @Setter
@NoArgsConstructor
public class ResponsavelAluno extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Guardian guardian;

    @ManyToOne
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

}
