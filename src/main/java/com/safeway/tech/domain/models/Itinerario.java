package com.safeway.tech.domain.models;

import com.safeway.tech.domain.enums.TipoViagemEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Time;

@Entity
@Table(name = "itinerarios")
@Getter @Setter
@NoArgsConstructor
public class Itinerario extends BaseEntity {

    private String nome;

    @Column(name = "horario_inicio")
    private Time horarioInicio;

    @Column(name = "horario_fim")
    private Time horarioFim;

    private TipoViagemEnum tipoViagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transporte_id", nullable = false)
    private Transport transport;
}
