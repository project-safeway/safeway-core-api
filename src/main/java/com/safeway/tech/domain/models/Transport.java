package com.safeway.tech.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "transports")
@Getter @Setter
@NoArgsConstructor
public class Transport extends BaseEntity {

    @Column(nullable = false, length = 7)
    private String licensePlate;
    private String model;
    private Integer capacity;
}
