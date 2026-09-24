package com.safeway.tech.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "itinerario_alunos")
@Getter @Setter
@NoArgsConstructor
public class ItinerarioAluno extends BaseEntity {

    @Column(name = "ordem_embarque")
    private Integer ordemEmbarque;

    @Column(name = "ordem_global")
    private Integer ordemGlobal;

    @ManyToOne
    @JoinColumn(name = "itinerario_id", nullable = false)
    private Route route;

    @ManyToOne
    @JoinColumn(name = "aluno_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endereco_id", nullable = false)
    private Address address;

}
