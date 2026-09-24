package com.safeway.tech.domain.models;

import com.safeway.tech.domain.enums.StatusChamadaEnum;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chamadas")
@Getter @Setter
@NoArgsConstructor
public class Chamada extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private StatusChamadaEnum status;

    @ManyToOne
    @JoinColumn(name = "itinerario_id", nullable = false)
    private Route route;
}
