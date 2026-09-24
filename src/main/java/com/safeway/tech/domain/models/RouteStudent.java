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
@Table(name = "route_students")
@Getter @Setter
@NoArgsConstructor
public class RouteStudent extends BaseEntity {

    @Column(name = "boarding_order")
    private Integer boardingOrder;

    @Column(name = "general_order")
    private Integer generalOrder;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

}
