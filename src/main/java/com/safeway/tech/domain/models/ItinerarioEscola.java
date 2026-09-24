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
@Table(name = "itinerario_escolas")
@Getter @Setter
@NoArgsConstructor
public class ItinerarioEscola extends BaseEntity {

    @Column(name = "ordem_parada")
    private Integer ordemParada;

    @Column(name = "ordem_global")
    private Integer ordemGlobal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerario_id", nullable = false)
    private Itinerario itinerario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escola_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endereco_id", nullable = false)
    private Address address;
}
