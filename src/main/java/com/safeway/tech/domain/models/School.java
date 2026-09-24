package com.safeway.tech.domain.models;

import com.safeway.tech.domain.enums.EducationLevelEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "schools")
@Getter @Setter
@NoArgsConstructor
public class School extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level")
    private EducationLevelEnum educationLevel;

    @ManyToOne
    @JoinColumn(name = "transport_id", nullable = false)
    private Transport transport;

    @OneToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;
}
